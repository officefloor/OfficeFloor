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
	 * Flows for displaying the {@link Product}.
	 */
	@FlowInterface
	public static interface DisplayProductsFlows {

		/**
		 * Displays the head.
		 */
		void displayHead();

		/**
		 * Displays the {@link Product}.
		 * 
		 * @param quantity
		 *            {@link ProductQuantity} containing detail to display.
		 */
		void displayProduct(ProductQuantity quantity);

		/**
		 * Displays the tail.
		 */
		void displayTail();
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

		// Display the head
		flows.displayHead();

		// Display the products
		for (ProductQuantity product : productQuantities) {
			flows.displayProduct(product);
		}

		// Display the tail
		flows.displayTail();
	}

}