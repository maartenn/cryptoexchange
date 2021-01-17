package nl.maarten.cryptoexchange.cryptoexchange;

import java.math.BigDecimal;
import java.util.UUID;

import org.axonframework.test.aggregate.AggregateTestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nl.maarten.naberink.cryptoexchange.aggregate.OrderBookAggregate;
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

class OrderBookAggregateTest {
	private static final String PAIR = "BTC_EUR";

	private static final String ORDERBOOK_ID = "76fe4bd9-fff5-4af1-904a-ddf7542e24f1";

	private static final BigDecimal ZERO_COMMISSION = BigDecimal.ZERO;

	private static OrderBookCreatedEvent SIMPLE_ORDERBOOK_CREATED_EVENT = new OrderBookCreatedEvent(ORDERBOOK_ID, PAIR,
			ZERO_COMMISSION, ZERO_COMMISSION);
	private static CryptoDepositedEvent CRYPTO_DEPOSITED_EVENT = new CryptoDepositedEvent(ORDERBOOK_ID,
			BigDecimal.valueOf(25), PAIR);

	private static final BigDecimal BUYCOMMISSION = BigDecimal.valueOf(0.02);
	private static final BigDecimal SELLCOMMISSION = BigDecimal.valueOf(0.03);
	private static final OrderBookCreatedEvent COMMISSION_ORDERBOOK_CREATED_EVENT = new OrderBookCreatedEvent(
			ORDERBOOK_ID, PAIR, BUYCOMMISSION, SELLCOMMISSION);

	private AggregateTestFixture<OrderBookAggregate> fixture;

	@BeforeEach
	public void setUp() {
		fixture = new AggregateTestFixture<>(OrderBookAggregate.class);
		fixture.registerInjectableResource(new ExternalExchangeService());
	}

	@Test
	void testCreateOrderbookZeroCommission() {
		fixture.givenNoPriorActivity()
				.when(new CreateOrderBookCommand(ORDERBOOK_ID, PAIR, ZERO_COMMISSION, ZERO_COMMISSION))
				.expectEvents(SIMPLE_ORDERBOOK_CREATED_EVENT);
	}

	@Test
	void testCreateOrderbookWithDifferentBuySellCommission() {
		fixture.givenNoPriorActivity()
				.when(new CreateOrderBookCommand(ORDERBOOK_ID, PAIR, BUYCOMMISSION, SELLCOMMISSION))
				.expectEvents(COMMISSION_ORDERBOOK_CREATED_EVENT);
	}

	@Test
	void testDepositCrypto() {
		fixture.given(SIMPLE_ORDERBOOK_CREATED_EVENT)
				.when(new DepositCryptoCommand(ORDERBOOK_ID, BigDecimal.valueOf(25)))
				.expectEvents(CRYPTO_DEPOSITED_EVENT);
	}

	@Test

	void testPlacingBuyOrder() {
		final BigDecimal price = BigDecimal.valueOf(50_000);
		final BigDecimal amountEurToBuyFor = BigDecimal.TEN;
		final String orderId = UUID.randomUUID().toString();
		final long timestampBuyOrder = 1610799755000L;
		fixture.given(SIMPLE_ORDERBOOK_CREATED_EVENT, CRYPTO_DEPOSITED_EVENT)
				.when(new PlaceOrderCommand(ORDERBOOK_ID, orderId, OrderType.BUY, BigDecimal.ZERO, amountEurToBuyFor,
						price, PAIR, timestampBuyOrder))
				.expectEvents(new BuyOrderPlacedEvent(ORDERBOOK_ID, orderId, amountEurToBuyFor, price, PAIR,
						timestampBuyOrder));
	}

	@Test
	void testPlacingSellOrder() {
		final BigDecimal price = BigDecimal.valueOf(60_000);
		final BigDecimal amountOfCryptoToSell = BigDecimal.ONE;
		final String orderId = UUID.randomUUID().toString();
		final long timestampSellOrder = 1610799755000L;

		fixture.given(SIMPLE_ORDERBOOK_CREATED_EVENT, CRYPTO_DEPOSITED_EVENT)
				.when(new PlaceOrderCommand(ORDERBOOK_ID, orderId, OrderType.SELL, amountOfCryptoToSell,
						BigDecimal.ZERO, price, PAIR, timestampSellOrder))
				.expectEvents(new SellOrderPlacedEvent(ORDERBOOK_ID, orderId, amountOfCryptoToSell, price, PAIR,
						timestampSellOrder));
	}

