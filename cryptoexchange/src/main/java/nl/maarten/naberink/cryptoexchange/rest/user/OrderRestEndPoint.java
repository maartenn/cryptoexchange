package nl.maarten.naberink.cryptoexchange.rest.user;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.annotation.security.RolesAllowed;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import nl.maarten.naberink.cryptoexchange.commands.PlaceOrderCommand;
import nl.maarten.naberink.cryptoexchange.enums.OrderType;
import nl.maarten.naberink.cryptoexchange.view.query.orderbook.FindOrderBookByCoinPairQuery;

@RolesAllowed("user")
@RestController
public class OrderRestEndPoint {

	private final CommandGateway commandGateway;
	private final QueryGateway queryGateway;

	public OrderRestEndPoint(CommandGateway commandGateway, QueryGateway queryGateway) {
		this.commandGateway = commandGateway;
		this.queryGateway = queryGateway;
	}

	@RequestMapping(value = "/buyOrder", method = RequestMethod.POST)
	public void confirmBuyOrder(@RequestParam String amountOfEur, @RequestParam(required = false) String price,
			@RequestParam String pair) {
		final CompletableFuture<String> query = queryGateway.query(new FindOrderBookByCoinPairQuery(pair),
				String.class);
		query.thenApply(uuid -> commandGateway
				.send(new PlaceOrderCommand(uuid, UUID.randomUUID().toString(), OrderType.BUY, BigDecimal.ZERO,
						new BigDecimal(amountOfEur), price != null ? new BigDecimal(price) : null, pair)));
	}

	@RequestMapping(value = "/sellOrder", method = RequestMethod.POST)
	public void confirmSellOrder(@RequestParam String amountOfCrypto, @RequestParam(required = false) String price,
			@RequestParam String pair) {
		final CompletableFuture<String> query = queryGateway.query(new FindOrderBookByCoinPairQuery(pair),
				String.class);
		query.thenApply(uuid -> commandGateway.send(new PlaceOrderCommand(uuid, UUID.randomUUID().toString(),
				OrderType.SELL, new BigDecimal(amountOfCrypto), BigDecimal.ZERO,
				price != null ? new BigDecimal(price) : null, pair)));
	}
}
