package net.officefloor.example.weborchestration.products;

import java.util.List;

import net.officefloor.example.weborchestration.Product;
import net.officefloor.example.weborchestration.ProductCatalogLocal;
import net.officefloor.example.weborchestration.WebUtil;
import net.officefloor.plugin.work.clazz.FlowInterface;

/**
 * Displays the {@link Product} instances.
 * 
 * @author daniel
 */
public class ProductsDisplayer {

	/**
	 * Flows for displaying the {@link Product}.
	 */
	@FlowInterface
	public static interface DisplayProductsFlows {

		/**
		 * Displays the {@link Product}.
		 * 
		 * @param quantity
		 *            {@link ProductQuantity} containing detail to display.
		 */
		void displayProduct(ProductQuantity quantity);
	}

	/**
	 * Displays the {@link Product} instances.
	 * 
	 * @param flows
	 *            {@link DisplayProductsFlows}.
	 */
	public void displayProducts(DisplayProductsFlows flows) {

		// Obtain the product listing
		ProductCatalogLocal catalog = WebUtil
				.lookupService(ProductCatalogLocal.class);
		List<Product> products = catalog.retrieveProductList();

		// Display the products
		for (int i = 0; i < products.size(); i++) {
			Product product = products.get(i);
			flows.displayProduct(new ProductQuantity(i, product));
		}
	}

}