	@Test
	void testTradeExecutingOnSellOrderCommand() {
		final String buyOrderId = UUID.randomUUID().toString();
		final BigDecimal eurToSpend = BigDecimal.valueOf(500.00).setScale(2);
		final BigDecimal priceCrypto = BigDecimal.valueOf(50_000.00000000);
		final long timestampBuyOrder = 1610799755000L;
		final BuyOrderPlacedEvent buyOrderPlacedEvent = new BuyOrderPlacedEvent(ORDERBOOK_ID, buyOrderId, eurToSpend,
				priceCrypto, PAIR, timestampBuyOrder);

		final String sellOrderId = UUID.randomUUID().toString();
		final long timestampSellOrder = 1610799755001L;

		final BigDecimal amountOfCryptoToSell = BigDecimal.valueOf(0.01).setScale(8);
		fixture.given(SIMPLE_ORDERBOOK_CREATED_EVENT, CRYPTO_DEPOSITED_EVENT, buyOrderPlacedEvent)
				.when(new PlaceOrderCommand(ORDERBOOK_ID, sellOrderId, OrderType.SELL, amountOfCryptoToSell,
						BigDecimal.ZERO, priceCrypto, PAIR, timestampSellOrder))
				.expectEvents(
						new SellOrderPlacedEvent(ORDERBOOK_ID, sellOrderId, amountOfCryptoToSell, priceCrypto, PAIR,
								timestampSellOrder),
						new CryptoBoughtEvent(ORDERBOOK_ID, amountOfCryptoToSell, PAIR),
						new SellOrderFilledEvent(sellOrderId, eurToSpend),
						new BuyOrderFilledEvent(buyOrderId, amountOfCryptoToSell));
	}

	@Test
	void testTradeExecutingOnBuyOrderCommand() {
		final String sellOrderId = UUID.randomUUID().toString();
		final BigDecimal amountOfCryptoToSell = BigDecimal.valueOf(0.01).setScale(8);
		final BigDecimal priceCrypto = BigDecimal.valueOf(50_000.00000000);
		final long timestampSellOrder = 1610799755000L;

		final SellOrderPlacedEvent sellOrderPlacedEvent = new SellOrderPlacedEvent(ORDERBOOK_ID, sellOrderId,
				amountOfCryptoToSell, priceCrypto, PAIR, timestampSellOrder);

		final String buyOrderId = UUID.randomUUID().toString();
		final long timestampBuyOrder = 1610799755010L;

		final BigDecimal amountOfEurToBuy = BigDecimal.valueOf(500.00).setScale(2);
		fixture.given(SIMPLE_ORDERBOOK_CREATED_EVENT, CRYPTO_DEPOSITED_EVENT, sellOrderPlacedEvent)
				.when(new PlaceOrderCommand(ORDERBOOK_ID, buyOrderId, OrderType.BUY, BigDecimal.ZERO, amountOfEurToBuy,
						priceCrypto, PAIR, timestampBuyOrder))
				.expectEvents(
						new BuyOrderPlacedEvent(ORDERBOOK_ID, buyOrderId, amountOfEurToBuy, priceCrypto, PAIR,
								timestampBuyOrder),
						new CryptoBoughtEvent(ORDERBOOK_ID, amountOfCryptoToSell, PAIR),
						new SellOrderFilledEvent(sellOrderId, amountOfEurToBuy),
						new BuyOrderFilledEvent(buyOrderId, amountOfCryptoToSell.setScale(8)));
	}

	@Test
	void testBuyOrderBeFilledPartially() {
		final String buyOrderId = UUID.randomUUID().toString();
		final BigDecimal eurToSpend = BigDecimal.valueOf(5_000.00).setScale(2);
		final BigDecimal priceCrypto = BigDecimal.valueOf(50_000.00000000);
		final long timestampBuyOrder = 1610799755000L;

		final BuyOrderPlacedEvent buyOrderPlacedEvent = new BuyOrderPlacedEvent(ORDERBOOK_ID, buyOrderId, eurToSpend,
				priceCrypto, PAIR, timestampBuyOrder);

		final String sellOrderId = UUID.randomUUID().toString();
		final long timestampSellOrder = 1610799755010L;

		final BigDecimal amountOfCryptoToSell = BigDecimal.valueOf(0.01).setScale(8);
		fixture.given(SIMPLE_ORDERBOOK_CREATED_EVENT, CRYPTO_DEPOSITED_EVENT, buyOrderPlacedEvent)
				.when(new PlaceOrderCommand(ORDERBOOK_ID, sellOrderId, OrderType.SELL, amountOfCryptoToSell,
						BigDecimal.ZERO, priceCrypto, PAIR, timestampSellOrder))
				.expectEvents(
						new SellOrderPlacedEvent(ORDERBOOK_ID, sellOrderId, amountOfCryptoToSell, priceCrypto, PAIR,
								timestampSellOrder),
						new CryptoBoughtEvent(ORDERBOOK_ID, amountOfCryptoToSell, PAIR),
						new SellOrderFilledEvent(sellOrderId, BigDecimal.valueOf(500).setScale(2)),
						new BuyOrderUpdatedEvent(buyOrderId, amountOfCryptoToSell,
								BigDecimal.valueOf(4500).setScale(2)));
	}

