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

/**
 * Tests creating an Invoice.
 * 
 * @author Daniel Sagenschneider
 */
public class InvoiceTestCase extends AbstractSeleniumTestCase {

	/**
	 * Ensure able to create {@link Invoice} from {@link ShoppingCart}.
	 */
	public void testCreateInvoice() {
		this.login();

		// Select the Products
		this.selectProducts(5);

		// Purchase the Products
		this.submit("purchase");

		// Ensure invoice created (with nothing allocated)
		this.assertTableCellValue("lineItems", 1, 2, "5");
		this.assertTableCellValue("lineItems", 1, 3, "0");
	}

	/**
	 * Ensure on delivering {@link Product} instances that allocated to
	 * {@link Invoice}.
	 */
	public void testDeliverProduct() {

		// Deliver the products
		this.deliverProducts(10);

		// Create Invoice
		this.login();
		this.selectProducts(50);
		this.submit("purchase");

		// Ensure invoice created (with allocated products)
		this.assertTableCellValue("lineItems", 1, 2, "50");
		this.assertTableCellValue("lineItems", 1, 3, "10");
	}

	/**
	 * Deliver the {@link Product} instances.
	 * 
	 * @param quantity
	 *            Quantity.
	 */
	private void deliverProducts(int quantity) {

		// Deliver Products
		this.clickLink("Deliver Products");
		for (int i = 0; i < 3; i++) {
			this.inputText("products[" + i + "].quantity", String
					.valueOf(quantity));
		}
		this.submit("deliver");
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