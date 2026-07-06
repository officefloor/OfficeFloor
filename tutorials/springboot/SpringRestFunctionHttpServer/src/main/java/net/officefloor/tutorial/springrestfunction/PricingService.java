package net.officefloor.tutorial.springrestfunction;

import org.springframework.stereotype.Service;

// START SNIPPET: tutorial
@Service
public class PricingService {

	private static final double UNIT_PRICE = 9.99;

	public double calculateTotal(String productId, int quantity) {
		return quantity * UNIT_PRICE;
	}
}
// END SNIPPET: tutorial
