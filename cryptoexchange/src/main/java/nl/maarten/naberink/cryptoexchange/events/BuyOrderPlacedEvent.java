package nl.maarten.naberink.cryptoexchange.events;

import java.math.BigDecimal;

public class BuyOrderPlacedEvent extends OrderPlacedEvent {

	public BuyOrderPlacedEvent(String orderBookId, String orderId, BigDecimal amountOfEurToSpend, BigDecimal price,
			String pair, long timestamp) {
		super(orderBookId, orderId, amountOfEurToSpend, price, pair, timestamp);
	}

}
