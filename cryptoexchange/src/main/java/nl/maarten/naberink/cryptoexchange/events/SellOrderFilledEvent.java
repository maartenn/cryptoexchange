package nl.maarten.naberink.cryptoexchange.events;

import java.math.BigDecimal;

public class SellOrderFilledEvent {

	private final String sellOrderId;
	private final BigDecimal totalAmountOfEurReceived;

	public String getSellOrderId() {
		return sellOrderId;
	}

	public BigDecimal getAmountOfEurReceived() {
		return totalAmountOfEurReceived;
	}

	public SellOrderFilledEvent(String sellOrderId, BigDecimal amountOfEurReceived) {
		this.sellOrderId = sellOrderId;
		this.totalAmountOfEurReceived = amountOfEurReceived;
	}

}
