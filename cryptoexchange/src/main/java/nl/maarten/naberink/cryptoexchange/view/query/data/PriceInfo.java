package nl.maarten.naberink.cryptoexchange.view.query.data;

public class PriceInfo {
	private final BuyOrderData buyOrderData;
	private final SellOrderData sellOrderData;

	public BuyOrderData getBuyOrderData() {
		return buyOrderData;
	}

	public SellOrderData getSellOrderData() {
		return sellOrderData;
	}

	public PriceInfo(BuyOrderData buyData, SellOrderData sellData) {
		this.buyOrderData = buyData;
		this.sellOrderData = sellData;
	}

}
