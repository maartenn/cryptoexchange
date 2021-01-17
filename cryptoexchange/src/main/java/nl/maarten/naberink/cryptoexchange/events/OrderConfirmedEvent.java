package nl.maarten.naberink.cryptoexchange.events;

import java.math.BigDecimal;

import nl.maarten.naberink.cryptoexchange.enums.OrderType;

public class OrderConfirmedEvent {

	private String orderBookId;
	private OrderType type;
	private String pair;
	private BigDecimal amountEur;
	private BigDecimal amountCrypto;
	private BigDecimal commissionPercentage;
	private BigDecimal commissionAmountInEur;
	private BigDecimal price;
	private String message;

	public String getOrderBookId() {
		return orderBookId;
	}

	public void setOrderBookId(String id) {
		this.orderBookId = id;
	}

	public OrderType getType() {
		return type;
	}

	public void setType(OrderType type) {
		this.type = type;
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

	public BigDecimal getCommissionPercentage() {
		return commissionPercentage;
	}

	public void setCommissionPercentage(BigDecimal commissionPercentage) {
		this.commissionPercentage = commissionPercentage;
	}

	public BigDecimal getCommissionAmountInEur() {
		return commissionAmountInEur;
	}

	public void setCommissionAmountInEur(BigDecimal commissionAmountInEur) {
		this.commissionAmountInEur = commissionAmountInEur;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public String getPair() {
		return pair;
	}

	public void setPair(String pair) {
		this.pair = pair;
	}

	public OrderConfirmedEvent(String id, OrderType type, BigDecimal amountEur, BigDecimal amountCrypto,
			BigDecimal commissionPercentage, BigDecimal commissionAmountInEur, String message, BigDecimal price,
			String pair) {
		super();
		this.orderBookId = id;
		this.type = type;
		this.amountEur = amountEur;
		this.amountCrypto = amountCrypto;
		this.commissionPercentage = commissionPercentage;
		this.commissionAmountInEur = commissionAmountInEur;
		this.message = message;
		this.price = price;
		this.pair = pair;
	}

	public OrderConfirmedEvent() {
	}

}
