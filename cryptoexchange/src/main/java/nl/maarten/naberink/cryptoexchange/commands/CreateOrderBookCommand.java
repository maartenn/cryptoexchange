package nl.maarten.naberink.cryptoexchange.commands;

import java.math.BigDecimal;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public class CreateOrderBookCommand {
	@TargetAggregateIdentifier
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

	public CreateOrderBookCommand(String id, String pair, BigDecimal buyCommission, BigDecimal sellCommission) {
		this.id = id;
		this.pair = pair;
		this.buyCommission = buyCommission;
		this.sellCommission = sellCommission;
	}

}
