package net.officefloor.tutorial.springrestvalidation;

// START SNIPPET: tutorial
public class OrderResponse {

	private final String message;

	public OrderResponse(String message) {
		this.message = message;
	}

	public String getMessage() { return message; }
}
// END SNIPPET: tutorial
