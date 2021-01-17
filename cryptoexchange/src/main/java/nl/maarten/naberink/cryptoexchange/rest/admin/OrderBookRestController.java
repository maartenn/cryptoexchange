package nl.maarten.naberink.cryptoexchange.rest.admin;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import nl.maarten.naberink.cryptoexchange.commands.CreateOrderBookCommand;
import nl.maarten.naberink.cryptoexchange.view.query.orderbook.FindAllOrderBooksQuery;
import nl.maarten.naberink.cryptoexchange.view.query.orderbook.FindOrderBookByCoinPairQuery;
import nl.maarten.naberink.cryptoexchange.view.query.orderbook.OrderBookDTO;

@RequestMapping("admin")
@RestController
public class OrderBookRestController {

	private final CommandGateway commandGateway;
	private final QueryGateway queryGateway;

	public OrderBookRestController(CommandGateway commandGateway, QueryGateway queryGateway) {
		this.commandGateway = commandGateway;
		this.queryGateway = queryGateway;
	}

	@RequestMapping(value = "/createOrderbook", method = RequestMethod.POST)
	public void createOrderbook(@RequestParam String pair) {
		final BigDecimal commission = BigDecimal.valueOf(0.01); // 1%
		commandGateway.send(new CreateOrderBookCommand(UUID.randomUUID().toString(), pair, commission, commission));
	}

	@RequestMapping(value = "/listOrderbooks", method = RequestMethod.GET)
	public List<OrderBookDTO> listOrderbooks() {
		return queryGateway.query(new FindAllOrderBooksQuery(), ResponseTypes.multipleInstancesOf(OrderBookDTO.class))
				.join();
	}

	@RequestMapping(value = "/findOrderbook", method = RequestMethod.GET)
	public CompletableFuture<String> findOrderbooks(@RequestParam String pair) {
		return queryGateway.query(new FindOrderBookByCoinPairQuery(pair), String.class);
	}

}