	@Test
	void testSellOrderBeFilledPartially() {
		final String sellOrderId = UUID.randomUUID().toString();
		final BigDecimal amountOfCryptoToSell = BigDecimal.valueOf(0.50).setScale(8);
		final BigDecimal priceCrypto = BigDecimal.valueOf(50_000.00000000);
		final long timestampSellOrder = 1610799755010L;

		final SellOrderPlacedEvent sellOrderPlacedEvent = new SellOrderPlacedEvent(ORDERBOOK_ID, sellOrderId,
				amountOfCryptoToSell, priceCrypto, PAIR, timestampSellOrder);

		final String buyOrderId = UUID.randomUUID().toString();
		final long timestampBuyOrder = 1610799755011L;

		final BigDecimal amountOfEurToBuy = BigDecimal.valueOf(1_000.00).setScale(2);

		fixture.given(SIMPLE_ORDERBOOK_CREATED_EVENT, CRYPTO_DEPOSITED_EVENT, sellOrderPlacedEvent)
				.when(new PlaceOrderCommand(ORDERBOOK_ID, buyOrderId, OrderType.BUY, BigDecimal.ZERO, amountOfEurToBuy,
						priceCrypto, PAIR, timestampBuyOrder))
				.expectEvents(
						new BuyOrderPlacedEvent(ORDERBOOK_ID, buyOrderId, amountOfEurToBuy, priceCrypto, PAIR,
								timestampBuyOrder),
						new CryptoBoughtEvent(ORDERBOOK_ID, BigDecimal.valueOf(0.02).setScale(8), PAIR),
						new SellOrderUpdatedEvent(sellOrderId, BigDecimal.valueOf(0.48).setScale(8), amountOfEurToBuy),
						new BuyOrderFilledEvent(buyOrderId, BigDecimal.valueOf(0.02).setScale(8)));
	}

	@Test
	void testAvgPriceWhenBuyPriceHigherThanSellPrice() {
		final String buyOrderId = UUID.randomUUID().toString();
		final BigDecimal eurToSpend = BigDecimal.valueOf(120_000).setScale(2);
		final BigDecimal buyPriceCrypto = BigDecimal.valueOf(50_000.00000000);
		final long timestampBuyOrder = 1610799755000L;
		final BuyOrderPlacedEvent buyOrderPlacedEvent = new BuyOrderPlacedEvent(ORDERBOOK_ID, buyOrderId, eurToSpend,
				buyPriceCrypto, PAIR, timestampBuyOrder);

		final String sellOrderId = UUID.randomUUID().toString();
		final BigDecimal sellPriceCrypto = BigDecimal.valueOf(30_000.00000000);
		final BigDecimal amountOfCryptoToSell = BigDecimal.valueOf(3).setScale(8);
		final long timestampSellOrder = 1610799755010L;

		fixture.given(SIMPLE_ORDERBOOK_CREATED_EVENT, CRYPTO_DEPOSITED_EVENT, buyOrderPlacedEvent)
				.when(new PlaceOrderCommand(ORDERBOOK_ID, sellOrderId, OrderType.SELL, amountOfCryptoToSell,
						BigDecimal.ZERO, sellPriceCrypto, PAIR, timestampSellOrder))
				.expectEvents(
						new SellOrderPlacedEvent(ORDERBOOK_ID, sellOrderId, amountOfCryptoToSell, sellPriceCrypto, PAIR,
								timestampSellOrder),
						new CryptoBoughtEvent(ORDERBOOK_ID, amountOfCryptoToSell, PAIR),
						new SellOrderFilledEvent(sellOrderId, eurToSpend),
						new BuyOrderFilledEvent(buyOrderId, BigDecimal.valueOf(3).setScale(8)));
	}

