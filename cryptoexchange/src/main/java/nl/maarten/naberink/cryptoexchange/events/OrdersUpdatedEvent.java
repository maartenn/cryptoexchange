package nl.maarten.naberink.cryptoexchange.events;

import java.math.BigDecimal;

public class OrdersUpdatedEvent {
	private final BigDecimal amountCrypto;
	private final BigDecimal amountEur;
	private final String buyOrderId;
	private final String sellOrderId;

	public BigDecimal getAmountCrypto() {
		return amountCrypto;
	}

	public String getBuyOrderId() {
		return buyOrderId;
	}

	public String getSellOrderId() {
		return sellOrderId;
	}

	public BigDecimal getAmountEur() {
		return amountEur;
	}

	public OrdersUpdatedEvent(String buyOrderId, String sellOrderId, BigDecimal amountCrypto, BigDecimal amountEur) {
		super();
		this.buyOrderId = buyOrderId;
		this.sellOrderId = sellOrderId;
		this.amountCrypto = amountCrypto;
		this.amountEur = amountEur;
	}

}
