package nl.maarten.naberink.cryptoexchange.aggregate.data;

import java.math.BigDecimal;

import nl.maarten.naberink.cryptoexchange.enums.OrderType;

public class BuyOrder extends Order {

	public BuyOrder(String orderId, BigDecimal amount, BigDecimal price, long timestamp) {
		super(orderId, amount, BigDecimal.ZERO, price, OrderType.BUY, timestamp);
	}

}
