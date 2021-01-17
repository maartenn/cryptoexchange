package nl.maarten.naberink.cryptoexchange.view.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Service;

import nl.maarten.naberink.cryptoexchange.events.OrderBookCreatedEvent;
import nl.maarten.naberink.cryptoexchange.view.query.orderbook.FindAllOrderBooksQuery;
import nl.maarten.naberink.cryptoexchange.view.query.orderbook.FindOrderBookByCoinPairQuery;
import nl.maarten.naberink.cryptoexchange.view.query.orderbook.OrderBookDTO;

@Service
public class OrderBookEventHandler {

	private final Map<String, String> orderBooks = new HashMap<>();

	@EventHandler
	public void handle(OrderBookCreatedEvent event) throws Exception {
		orderBooks.put(event.getPair(), event.getId());
	}

	@QueryHandler
	public List<OrderBookDTO> handle(FindAllOrderBooksQuery query) {
		final List<OrderBookDTO> list = new ArrayList<>();
		orderBooks.forEach((k, v) -> list.add(new OrderBookDTO(v, k)));
		return list;
	}

	@QueryHandler
	public String handle(FindOrderBookByCoinPairQuery query) {
		return orderBooks.get(query.getPair());
	}
}
