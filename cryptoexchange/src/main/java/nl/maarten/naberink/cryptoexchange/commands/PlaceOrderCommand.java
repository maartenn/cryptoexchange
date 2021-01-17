package nl.maarten.naberink.cryptoexchange.commands;

import java.math.BigDecimal;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import nl.maarten.naberink.cryptoexchange.enums.OrderType;

public class PlaceOrderCommand {

	@TargetAggregateIdentifier
	private String orderBookId;
	private OrderType type;
	private BigDecimal amountEur;
	private BigDecimal amountCrypto;
	private BigDecimal commissionPercentage;
	private BigDecimal commissionAmountInEur;
	private String message;
	private BigDecimal price;
	private String pair;
	private String orderId;
	private long timestampMillis;

	public String getOrderBookId() {
		return orderBookId;
	}

	public String getOrderId() {
		return orderId;
	}

	public OrderType getType() {
		return type;
	}

	public BigDecimal getAmountEur() {
		return amountEur;
	}

	public BigDecimal getAmountCrypto() {
		return amountCrypto;
	}

	public BigDecimal getCommissionPercentage() {
		return commissionPercentage;
	}

	public BigDecimal getCommissionAmountInEur() {
		return commissionAmountInEur;
	}

	public String getMessage() {
		return message;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public String getPair() {
		return pair;
	}

	public long getTimestampMillis() {
		return timestampMillis;
	}

	public PlaceOrderCommand(String orderBookId, String orderId, OrderType type, BigDecimal amountCrypto,
			BigDecimal amountEur, BigDecimal price, String pair) {
		this(orderBookId, orderId, type, amountCrypto, amountEur, price, pair, System.currentTimeMillis());
	}

	public PlaceOrderCommand(String orderBookId, String orderId, OrderType type, BigDecimal amountCrypto,
			BigDecimal amountEur, BigDecimal price, String pair, long timestamp) {
		this.orderBookId = orderBookId;
		this.orderId = orderId;
		this.type = type;
		this.amountEur = amountEur;
		this.amountCrypto = amountCrypto;
		this.price = price;
		this.pair = pair;
		this.timestampMillis = timestamp;
	}

	public PlaceOrderCommand() {
	}

}
