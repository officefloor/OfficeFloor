package net.officefloor.tutorial.springrestfunction;

import lombok.AllArgsConstructor;
import lombok.Data;

// START SNIPPET: tutorial
@Data
@AllArgsConstructor
public class PricedOrder {

	private String productId;
	private int quantity;
	private double total;
}
// END SNIPPET: tutorial
