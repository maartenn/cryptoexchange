package nl.maarten.naberink.cryptoexchange.view.query;

public class FindLatestPriceDataQuery {
	private final String pair;

	public String getPair() {
		return pair;
	}

	public FindLatestPriceDataQuery(String pair) {
		this.pair = pair;
	}
}
