package nl.maarten.naberink.cryptoexchange.view.query.data;

import java.math.BigDecimal;

import nl.maarten.naberink.cryptoexchange.enums.OrderType;

public class BuyOrderData extends OrderData {

	public BuyOrderData(String orderId, String pair, BigDecimal amountEur, BigDecimal amountCrypto, BigDecimal price) {
		super(orderId, pair, amountEur, amountCrypto, OrderType.BUY, price);
	}
}
