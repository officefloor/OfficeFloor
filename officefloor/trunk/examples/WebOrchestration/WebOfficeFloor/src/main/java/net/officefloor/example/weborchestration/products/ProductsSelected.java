package net.officefloor.example.weborchestration.products;

import java.util.HashMap;
import java.util.Map;

public class ProductsSelected {

	/**
	 * {@link ProductQuantity} instances.
	 */
	private final Map<String, ProductQuantity> productQuanties = new HashMap<String, ProductQuantity>();

	/**
	 * Loads the {@link ProductQuantity} instances.
	 * 
	 * @param key
	 *            Key identifying the {@link ProductQuantity}.
	 * @param productQuantity
	 *            {@link ProductQuantity} instance.
	 */
	public void setProducts(String key, ProductQuantity productQuantity) {
		productQuantity.setRowIndex(Integer.parseInt(key));
		this.productQuanties.put(key, productQuantity);
	}

	/**
	 * Obtains the {@link ProductQuantity} instances.
	 * 
	 * @return {@link ProductQuantity} instances.
	 */
	public Map<String, ProductQuantity> getProducts() {
		return this.productQuanties;
	}
}