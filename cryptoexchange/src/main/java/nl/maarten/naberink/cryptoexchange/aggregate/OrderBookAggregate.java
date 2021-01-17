package nl.maarten.naberink.cryptoexchange.aggregate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.TreeSet;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.modelling.command.AggregateMember;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

import nl.maarten.naberink.cryptoexchange.aggregate.data.BuyOrder;
import nl.maarten.naberink.cryptoexchange.aggregate.data.Order;
import nl.maarten.naberink.cryptoexchange.aggregate.data.SellOrder;
import nl.maarten.naberink.cryptoexchange.commands.CreateOrderBookCommand;
import nl.maarten.naberink.cryptoexchange.commands.DepositCryptoCommand;
import nl.maarten.naberink.cryptoexchange.commands.PlaceOrderCommand;
import nl.maarten.naberink.cryptoexchange.enums.OrderType;
import nl.maarten.naberink.cryptoexchange.events.BuyOrderFilledEvent;
import nl.maarten.naberink.cryptoexchange.events.BuyOrderPlacedEvent;
import nl.maarten.naberink.cryptoexchange.events.BuyOrderUpdatedEvent;
import nl.maarten.naberink.cryptoexchange.events.CommissionEarningUpdateEvent;
import nl.maarten.naberink.cryptoexchange.events.CryptoBoughtEvent;
import nl.maarten.naberink.cryptoexchange.events.CryptoDepositedEvent;
import nl.maarten.naberink.cryptoexchange.events.OrderBookCreatedEvent;
import nl.maarten.naberink.cryptoexchange.events.SellOrderFilledEvent;
import nl.maarten.naberink.cryptoexchange.events.SellOrderPlacedEvent;
import nl.maarten.naberink.cryptoexchange.events.SellOrderUpdatedEvent;
import nl.maarten.naberink.cryptoexchange.services.ExternalExchangeService;

@Aggregate
public class OrderBookAggregate {

	@AggregateIdentifier
	private String orderBookId;

	private String pair;

	@AggregateMember
	private TreeSet<BuyOrder> buyOrders;

	@AggregateMember
	private TreeSet<SellOrder> sellOrders;

	private BigDecimal commissionBuy;

	private BigDecimal commissionSell;

	private BigDecimal cryptoBalance;

	private BigDecimal minimalAmount;

	// highest price and lowest timestamp
	private final static Comparator<Order> COMPARATOR = Comparator
			.comparing(Order::getPrice, Comparator.nullsLast(Comparator.naturalOrder()))
			.thenComparing(Order::getTimestampMillis, Comparator.reverseOrder());

	public OrderBookAggregate() {

	}

	@CommandHandler
	public OrderBookAggregate(CreateOrderBookCommand command) {
		AggregateLifecycle.apply(new OrderBookCreatedEvent(command.getId(), command.getPair(),
				command.getBuyCommission(), command.getSellCommission()));
	}

	@EventSourcingHandler
	public void handle(OrderBookCreatedEvent event) throws Exception {
		this.orderBookId = event.getId();
		this.pair = event.getPair();
		buyOrders = new TreeSet<>(COMPARATOR);
		sellOrders = new TreeSet<>(COMPARATOR);
		commissionBuy = event.getBuyCommission();
		commissionSell = event.getSellCommission();
		cryptoBalance = BigDecimal.ZERO;
		minimalAmount = new BigDecimal(20);
	}

	@CommandHandler
	public void handle(DepositCryptoCommand command) {
		AggregateLifecycle.apply(new CryptoDepositedEvent(command.getOrderBookId(), command.getAmount(), pair));
	}

	@EventSourcingHandler
	public void on(CryptoDepositedEvent event) throws Exception {
		cryptoBalance = cryptoBalance.add(event.getAmount());
	}

	@EventSourcingHandler
	public void on(CryptoBoughtEvent event) throws Exception {
		cryptoBalance = cryptoBalance.subtract(event.getAmount());
	}

	@CommandHandler
	public void handle(PlaceOrderCommand command, @Autowired ExternalExchangeService externalExchange) {
		if (command.getType() == OrderType.BUY)
			AggregateLifecycle.apply(new BuyOrderPlacedEvent(command.getOrderBookId(), command.getOrderId(),
					command.getAmountEur(), command.getPrice(), command.getPair(), command.getTimestampMillis()));
		else
			AggregateLifecycle.apply(new SellOrderPlacedEvent(command.getOrderBookId(), command.getOrderId(),
					command.getAmountCrypto(), command.getPrice(), command.getPair(), command.getTimestampMillis()));

		trade(externalExchange);

	}

