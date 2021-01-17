package nl.maarten.naberink.cryptoexchange.events;

import java.math.BigDecimal;

public class BuyOrderFilledEvent {

	private final String buyOrderId;
	private final BigDecimal amountOfCryptoBought;

	public String getBuyOrderId() {
		return buyOrderId;
	}

	public BigDecimal getAmountOfCryptoBought() {
		return amountOfCryptoBought;
	}

	public BuyOrderFilledEvent(String buyOrderId, BigDecimal amountOfCryptoBought) {
		this.buyOrderId = buyOrderId;
		this.amountOfCryptoBought = amountOfCryptoBought;
	}

}