	@Test
	void testHighestBuyOrdersFilledFirst() {
		final BigDecimal lowOrderEurToSpend = BigDecimal.valueOf(1_000.00).setScale(2);
		final BigDecimal highOrderEurToSpend = BigDecimal.valueOf(5_000.00).setScale(2);
		final BigDecimal lowPriceCrypto = BigDecimal.valueOf(30_000.00000000);
		final BigDecimal highPriceCrypto = BigDecimal.valueOf(50_000.00000000);

		// low price 1

		final String buyOrderId1 = UUID.randomUUID().toString();
		final BuyOrderPlacedEvent buyOrderPlacedEvent1 = new BuyOrderPlacedEvent(ORDERBOOK_ID, buyOrderId1,
				lowOrderEurToSpend, lowPriceCrypto, PAIR, 1610799755001L);
		// high price
		final String buyOrderId2 = UUID.randomUUID().toString();
		final BuyOrderPlacedEvent buyOrderPlacedEvent2 = new BuyOrderPlacedEvent(ORDERBOOK_ID, buyOrderId2,
				highOrderEurToSpend, highPriceCrypto, PAIR, 1610799755002L);
		// low price 2
		final String buyOrderId3 = UUID.randomUUID().toString();
		final BuyOrderPlacedEvent buyOrderPlacedEvent3 = new BuyOrderPlacedEvent(ORDERBOOK_ID, buyOrderId3,
				lowOrderEurToSpend, lowPriceCrypto, PAIR, 1610799755003L);

		final String sellOrderId = UUID.randomUUID().toString();
		final BigDecimal amountOfCryptoToSell = BigDecimal.valueOf(0.1).setScale(8);
		final long timestampSellOrder = 1610799755101L;
		fixture.given(SIMPLE_ORDERBOOK_CREATED_EVENT, CRYPTO_DEPOSITED_EVENT, buyOrderPlacedEvent1,
				buyOrderPlacedEvent2, buyOrderPlacedEvent3)
				.when(new PlaceOrderCommand(ORDERBOOK_ID, sellOrderId, OrderType.SELL, amountOfCryptoToSell,
						BigDecimal.ZERO, highPriceCrypto, PAIR, timestampSellOrder))
				.expectEvents(
						new SellOrderPlacedEvent(ORDERBOOK_ID, sellOrderId, amountOfCryptoToSell, highPriceCrypto, PAIR,
								timestampSellOrder),
						new CryptoBoughtEvent(ORDERBOOK_ID, amountOfCryptoToSell, PAIR),
						new SellOrderFilledEvent(sellOrderId, highOrderEurToSpend),
						new BuyOrderFilledEvent(buyOrderId2, amountOfCryptoToSell));
	}

	@Test
	void testFillMultipleBuyOrdersFifoWithBigSellOrder() {
		final BigDecimal orderEurToSpend = BigDecimal.valueOf(5_000.00).setScale(2);
		final BigDecimal priceCrypto = BigDecimal.valueOf(50_000.00000000);

		final String buyOrderId1 = UUID.randomUUID().toString();
		final BuyOrderPlacedEvent buyOrderPlacedEvent1 = new BuyOrderPlacedEvent(ORDERBOOK_ID, buyOrderId1,
				orderEurToSpend, priceCrypto, PAIR, 1610799755001L);
		final String buyOrderId2 = UUID.randomUUID().toString();
		final BuyOrderPlacedEvent buyOrderPlacedEvent2 = new BuyOrderPlacedEvent(ORDERBOOK_ID, buyOrderId2,
				orderEurToSpend, priceCrypto, PAIR, 1610799755002L);
		final String buyOrderId3 = UUID.randomUUID().toString();
		final BuyOrderPlacedEvent buyOrderPlacedEvent3 = new BuyOrderPlacedEvent(ORDERBOOK_ID, buyOrderId3,
				orderEurToSpend, priceCrypto, PAIR, 1610799755003L);

		final String sellOrderId = UUID.randomUUID().toString();
		final BigDecimal amountOfCryptoToSell = BigDecimal.valueOf(0.25).setScale(8);
		final long timestampSellOrder = 1610799755010L;

		fixture.given(SIMPLE_ORDERBOOK_CREATED_EVENT, CRYPTO_DEPOSITED_EVENT, buyOrderPlacedEvent1,
				buyOrderPlacedEvent2, buyOrderPlacedEvent3)
				.when(new PlaceOrderCommand(ORDERBOOK_ID, sellOrderId, OrderType.SELL, amountOfCryptoToSell,
						BigDecimal.ZERO, priceCrypto, PAIR, timestampSellOrder))
				.expectEvents(
						new SellOrderPlacedEvent(ORDERBOOK_ID, sellOrderId, amountOfCryptoToSell, priceCrypto, PAIR,
								timestampSellOrder),
						new CryptoBoughtEvent(ORDERBOOK_ID, BigDecimal.valueOf(0.1).setScale(8), PAIR),
						new SellOrderUpdatedEvent(sellOrderId, BigDecimal.valueOf(0.15).setScale(8), orderEurToSpend),
						new BuyOrderFilledEvent(buyOrderId1, BigDecimal.valueOf(0.1).setScale(8)),
						new CryptoBoughtEvent(ORDERBOOK_ID, BigDecimal.valueOf(0.1).setScale(8), PAIR),
						new SellOrderUpdatedEvent(sellOrderId, BigDecimal.valueOf(0.05).setScale(8),
								BigDecimal.valueOf(10_000).setScale(2)),
						new BuyOrderFilledEvent(buyOrderId2, BigDecimal.valueOf(0.1).setScale(8)),
						new CryptoBoughtEvent(ORDERBOOK_ID, BigDecimal.valueOf(0.05).setScale(8), PAIR),
						new SellOrderFilledEvent(sellOrderId, BigDecimal.valueOf(12_500).setScale(2)),
						new BuyOrderUpdatedEvent(buyOrderId3, BigDecimal.valueOf(0.05).setScale(8),
								BigDecimal.valueOf(2_500).setScale(2)));

	}

