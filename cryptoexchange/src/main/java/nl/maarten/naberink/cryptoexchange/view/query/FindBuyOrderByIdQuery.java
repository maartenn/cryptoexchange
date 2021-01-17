package nl.maarten.naberink.cryptoexchange.view.query;

public class FindBuyOrderByIdQuery {

	private final String orderId;

	public FindBuyOrderByIdQuery(String orderId) {
		this.orderId = orderId;
	}

	public String getOrderId() {
		return orderId;
	}

}
