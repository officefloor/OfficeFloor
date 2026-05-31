package net.officefloor.tutorial.springrestfunction;

import net.officefloor.plugin.section.clazz.Flow;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

// START SNIPPET: tutorial
public class ValidateOrderLogic {

	@FunctionalInterface
	public interface ValidOrderFlow {
		void flow(OrderRequest order);
	}

	public void service(
			@RequestBody OrderRequest request,
			@Flow("valid") ValidOrderFlow validFlow,
			ObjectResponse<OrderResponse> response) {
		if (request.getProductId() == null || request.getProductId().isBlank()
				|| request.getQuantity() <= 0) {
			response.send(new OrderResponse(null, request.getProductId(), request.getQuantity(), 0.0));
		} else {
			validFlow.flow(request);
		}
	}
}
// END SNIPPET: tutorial
