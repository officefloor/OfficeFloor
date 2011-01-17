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

package net.officefloor.example.weborchestration;

/**
 * Tests a Quote.
 * 
 * @author Daniel Sagenschneider
 */
public class QuoteTestCase extends AbstractSeleniumTestCase {

	/**
	 * Ensure can create {@link Quote}.
	 */
	public void testCreateQuote() {
		this.login();

		// Select the Products
		this.selectProducts(10);

		// Create the Quote
		this.clickLink("Quote");

		// Verify Quote
		this.assertTableCellValue("items", 1, 2, "10");
	}

	/**
	 * Ensure able to create an {@link Invoice} from the {@link Quote}.
	 */
	public void testInvoiceQuote() {
		this.login();

		// Select Products for Quote
		this.selectProducts(10);

		// Create the Quote
		this.clickLink("Quote");
		this.assertTableCellValue("items", 1, 2, "10");

		// Create the Invoice
		this.submit("purchase");

		// Ensure invoice created
		this.assertTableCellValue("lineItems", 1, 2, "10");

		// Ensure nothing allocated
		this.assertTableCellValue("lineItems", 1, 3, "0");
	}

	/**
	 * Select the {@link Product} instances.
	 * 
	 * @param quantity
	 *            Quantity.
	 */
	private void selectProducts(int quantity) {

		// Select Products for Quote
		this.clickLink("Select Products");
		this.inputText("products[0].quantity", String.valueOf(quantity));
		this.submit("addProducts");

		// Ensure correct quantity on Shopping Cart
		this.assertTableCellValue("items", 1, 1, String.valueOf(quantity));
	}

}