	private void trade(ExternalExchangeService externalExchange) {
		// do this until there is noone bidding higher than the asking price
		boolean tradingFinished = false;
		while (!tradingFinished && !buyOrders.isEmpty() && !sellOrders.isEmpty()) {
			final BuyOrder buyOrder = buyOrders.last();
			final SellOrder sellOrder = sellOrders.first();

			if (buyOrder.getPrice() == null || sellOrder.getPrice() == null
					|| buyOrder.getPrice().compareTo(sellOrder.getPrice()) >= 0) {

				final BigDecimal orderPrice = calculateOrderPrice(buyOrder.getPrice(), sellOrder.getPrice());
				final BigDecimal amountOfCryptoInTrade = calculateAmountOfCryptoThatCanBeBoughtInOrder(buyOrder,
						sellOrder, orderPrice);
				final BigDecimal amountOfEurInTrade = amountOfCryptoInTrade.multiply(orderPrice).setScale(2,
						RoundingMode.HALF_EVEN);

				// sellorder updating
				final BigDecimal sellOrderRemainingBalanceCrypto = sellOrder.getAmountCrypto()
						.subtract(amountOfCryptoInTrade);
				final BigDecimal sellCommissionInEur = amountOfEurInTrade.multiply(commissionSell);
				final BigDecimal sellOrderEurForSeller = amountOfEurInTrade.subtract(sellCommissionInEur);
				final BigDecimal sellOrderNewAmountOfEur = sellOrder.getAmountEur().add(sellOrderEurForSeller);

				// buyorder updating
				final BigDecimal buyOrderRemainingBalanceEur = buyOrder.getAmountEur().subtract(amountOfEurInTrade);
				final BigDecimal buyCommissionInCrypto = amountOfCryptoInTrade.multiply(commissionBuy);
				final BigDecimal cryptoForBuyer = amountOfCryptoInTrade.subtract(buyCommissionInCrypto);
				final BigDecimal buyOrderNewAmountOfCrypto = buyOrder.getAmountCrypto().add(cryptoForBuyer);

				// subtract crypto from orderbookbalance
				final BigDecimal newCryptoBalance = cryptoBalance.subtract(cryptoForBuyer);
				if (newCryptoBalance.compareTo(minimalAmount) == -1) {
					// buy crypto from external exchange
					final BigDecimal amountOfCryptoToBuy = minimalAmount.subtract(newCryptoBalance).abs();
					if (externalExchange.buy(amountOfCryptoToBuy))
						AggregateLifecycle.apply(new CryptoDepositedEvent(orderBookId, amountOfCryptoToBuy, pair));
					else
						throw new IllegalStateException("external exhange down when needed");
				}
				AggregateLifecycle.apply(new CryptoBoughtEvent(orderBookId, cryptoForBuyer, pair));

				if (sellOrderRemainingBalanceCrypto.compareTo(BigDecimal.ZERO) <= 0)
					AggregateLifecycle.apply(new SellOrderFilledEvent(sellOrder.getId(), sellOrderNewAmountOfEur));
				else
					AggregateLifecycle.apply(new SellOrderUpdatedEvent(sellOrder.getId(),
							sellOrderRemainingBalanceCrypto, sellOrderNewAmountOfEur));

				if (buyOrderRemainingBalanceEur.compareTo(BigDecimal.ZERO) <= 0)
					AggregateLifecycle.apply(new BuyOrderFilledEvent(buyOrder.getId(), buyOrderNewAmountOfCrypto));
				else
					AggregateLifecycle.apply(new BuyOrderUpdatedEvent(buyOrder.getId(), buyOrderNewAmountOfCrypto,
							buyOrderRemainingBalanceEur));

				if (commissionBuy.compareTo(BigDecimal.ZERO) != 0 && commissionSell.compareTo(BigDecimal.ZERO) != 0)
					AggregateLifecycle.apply(new CommissionEarningUpdateEvent(orderBookId, buyCommissionInCrypto,
							sellCommissionInEur, pair));

			} else {
				tradingFinished = true;
			}
		}
	}

	private BigDecimal calculateOrderPrice(@Nullable final BigDecimal buyPrice, @Nullable final BigDecimal sellPrice) {
		if (buyPrice == null) {
			return sellPrice;
		} else if (sellPrice == null) {
			return buyPrice;
		} else {
			return buyPrice.add(sellPrice).divide(BigDecimal.valueOf(2));
		}
	}

	private BigDecimal calculateAmountOfCryptoThatCanBeBoughtInOrder(BuyOrder buyOrder, SellOrder sellOrder,
			BigDecimal avgPrice) {
		final BigDecimal amountOfCryptoThatCanBeBoughtByBuyer = buyOrder.getAmountEur().divide(avgPrice, 8,
				RoundingMode.HALF_EVEN);
		return amountOfCryptoThatCanBeBoughtByBuyer.min(sellOrder.getAmountCrypto());
	}

	@EventSourcingHandler
	public void on(SellOrderUpdatedEvent event) {
		final SellOrder sellOrder = sellOrders.last();
		sellOrder.setAmountCrypto(event.getNewAmountOfCrypto());
		sellOrder.setAmountEur(event.getNewAmountOfEur());
	}

	@EventSourcingHandler
	public void on(BuyOrderUpdatedEvent event) {
		final BuyOrder buyOrder = buyOrders.last();
		buyOrder.setAmountCrypto(event.getNewAmountOfCrypto());
		buyOrder.setAmountEur(event.getNewAmountOfEur());
	}

	@EventSourcingHandler
	public void on(SellOrderFilledEvent event) {
		sellOrders.remove(sellOrders.last());
	}

