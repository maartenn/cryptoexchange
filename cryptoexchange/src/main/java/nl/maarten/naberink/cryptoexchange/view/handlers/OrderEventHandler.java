package nl.maarten.naberink.cryptoexchange.view.handlers;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Service;

import nl.maarten.naberink.cryptoexchange.events.BuyOrderFilledEvent;
import nl.maarten.naberink.cryptoexchange.events.BuyOrderPlacedEvent;
import nl.maarten.naberink.cryptoexchange.events.BuyOrderUpdatedEvent;
import nl.maarten.naberink.cryptoexchange.events.CommissionEarningUpdateEvent;
import nl.maarten.naberink.cryptoexchange.events.OrderBookCreatedEvent;
import nl.maarten.naberink.cryptoexchange.events.SellOrderFilledEvent;
import nl.maarten.naberink.cryptoexchange.events.SellOrderPlacedEvent;
import nl.maarten.naberink.cryptoexchange.events.SellOrderUpdatedEvent;
import nl.maarten.naberink.cryptoexchange.view.query.FindAllOrdersQuery;
import nl.maarten.naberink.cryptoexchange.view.query.FindBuyOrderByIdQuery;
import nl.maarten.naberink.cryptoexchange.view.query.FindLatestPriceDataQuery;
import nl.maarten.naberink.cryptoexchange.view.query.FindOpenBuyOrdersQuery;
import nl.maarten.naberink.cryptoexchange.view.query.FindOpenSellOrderQuery;
import nl.maarten.naberink.cryptoexchange.view.query.data.BuyOrderData;
import nl.maarten.naberink.cryptoexchange.view.query.data.OrderData;
import nl.maarten.naberink.cryptoexchange.view.query.data.PriceInfo;
import nl.maarten.naberink.cryptoexchange.view.query.data.SellOrderData;

@Service
public class OrderEventHandler {

	private final Map<String, BuyOrderData> openBuyOrdersData = new HashMap<>();
	private final Map<String, SellOrderData> openSellOrdersData = new HashMap<>();

	private final Map<String, BuyOrderData> closedBuyOrdersData = new HashMap<>();
	private final Map<String, SellOrderData> closedSellOrdersData = new HashMap<>();

	private final Map<String, BigDecimal> cryptoCommission = new HashMap<>();
	private final BigDecimal eurBalance = BigDecimal.ZERO;

	@EventHandler
	public void on(OrderBookCreatedEvent event) {
		cryptoCommission.put(event.getPair(), BigDecimal.ZERO);
	}

	@EventHandler
	public void on(BuyOrderPlacedEvent event) {
		final String orderId = event.getId();
		openBuyOrdersData.put(orderId,
				new BuyOrderData(orderId, event.getPair(), event.getAmount(), BigDecimal.ZERO, event.getPrice()));
	}

	@EventHandler
	public void on(SellOrderPlacedEvent event) {
		final String orderId = event.getId();
		openSellOrdersData.put(orderId,
				new SellOrderData(orderId, event.getPair(), BigDecimal.ZERO, event.getAmount(), event.getPrice()));
	}

	@EventHandler
	public void on(BuyOrderUpdatedEvent event) {
		final BuyOrderData buyOrderData = openBuyOrdersData.get(event.getBuyOrderId());
		buyOrderData.setAmountCrypto(event.getNewAmountOfCrypto());
		buyOrderData.setAmountEur(event.getNewAmountOfEur());
	}

	@EventHandler
	public void on(SellOrderUpdatedEvent event) {
		final SellOrderData sellOrderData = openSellOrdersData.get(event.getSellOrderId());
		if (sellOrderData == null) {
			// couldn't be found
			return;
		}
		sellOrderData.setAmountCrypto(event.getNewAmountOfCrypto());
		sellOrderData.setAmountEur(event.getNewAmountOfEur());
	}

	@EventHandler
	public void on(SellOrderFilledEvent event) {
		final SellOrderData filledOrder = openSellOrdersData.remove(event.getSellOrderId());
		closedSellOrdersData.put(event.getSellOrderId(), filledOrder);
	}

	private Optional<SellOrderData> getLowestSellPrice(final String pair) {
		return openSellOrdersData.values().stream().filter(b -> b.getPair().equals(pair))
				.max(Comparator.comparing(SellOrderData::getPrice, Comparator.nullsFirst(Comparator.naturalOrder())));
	}

	@EventHandler
	public void on(BuyOrderFilledEvent event) {
		final BuyOrderData filledOrder = openBuyOrdersData.remove(event.getBuyOrderId());
		closedBuyOrdersData.put(event.getBuyOrderId(), filledOrder);
	}

	private Optional<BuyOrderData> getMaxBuyPrice(final String pair) {
		return openBuyOrdersData.values().stream().filter(b -> b.getPair().equals(pair))
				.max(Comparator.comparing(BuyOrderData::getPrice, Comparator.nullsFirst(Comparator.naturalOrder())));
	}

	@EventHandler
	public void on(CommissionEarningUpdateEvent event) {
		eurBalance.add(event.getEuroCommission());
		cryptoCommission.get(event.getPair()).add(event.getCryptoCommission());
	}

	@QueryHandler
	public List<OrderData> handle(FindAllOrdersQuery query) {
		return Stream.of(openBuyOrdersData.values(), openSellOrdersData.values()).flatMap(x -> x.stream())
				.collect(Collectors.toList());
	}

	@QueryHandler
	public OrderData handle(FindBuyOrderByIdQuery query) {
		return openBuyOrdersData.get(query.getOrderId());
	}

	@QueryHandler
	public Collection<BuyOrderData> handle(FindOpenBuyOrdersQuery query) {
		return openBuyOrdersData.values();
	}

	@QueryHandler
	public Collection<SellOrderData> handle(FindOpenSellOrderQuery query) {
		return openSellOrdersData.values();
	}

	@QueryHandler
	public PriceInfo handle(FindLatestPriceDataQuery query) {
		return new PriceInfo(getMaxBuyPrice(query.getPair()).orElse(null),
				getLowestSellPrice(query.getPair()).orElse(null));
	}

}
