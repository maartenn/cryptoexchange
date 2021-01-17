package nl.maarten.naberink.cryptoexchange.events;

import java.math.BigDecimal;

//TODO make parent class updated event
public class BuyOrderUpdatedEvent {
	private final String buyOrderId;
	private final BigDecimal currentAmountOfCrypto;
	private final BigDecimal currentAmountOfEur;

	public String getBuyOrderId() {
		return buyOrderId;
	}

	public BigDecimal getNewAmountOfCrypto() {
		return currentAmountOfCrypto;
	}

	public BigDecimal getNewAmountOfEur() {
		return currentAmountOfEur;
	}

	public BuyOrderUpdatedEvent(String buyOrderId, BigDecimal currentAmountOfCrypto, BigDecimal currentAmountOfEur) {
		this.buyOrderId = buyOrderId;
		this.currentAmountOfCrypto = currentAmountOfCrypto;
		this.currentAmountOfEur = currentAmountOfEur;
	}
}