	@Test
	void testComission() {
		final String buyOrderId = UUID.randomUUID().toString();
		final BigDecimal eurToSpend = BigDecimal.valueOf(500.00).setScale(2);
		final BigDecimal priceCrypto = BigDecimal.valueOf(50_000.00000000);
		final long timestampBuyOrder = 1610799755000L;
		final BuyOrderPlacedEvent buyOrderPlacedEvent = new BuyOrderPlacedEvent(ORDERBOOK_ID, buyOrderId, eurToSpend,
				priceCrypto, PAIR, timestampBuyOrder);

		final String sellOrderId = UUID.randomUUID().toString();
		final long timestampSellOrder = 1610799755001L;

		final BigDecimal amountOfCryptoToSell = BigDecimal.valueOf(0.01).setScale(8);
		final BigDecimal buyCommission = amountOfCryptoToSell.multiply(BUYCOMMISSION);
		final BigDecimal sellCommission = eurToSpend.multiply(SELLCOMMISSION);
		fixture.given(COMMISSION_ORDERBOOK_CREATED_EVENT, CRYPTO_DEPOSITED_EVENT, buyOrderPlacedEvent)
				.when(new PlaceOrderCommand(ORDERBOOK_ID, sellOrderId, OrderType.SELL, amountOfCryptoToSell,
						BigDecimal.ZERO, priceCrypto, PAIR, timestampSellOrder))
				.expectEvents(
						new SellOrderPlacedEvent(ORDERBOOK_ID, sellOrderId, amountOfCryptoToSell, priceCrypto, PAIR,
								timestampSellOrder),
						new CryptoBoughtEvent(ORDERBOOK_ID, amountOfCryptoToSell.subtract(buyCommission), PAIR),
						new SellOrderFilledEvent(sellOrderId, eurToSpend.subtract(sellCommission)),
						new BuyOrderFilledEvent(buyOrderId, amountOfCryptoToSell.subtract(buyCommission)),
						new CommissionEarningUpdateEvent(ORDERBOOK_ID, buyCommission, sellCommission, PAIR));
	}

