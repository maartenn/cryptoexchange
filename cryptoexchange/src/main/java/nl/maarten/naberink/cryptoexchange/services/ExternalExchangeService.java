package nl.maarten.naberink.cryptoexchange.services;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

/**
 * Dummy class to mock an api connection to an external exchange. Build for
 * development purpose, commission is only 0,1% for sell / buy and the supply is
 * unlimited =D
 *
 */
@Component
public class ExternalExchangeService {

	private final BigDecimal commission = new BigDecimal(0.001);

	public ExternalExchangeService() {
	}

	public boolean buy(BigDecimal amountInCryptoToBuy) {
		amountInCryptoToBuy.add(amountInCryptoToBuy.multiply(commission));
		return true;
	}

	public boolean sell(double amountCryptoToSell) {
		throw new UnsupportedOperationException("not yet implemented");
	}

}
