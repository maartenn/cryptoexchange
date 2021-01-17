package nl.maarten.naberink.cryptoexchange.events;

import java.math.BigDecimal;

import org.springframework.lang.NonNull;

public class CryptoDepositedEvent {
	private final String orderBookId;
	private final BigDecimal amount;
	private final String pair;

	public String getOrderBookId() {
		return orderBookId;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public String getPair() {
		return pair;
	}

	public CryptoDepositedEvent(@NonNull String orderBookId, @NonNull BigDecimal amount, String pair) {
		this.orderBookId = orderBookId;
		this.amount = amount;
		this.pair = pair;
	}

}
