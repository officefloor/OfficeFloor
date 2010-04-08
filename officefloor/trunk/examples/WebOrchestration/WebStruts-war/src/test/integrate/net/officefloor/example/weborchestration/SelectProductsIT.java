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

import net.officefloor.example.weborchestration.Product;

/**
 * Tests to select {@link Product} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class SelectProductsIT extends SeleniumTestCase {

	/**
	 * Ensure errors on invalid input.
	 */
	public void testInvalidSelection() {
		this.login();

		// Provide invalid values
		this.clickLink("Select Products");
		this.inputText("products[0].quantity", "invalid");
		this.inputText("products[1].quantity", "-1");
		this.submit("addProducts");

		// Ensure show error messages
		this.assertTableCellValue("products", 1, 3, "Must be an integer value");
		this.assertTableCellValue("products", 2, 3,
				"Must be a positive integer value");
	}

	/**
	 * Ensure able to select products.
	 */
	public void testSelectProducts() {
		this.login();

		// Select Products
		this.clickLink("Select Products");
		this.inputText("products[0].quantity", "3");
		this.submit("addProducts");

		// Ensure show Item in Shopping Cart
		this.assertTableCellValue("items", 1, 1, "3");
	}

	/**
	 * Ensure able to continue shopping.
	 */
	public void testContinueShopping() {
		this.login();

		// Select Products
		this.clickLink("Select Products");
		this.inputText("products[0].quantity", "3");
		this.submit("addProducts");

		// Continue Shopping
		this.clickLink("Continue Shopping");

		// Ensure continue Shopping
		this.assertTextPresent("Select Products");
	}

}