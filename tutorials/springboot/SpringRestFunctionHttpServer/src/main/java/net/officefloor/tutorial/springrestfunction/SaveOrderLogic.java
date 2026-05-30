package net.officefloor.tutorial.springrestfunction;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;

// START SNIPPET: tutorial
public class SaveOrderLogic {

	public void save(
			@Parameter PricedOrder order,
			OrderService orderService,
			ObjectResponse<OrderResponse> response) {
		String orderId = orderService.createOrder(order.getProductId(), order.getQuantity(), order.getTotal());
		response.send(new OrderResponse(orderId, order.getProductId(), order.getQuantity(), order.getTotal()));
	}
}
// END SNIPPET: tutorial
