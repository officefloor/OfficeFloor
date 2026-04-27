package net.officefloor.tutorial.springrestvalidation;

import jakarta.validation.Valid;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

import net.officefloor.web.ObjectResponse;

// START SNIPPET: tutorial
@Validated
public class PlaceOrderService {

	public void service(@Valid @RequestBody OrderRequest request,
			ObjectResponse<OrderResponse> response) {
		response.send(new OrderResponse("Ordered " + request.getQuantity()
				+ " x " + request.getProduct()));
	}
}
// END SNIPPET: tutorial
