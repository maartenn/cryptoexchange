package nl.maarten.naberink.cryptoexchange.events;

import java.math.BigDecimal;

public class OrderPlacedEvent {
	private String id;

	private final BigDecimal amount;

	private final BigDecimal price;

	private final String orderbookId;

	private final String pair;

	private final long timestamp;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public String getOrderbookId() {
		return orderbookId;
	}

	public String getPair() {
		return pair;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public OrderPlacedEvent(String orderBookId, String orderId, BigDecimal amount, BigDecimal price, String pair,
			long timestamp) {
		this.id = orderId;
		this.orderbookId = orderBookId;
		this.amount = amount;
		this.price = price;
		this.pair = pair;
		this.timestamp = timestamp;
	}

}
