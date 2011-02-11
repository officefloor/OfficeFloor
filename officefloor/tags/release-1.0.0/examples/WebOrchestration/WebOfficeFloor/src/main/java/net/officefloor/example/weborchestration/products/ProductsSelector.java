/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.officefloor.example.weborchestration.products;

import java.util.Map;

import net.officefloor.example.weborchestration.Customer;
import net.officefloor.example.weborchestration.Product;
import net.officefloor.example.weborchestration.ProductCatalogLocal;
import net.officefloor.example.weborchestration.SalesLocal;
import net.officefloor.example.weborchestration.ShoppingCart;
import net.officefloor.example.weborchestration.ShoppingCartItem;
import net.officefloor.example.weborchestration.WebUtil;
import net.officefloor.plugin.web.http.session.object.HttpSessionObject;
import net.officefloor.plugin.work.clazz.FlowInterface;

/**
 * Loads the selected the {@link Product} instances to the {@link ShoppingCart}.
 * 
 * @author daniel
 */
public class ProductsSelector {

	/**
	 * Flows for the {@link ProductsSelector}.
	 */
	@FlowInterface
	public static interface Flows {

		/**
		 * Indicates invalid input.
		 * 
		 * @param products
		 *            Invalid {@link ProductQuantity} instances.
		 */
		void invalidInput(ProductQuantity[] products);

	}

	/**
	 * Loads the selected {@link Product} instances to the {@link ShoppingCart}.
	 * 
	 * @param selected
	 *            {@link ProductsSelected}.
	 * @param customerSessionObject
	 *            {@link HttpSessionObject} containing the {@link Customer}.
	 * @param flows
	 *            {@link Flows}.
	 */
	public void selectProducts(ProductsSelected selected,
			HttpSessionObject<Customer> customerSessionObject, Flows flows) {

		// Obtain the customer
		Customer customer = customerSessionObject.getSessionObject();

		// Obtain the shopping cart
		SalesLocal sales = WebUtil.lookupService(SalesLocal.class);
		ShoppingCart shoppingCart = sales.retrieveShoppingCart(customer);

		// Obtain the product catalog
		ProductCatalogLocal catalog = WebUtil
				.lookupService(ProductCatalogLocal.class);

		// Process the products
		boolean isError = false;
		Map<String, ProductQuantity> selectedProducts = selected.getProducts();
		for (String key : selectedProducts.keySet()) {
			ProductQuantity product = selectedProducts.get(key);

			// Determine if quantity (handling valid value)
			int quantity;
			try {
				String quantityText = product.getQuantity();
				quantity = (WebUtil.isBlank(quantityText) ? 0 : Integer
						.parseInt(quantityText.trim()));
				if (quantity < 0) {
					isError = true;
					product.setError("Must be a positive integer value");
				}
			} catch (NumberFormatException ex) {
				isError = true;
				product.setError("Must be an integer value");
				continue; // do not process further
			}

			// Ignore if quantity is zero
			if (quantity == 0) {
				continue;
			}

			// Add to Shopping Cart
			boolean isAddedToExistingItem = false;
			Long productId = Long.valueOf(product.getProductId());
			for (ShoppingCartItem item : shoppingCart.getShoppingCartItems()) {
				if (productId.equals(item.getProduct().getProductId())) {
					// Product already in shopping cart so add to item
					item.setQuantity(item.getQuantity() + quantity);
					isAddedToExistingItem = true;
				}
			}
			if (!isAddedToExistingItem) {
				// Add item for the Product
				Product itemProduct = catalog.retrieveProduct(productId);
				shoppingCart.getShoppingCartItems().add(
						new ShoppingCartItem(itemProduct, quantity));
			}
		}
		if (isError) {
			ProductQuantity[] products = selectedProducts.values().toArray(
					new ProductQuantity[0]);
			flows.invalidInput(products);
		}

		// No error, so store changes to shopping cart
		sales.storeShoppingCart(shoppingCart);

		// Successful
	}
}