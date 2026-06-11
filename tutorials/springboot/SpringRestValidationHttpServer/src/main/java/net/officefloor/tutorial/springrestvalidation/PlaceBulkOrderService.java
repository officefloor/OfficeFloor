package net.officefloor.tutorial.springrestvalidation;

import jakarta.validation.Valid;
import net.officefloor.web.ObjectResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

// START SNIPPET: tutorial
@Validated
public class PlaceBulkOrderService {

	public void service(@Valid @RequestBody BulkOrderRequest request,
			ObjectResponse<OrderResponse> response) {
		response.send(new OrderResponse(
				"Bulk order: " + request.getQuantity() + " x " + request.getProduct()));
	}
}
// END SNIPPET: tutorial
