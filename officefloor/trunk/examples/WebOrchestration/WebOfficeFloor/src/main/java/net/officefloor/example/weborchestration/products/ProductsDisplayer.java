package net.officefloor.example.weborchestration.products;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.example.weborchestration.Product;
import net.officefloor.example.weborchestration.ProductCatalogLocal;
import net.officefloor.example.weborchestration.WebUtil;
import net.officefloor.plugin.work.clazz.FlowInterface;
import edu.emory.mathcs.backport.java.util.Arrays;

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
	 * Obtains the {@link ProductQuantity} instances.
	 * 
	 * @return {@link ProductQuantity} instances.
	 */
	public ProductQuantity[] obtainProductQuantities() {

		// Obtain the product listing
		ProductCatalogLocal catalog = WebUtil
				.lookupService(ProductCatalogLocal.class);
		List<Product> products = catalog.retrieveProductList();

		// Load the product quantities
		List<ProductQuantity> quantities = new LinkedList<ProductQuantity>();
		for (int i = 0; i < products.size(); i++) {
			Product product = products.get(i);
			quantities.add(new ProductQuantity(i, product));
		}

		// Return the product quantities
		return quantities.toArray(new ProductQuantity[0]);
	}

	/**
	 * Displays the {@link Product} instances.
	 * 
	 * @param productQuantities
	 *            {@link ProductQuantity} instances to display.
	 * @param flows
	 *            {@link DisplayProductsFlows}.
	 */
	public void displayProducts(ProductQuantity[] productQuantities,
			DisplayProductsFlows flows) {

		// Sort the product quantities
		Arrays.sort(productQuantities, new Comparator<ProductQuantity>() {
			@Override
			public int compare(ProductQuantity a, ProductQuantity b) {
				return a.getRowIndex() - b.getRowIndex();
			}
		});

		// Display the products
		for (ProductQuantity product : productQuantities) {
			flows.displayProduct(product);
		}
	}

}