	@Test
	void testNoBuyPriceThenUseSellPrice() {
		final String buyOrderId = UUID.randomUUID().toString();
		final BigDecimal eurToSpend = BigDecimal.valueOf(500.00).setScale(2);
		final BigDecimal priceCrypto = BigDecimal.valueOf(50_000.00000000);
		final long timestampBuyOrder = 1610799755000L;
		final BuyOrderPlacedEvent buyOrderPlacedEvent = new BuyOrderPlacedEvent(ORDERBOOK_ID, buyOrderId, eurToSpend,
				null, PAIR, timestampBuyOrder);

		final String sellOrderId = UUID.randomUUID().toString();
		final long timestampSellOrder = 1610799755001L;

		final BigDecimal amountOfCryptoToSell = BigDecimal.valueOf(0.01).setScale(8);
		fixture.given(SIMPLE_ORDERBOOK_CREATED_EVENT, CRYPTO_DEPOSITED_EVENT, buyOrderPlacedEvent)
				.when(new PlaceOrderCommand(ORDERBOOK_ID, sellOrderId, OrderType.SELL, amountOfCryptoToSell,
						BigDecimal.ZERO, priceCrypto, PAIR, timestampSellOrder))
				.expectEvents(
						new SellOrderPlacedEvent(ORDERBOOK_ID, sellOrderId, amountOfCryptoToSell, priceCrypto, PAIR,
								timestampSellOrder),
						new CryptoBoughtEvent(ORDERBOOK_ID, amountOfCryptoToSell, PAIR),
						new SellOrderFilledEvent(sellOrderId, eurToSpend),
						new BuyOrderFilledEvent(buyOrderId, amountOfCryptoToSell));

	}

	@Test
	void testNoSellPriceThenUseBuyPrice() {
		final String buyOrderId = UUID.randomUUID().toString();
		final BigDecimal eurToSpend = BigDecimal.valueOf(500.00).setScale(2);
		final BigDecimal priceCrypto = BigDecimal.valueOf(50_000.00000000);
		final long timestampBuyOrder = 1610799755000L;
		final BuyOrderPlacedEvent buyOrderPlacedEvent = new BuyOrderPlacedEvent(ORDERBOOK_ID, buyOrderId, eurToSpend,
				priceCrypto, PAIR, timestampBuyOrder);

		final String sellOrderId = UUID.randomUUID().toString();
		final long timestampSellOrder = 1610799755001L;

		final BigDecimal amountOfCryptoSold = BigDecimal.valueOf(0.01).setScale(8);
		fixture.given(SIMPLE_ORDERBOOK_CREATED_EVENT, CRYPTO_DEPOSITED_EVENT, buyOrderPlacedEvent)
				.when(new PlaceOrderCommand(ORDERBOOK_ID, sellOrderId, OrderType.SELL, amountOfCryptoSold,
						BigDecimal.ZERO, null, PAIR, timestampSellOrder))
				.expectEvents(
						new SellOrderPlacedEvent(ORDERBOOK_ID, sellOrderId, amountOfCryptoSold, null, PAIR,
								timestampSellOrder),
						new CryptoBoughtEvent(ORDERBOOK_ID, amountOfCryptoSold, PAIR),
						new SellOrderFilledEvent(sellOrderId, eurToSpend),
						new BuyOrderFilledEvent(buyOrderId, amountOfCryptoSold));

	}

	@Test
	void testBuyCryptoFromExternalExchangeWhenBalanceTooLow() {
		final String buyOrderId = UUID.randomUUID().toString();
		final BigDecimal eurToSpend = BigDecimal.valueOf(1_100_000.00).setScale(2);
		final BigDecimal priceCrypto = BigDecimal.valueOf(50_000.00000000);
		final long timestampBuyOrder = 1610799755000L;
		final BuyOrderPlacedEvent buyOrderPlacedEvent = new BuyOrderPlacedEvent(ORDERBOOK_ID, buyOrderId, eurToSpend,
				priceCrypto, PAIR, timestampBuyOrder);

		final String sellOrderId = UUID.randomUUID().toString();
		final long timestampSellOrder = 1610799755001L;

		final BigDecimal amountOfCryptoSold = BigDecimal.valueOf(22).setScale(8);
		fixture.given(SIMPLE_ORDERBOOK_CREATED_EVENT, CRYPTO_DEPOSITED_EVENT, buyOrderPlacedEvent)
				.when(new PlaceOrderCommand(ORDERBOOK_ID, sellOrderId, OrderType.SELL, amountOfCryptoSold,
						BigDecimal.ZERO, null, PAIR, timestampSellOrder))
				.expectEvents(
						new SellOrderPlacedEvent(ORDERBOOK_ID, sellOrderId, amountOfCryptoSold, null, PAIR,
								timestampSellOrder),
						new CryptoDepositedEvent(ORDERBOOK_ID, BigDecimal.valueOf(17).setScale(8), PAIR),
						new CryptoBoughtEvent(ORDERBOOK_ID, amountOfCryptoSold, PAIR),
						new SellOrderFilledEvent(sellOrderId, eurToSpend),
						new BuyOrderFilledEvent(buyOrderId, amountOfCryptoSold));
	}

	// add more tests for :
	// * update balance when buying more than in liquidity
	// *
}
