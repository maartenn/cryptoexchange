package nl.maarten.naberink.cryptoexchange.view.query.orderbook;

public class FindOrderBookByCoinPairQuery {

	private final String pair;

	public String getPair() {
		return pair;
	}

	public FindOrderBookByCoinPairQuery(String pair) {
		this.pair = pair;
	}

}
