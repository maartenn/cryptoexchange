package nl.maarten.naberink.cryptoexchange.view.query.data;

import java.math.BigDecimal;

import nl.maarten.naberink.cryptoexchange.enums.OrderType;

public class OrderData {

	private final String orderId;

	private final String pair;
	private BigDecimal amountEur;
	private BigDecimal amountCrypto;

	private final OrderType orderType;
	private final BigDecimal price;

	public OrderData(String orderId, String pair, BigDecimal amountEur, BigDecimal amountCrypto, OrderType type,
			BigDecimal price) {
		this.orderId = orderId;
		this.pair = pair;
		this.amountEur = amountEur;
		this.amountCrypto = amountCrypto;
		this.orderType = type;
		this.price = price;
	}

	public String getOrderId() {
		return orderId;
	}

	public String getPair() {
		return pair;
	}

	public BigDecimal getAmountEur() {
		return amountEur;
	}

	public void setAmountEur(BigDecimal amountEur) {
		this.amountEur = amountEur;
	}

	public BigDecimal getAmountCrypto() {
		return amountCrypto;
	}

	public void setAmountCrypto(BigDecimal amountCrypto) {
		this.amountCrypto = amountCrypto;
	}

	public OrderType getOrderType() {
		return orderType;
	}

	public BigDecimal getPrice() {
		return price;
	}
}
