/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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
package net.officefloor.example.weborchestration;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.example.weborchestration.Customer;
import net.officefloor.example.weborchestration.Product;
import net.officefloor.example.weborchestration.ProductCatalogLocal;
import net.officefloor.example.weborchestration.SalesLocal;
import net.officefloor.example.weborchestration.ShoppingCart;
import net.officefloor.example.weborchestration.ShoppingCartItem;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionSupport;

/**
 * {@link Action} to handle {@link Product} instances selected.
 * 
 * @author Daniel Sagenschneider
 */
public class ProductsSelectedAction extends ActionSupport {

	/**
	 * {@link ProductQuantity} instances.
	 */
	private List<ProductQuantity> products = new LinkedList<ProductQuantity>();

	/**
	 * Obtains the {@link ProductQuantity} instances.
	 * 
	 * @return {@link ProductQuantity} instances.
	 */
	public List<ProductQuantity> getProducts() {
		return this.products;
	}

	/*
	 * ========================= ActionSupport ==========================
	 */

	@Override
	public String execute() throws Exception {

		// Ensure the customer is logged in
		if (!ActionUtil.isCustomerLoggedIn()) {
			return LOGIN;
		}
		Customer loggedInCustomer = ActionUtil.getLoggedInCustomer();

		// Obtain the Shopping Cart
		SalesLocal sales = ActionUtil.lookupService(SalesLocal.class);
		ShoppingCart shoppingCart = sales
				.retrieveShoppingCart(loggedInCustomer);

		// Obtain the Product Catalog
		ProductCatalogLocal catalog = ActionUtil
				.lookupService(ProductCatalogLocal.class);

		// Process the products
		boolean isError = false;
		for (ProductQuantity product : this.products) {

			// Determine if quantity (handling valid value)
			int quantity;
			try {
				String quantityText = product.getQuantity();
				quantity = (ActionUtil.isBlank(quantityText) ? 0 : Integer
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
			return ERROR;
		}

		// No error, so store changes to shopping cart
		sales.storeShoppingCart(shoppingCart);

		// Successful
		return SUCCESS;
	}

}