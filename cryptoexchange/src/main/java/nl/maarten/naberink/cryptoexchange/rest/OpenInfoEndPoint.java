package nl.maarten.naberink.cryptoexchange.rest;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import nl.maarten.naberink.cryptoexchange.view.query.FindAllOrdersQuery;
import nl.maarten.naberink.cryptoexchange.view.query.FindBuyOrderByIdQuery;
import nl.maarten.naberink.cryptoexchange.view.query.FindLatestPriceDataQuery;
import nl.maarten.naberink.cryptoexchange.view.query.data.OrderData;
import nl.maarten.naberink.cryptoexchange.view.query.data.PriceInfo;

@RestController
public class OpenInfoEndPoint {
	private final QueryGateway queryGateway;

	public OpenInfoEndPoint(QueryGateway queryGateway) {
		this.queryGateway = queryGateway;
	}

	@RequestMapping(value = "/getPriceInfo", method = RequestMethod.GET)
	public CompletableFuture<PriceInfo> listOrderbooks(@RequestParam String pair) {
		return queryGateway.query(new FindLatestPriceDataQuery(pair), PriceInfo.class);
	}

	@GetMapping("/getOrder")
	public CompletableFuture<OrderData> getOrder(@RequestParam String orderId) {
		return queryGateway.query(new FindBuyOrderByIdQuery(orderId), OrderData.class);
	}

	// get all orders
	@GetMapping("/all-orders")
	public List<OrderData> findAllOrderedProducts() {
		return queryGateway.query(new FindAllOrdersQuery(), ResponseTypes.multipleInstancesOf(OrderData.class)).join();
	}
}
