package nl.maarten.naberink.cryptoexchange.events;

import java.math.BigDecimal;

//TODO make parent class updated event
public class SellOrderUpdatedEvent {
	private final String sellOrderId;
	private final BigDecimal remainingAmountOfCrypto;
	private final BigDecimal currentAmountOfEur;

	public String getSellOrderId() {
		return sellOrderId;
	}

	public BigDecimal getNewAmountOfCrypto() {
		return remainingAmountOfCrypto;
	}

	public BigDecimal getNewAmountOfEur() {
		return currentAmountOfEur;
	}

	public SellOrderUpdatedEvent(String sellOrderId, BigDecimal remainingAmountOfCrypto,
			BigDecimal currentAmountOfEur) {
		this.sellOrderId = sellOrderId;
		this.remainingAmountOfCrypto = remainingAmountOfCrypto;
		this.currentAmountOfEur = currentAmountOfEur;
	}
}
