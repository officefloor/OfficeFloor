package net.officefloor.tutorial.springrestvalidation;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

// START SNIPPET: tutorial
public class OrderRequest {

	@NotBlank(message = "Product name is required")
	private String product;

	@Min(value = 1, message = "Quantity must be at least 1")
	private int quantity;

	public String getProduct() { return product; }
	public void setProduct(String product) { this.product = product; }

	public int getQuantity() { return quantity; }
	public void setQuantity(int quantity) { this.quantity = quantity; }
}
// END SNIPPET: tutorial
