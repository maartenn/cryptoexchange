package nl.maarten.naberink.cryptoexchange.aggregate.data;

import java.math.BigDecimal;

import nl.maarten.naberink.cryptoexchange.enums.OrderType;

public abstract class Order {

	private final String id;
	private BigDecimal amountEuro;
	private final BigDecimal price;
	private BigDecimal amountCrypto;
	private final OrderType type;
	private final long timestampMillis;

	public Order(String orderId, BigDecimal amountEuro, BigDecimal amountCrypto, BigDecimal price, OrderType type,
			long timestamp) {
		this.id = orderId;
		this.amountEuro = amountEuro;
		this.amountCrypto = amountCrypto;
		this.price = price;
		this.type = type;
		this.timestampMillis = timestamp;
	}

	public String getId() {
		return id;
	}

	public BigDecimal getAmountEur() {
		return amountEuro;
	}

	public void setAmountEur(BigDecimal amountEuro) {
		this.amountEuro = amountEuro;
	}

	public BigDecimal getAmountCrypto() {
		return amountCrypto;
	}

	public void setAmountCrypto(BigDecimal amountCrypto) {
		this.amountCrypto = amountCrypto;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public OrderType getType() {
		return type;
	}

	public long getTimestampMillis() {
		return timestampMillis;
	}

	@Override
	public String toString() {
		return String.format("id: %s timestamp : %d Type: %s Amount: %f Price: %f", id, timestampMillis,
				type.toString(), amountEuro, amountCrypto);
	}
}