	@EventSourcingHandler
	public void on(BuyOrderFilledEvent event) {
		buyOrders.remove(buyOrders.last());
	}

	@EventSourcingHandler
	public void on(SellOrderPlacedEvent event) {
		sellOrders.add(new SellOrder(event.getId(), event.getAmount(), event.getPrice(), event.getTimestamp()));
	}

	@EventSourcingHandler
	public void on(BuyOrderPlacedEvent event) {
		buyOrders.add(new BuyOrder(event.getId(), event.getAmount(), event.getPrice(), event.getTimestamp()));
	}
//

//	public BigDecimal buyAmountOfCryptoForEur(final BigDecimal amountInEur, boolean dryRun) {
//		Assert.isTrue(amountInEur.compareTo(new BigDecimal(0)) < 0, "amount must be positive");
//		final BigDecimal amountInEurAvailableForBuy = amountInEur
//				.subtract(amountInEur.multiply(commissionPercentBuy).divide(new BigDecimal("100")));
////		if (totalForSale.compareTo(amountInEurAvailableForBuy) < 0)
////			throw new IllegalArgumentException("amount too high, available in eur: " + totalForSale);
//
//		// try to match price
//		final BigDecimal orderCommissionInEur = amountInEur.subtract(amountInEurAvailableForBuy);
//		BigDecimal totalOrderAmountInCrypto = new BigDecimal(0);
//		BigDecimal orderRemainingAmountInEur = amountInEurAvailableForBuy;
//		final List<SellOrder> sellOrdersToRemove = new ArrayList<>();
//		final Iterator<SellOrder> iterator = sellOrders.iterator();
//		while (orderRemainingAmountInEur.compareTo(new BigDecimal(0)) > 0 && iterator.hasNext()) {
//			final SellOrder sellOrder = iterator.next();
//			final BigDecimal amountOfSellOrderInEur = sellOrder.getPrice().multiply(sellOrder.getAmountEur());
//
//			final BigDecimal min = amountOfSellOrderInEur.min(orderRemainingAmountInEur);
//			totalOrderAmountInCrypto = totalOrderAmountInCrypto.add(sellOrder.getAmountEur());
//			sellOrder.setAmountEur(sellOrder.getAmountEur().subtract(min));
//			if (!dryRun && sellOrder.getAmountEur().compareTo(new BigDecimal(0)) <= 0) {
//				// order fullfilled
//				// remove sellorder
//				sellOrdersToRemove.add(sellOrder);
//				// trade executed event?
//			}
//			orderRemainingAmountInEur = amountOfSellOrderInEur.subtract(min);
////
////
////			orderRemainingAmountInEur
////
////			if (orderRemainingAmountInEur.compareTo(amountOfSellOrderInEur) < 0) {
////				// only deduce amount that is for sale
////				orderRemainingAmountInEur -= amountOfSellOrderInEur;
////				totalOrderAmountInCrypto += sellOrder.getAmount();
////				sellOrdersToRemove.add(sellOrder);
////			} else {
////				// more to sell in sellorder than to buy from given buyorder
////				final double currentSellOrderAmount = orderRemainingAmountInEur / sellOrder.getPrice();
////				totalOrderAmountInCrypto += currentSellOrderAmount;
////				orderRemainingAmountInEur = 0;
////				// subtract bought amount from sellOrder
////				if (!dryRun) {
////					final double sellOrderRemainingAmount = sellOrder.getAmount() - currentSellOrderAmount;
////					orderbook.updateAmountSellOrder(sellOrder, sellOrderRemainingAmount);
////				}
////			}
//		}
//// CHECK IF REBUY NECESSARY
////		if (totalOrderAmountInCrypto > exchange.getCryptoInventory()) {
////			// buy additional crypto from external exchange and refill to minimum (if
////			// possible)
////			final double orderSurplusInCrypto = totalOrderAmountInCrypto - exchange.getCryptoInventory();
////			cryptoExchangeService.refillToMinimalAndBuySurplus(orderSurplusInCrypto);
////		}
//
////		cryptoExchangeService.updateInventory(exchange, OrderType.BUY, totalOrderAmountInCrypto, amountInEur);
////		if (!dryRun) {
////			sellOrdersToRemove.forEach(orderbook::removeSellOrder);
////			updateOrderbook();
////		}
//		return totalOrderAmountInCrypto;
////		return new OrderInformation(OrderType.BUY, amountInEur - orderRemainingAmountInEur, totalOrderAmountInCrypto,
////				buyCommissionPercentage, orderCommissionInEur);
//	}
//	@CommandHandler
//	public void handle(OrderRequestCommand command) throws Exception {
//		// get current price
//		final BigDecimal amountOfCrypto = buyAmountOfCryptoForEur(command.getAmount(), false);
//		createNew(OrderInformationAggregate.class,
//				() -> new OrderInformationAggregate(orderBookId, OrderType.BUY, command.getAmount(), amountOfCrypto));
////		AggregateLifecycle.apply(new OrderRequestedEvent(command.getId().toString(), command.getType(),
////				command.getPair(), command.getAmount()));
//	}
}
