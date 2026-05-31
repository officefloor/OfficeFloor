package net.officefloor.tutorial.springrestfunction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// START SNIPPET: tutorial
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {

	private String productId;
	private int quantity;
}
// END SNIPPET: tutorial
