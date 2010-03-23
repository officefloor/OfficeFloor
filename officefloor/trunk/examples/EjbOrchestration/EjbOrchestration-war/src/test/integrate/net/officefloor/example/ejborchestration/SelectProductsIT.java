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
package net.officefloor.example.ejborchestration;

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
		assertEquals("Incorrect invalid error", "Must be an integer value",
				this.selenium.getTable("products.1.3"));
		assertEquals("Incorrect negative error",
				"Must be a positive integer value", this.selenium
						.getTable("products.2.3"));
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
		assertEquals("Incorrect item quantity", "3", this.selenium
				.getTable("items.1.1"));
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