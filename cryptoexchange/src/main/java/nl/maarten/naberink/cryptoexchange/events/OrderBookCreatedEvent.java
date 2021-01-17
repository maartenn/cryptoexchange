package nl.maarten.naberink.cryptoexchange.events;

import java.math.BigDecimal;

public class OrderBookCreatedEvent {
	private final String id;
	private final String pair;
	private final BigDecimal buyCommission;
	private final BigDecimal sellCommission;

	public String getId() {
		return id;
	}

	public String getPair() {
		return pair;
	}

	public BigDecimal getBuyCommission() {
		return buyCommission;
	}

	public BigDecimal getSellCommission() {
		return sellCommission;
	}

	public OrderBookCreatedEvent(String id, String pair, BigDecimal buyCommission, BigDecimal sellCommission) {
		this.id = id;
		this.pair = pair;
		this.buyCommission = buyCommission;
		this.sellCommission = sellCommission;
	}

}
