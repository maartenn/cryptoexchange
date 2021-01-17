package nl.maarten.naberink.cryptoexchange.view.query.orderbook;

public class OrderBookDTO {
	private final String id;
	private final String pair;

	public String getId() {
		return id;
	}

	public String getPair() {
		return pair;
	}

	public OrderBookDTO(String id, String pair) {
		this.id = id;
		this.pair = pair;
	}
}
