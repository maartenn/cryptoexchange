package nl.maarten.naberink.cryptoexchange.events;

import java.math.BigDecimal;

public class CommissionEarningUpdateEvent {
	private final String orderBookId;
	private final BigDecimal cryptoCommission;
	private final BigDecimal euroCommission;
	private final String pair;

	public String getOrderBookId() {
		return orderBookId;
	}

	public BigDecimal getCryptoCommission() {
		return cryptoCommission;
	}

	public BigDecimal getEuroCommission() {
		return euroCommission;
	}

	public String getPair() {
		return pair;
	}

	public CommissionEarningUpdateEvent(String orderBookId, BigDecimal cryptoCommission, BigDecimal euroCommission,
			String pair) {
		this.orderBookId = orderBookId;
		this.cryptoCommission = cryptoCommission;
		this.euroCommission = euroCommission;
		this.pair = pair;
	}

}
