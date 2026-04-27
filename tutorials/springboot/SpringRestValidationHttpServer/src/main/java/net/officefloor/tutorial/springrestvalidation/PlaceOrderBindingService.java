package net.officefloor.tutorial.springrestvalidation;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

import net.officefloor.web.ObjectResponse;

// START SNIPPET: tutorial
@Validated
public class PlaceOrderBindingService {

	public void service(@Valid @RequestBody OrderRequest request, BindingResult result,
			ObjectResponse<ResponseEntity<OrderResponse>> response) {
		if (result.hasErrors()) {
			String errors = result.getFieldErrors().stream()
					.map(e -> e.getField() + ": " + e.getDefaultMessage())
					.reduce((a, b) -> a + "; " + b)
					.orElse("Validation failed");
			response.send(ResponseEntity.badRequest().body(new OrderResponse(errors)));
			return;
		}
		response.send(ResponseEntity.ok(new OrderResponse(
				"Ordered " + request.getQuantity() + " x " + request.getProduct())));
	}
}
// END SNIPPET: tutorial
