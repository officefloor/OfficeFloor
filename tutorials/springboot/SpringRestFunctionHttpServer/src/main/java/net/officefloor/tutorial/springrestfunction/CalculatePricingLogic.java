package net.officefloor.tutorial.springrestfunction;

import net.officefloor.plugin.section.clazz.Parameter;

// START SNIPPET: tutorial
public class CalculatePricingLogic {

	public PricedOrder price(
			@Parameter OrderRequest order,
			PricingService pricingService) {
		double total = pricingService.calculateTotal(order.getProductId(), order.getQuantity());
		return new PricedOrder(order.getProductId(), order.getQuantity(), total);
	}
}
// END SNIPPET: tutorial
