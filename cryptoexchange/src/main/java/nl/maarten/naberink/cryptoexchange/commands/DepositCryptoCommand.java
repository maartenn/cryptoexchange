package nl.maarten.naberink.cryptoexchange.commands;

import java.math.BigDecimal;

import org.axonframework.modelling.command.TargetAggregateIdentifier;
import org.springframework.lang.NonNull;

public class DepositCryptoCommand {
	@TargetAggregateIdentifier
	private final String orderBookId;
	private final BigDecimal amount;

	public String getOrderBookId() {
		return orderBookId;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public DepositCryptoCommand(@NonNull String orderBookId, @NonNull BigDecimal amount) {
		this.orderBookId = orderBookId;
		this.amount = amount;
	}
}
