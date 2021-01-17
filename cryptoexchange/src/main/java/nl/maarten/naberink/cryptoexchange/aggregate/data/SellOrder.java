package nl.maarten.naberink.cryptoexchange.aggregate.data;

import java.math.BigDecimal;

import nl.maarten.naberink.cryptoexchange.enums.OrderType;

public class SellOrder extends Order {

	public SellOrder(String orderId, BigDecimal amount, BigDecimal price, long timestamp) {
		super(orderId, BigDecimal.ZERO, amount, price, OrderType.SELL, timestamp);
	}
}
