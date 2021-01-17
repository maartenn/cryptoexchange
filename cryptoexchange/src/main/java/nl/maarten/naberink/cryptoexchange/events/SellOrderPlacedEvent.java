package nl.maarten.naberink.cryptoexchange.events;

import java.math.BigDecimal;

public class SellOrderPlacedEvent extends OrderPlacedEvent {

	public SellOrderPlacedEvent(String orderBookId, String orderId, BigDecimal amount, BigDecimal price, String pair,
			long timestamp) {
		super(orderBookId, orderId, amount, price, pair, timestamp);
	}

}
