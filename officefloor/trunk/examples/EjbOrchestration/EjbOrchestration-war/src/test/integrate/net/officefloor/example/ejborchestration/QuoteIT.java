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
 * Tests a {@link Quote}.
 * 
 * @author Daniel Sagenschneider
 */
public class QuoteIT extends SeleniumTestCase {

	/**
	 * Ensure can create {@link Quote}.
	 */
	public void testCreateQuote() {
		this.login();

		// Select Products for Quote
		this.clickLink("Select Products");
		this.inputText("products[0].quantity", "10");
		this.submit("addProducts");

		// Ensure correct quantity on Shopping Cart
		this.assertTableCellValue("items", 1, 1, "10");

		// Create the Quote
		this.clickLink("Quote");

		// Verify Quote
		this.assertTableCellValue("items", 1, 2, "10");
	}

}