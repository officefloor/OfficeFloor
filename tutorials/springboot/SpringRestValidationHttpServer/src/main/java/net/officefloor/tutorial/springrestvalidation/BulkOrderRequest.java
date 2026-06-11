package net.officefloor.tutorial.springrestvalidation;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

// START SNIPPET: tutorial
public class BulkOrderRequest {

	@NotBlank(message = "Product name is required")
	private String product;

	@Min(value = 2, message = "Bulk quantity must be at least 2")
	@EvenQuantity
	private int quantity;

	public String getProduct() { return product; }
	public void setProduct(String product) { this.product = product; }

	public int getQuantity() { return quantity; }
	public void setQuantity(int quantity) { this.quantity = quantity; }
}
// END SNIPPET: tutorial
