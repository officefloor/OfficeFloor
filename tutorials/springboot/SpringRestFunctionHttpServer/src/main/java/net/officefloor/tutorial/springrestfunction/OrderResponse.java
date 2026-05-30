package net.officefloor.tutorial.springrestfunction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// START SNIPPET: tutorial
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

	private String orderId;
	private String productId;
	private int quantity;
	private double total;
}
// END SNIPPET: tutorial
