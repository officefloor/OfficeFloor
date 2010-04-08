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

import java.util.LinkedList;
import java.util.List;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionSupport;

/**
 * {@link Action} to reset the state for next test.
 * 
 * @author Daniel Sagenschneider
 */
public class TestResetAction extends ActionSupport {

	/**
	 * {@link Customer}.
	 */
	public static Customer customer;

	/**
	 * {@link Product} instances.
	 */
	public static List<Product> products = new LinkedList<Product>();

	/*
	 * ================= ActionSupport =======================
	 */

	@Override
	public String execute() throws Exception {

		// Reset the for testing
		ActionUtil.lookupService(TestResetLocal.class).reset();

		// Create customer
		SalesLocal sales = ActionUtil.lookupService(SalesLocal.class);
		customer = sales.createCustomer("daniel@officefloor.net", "Daniel");

		// Create the products
		ProductCatalogLocal catalog = ActionUtil
				.lookupService(ProductCatalogLocal.class);
		products.add(catalog.createProduct("Shirt", 19.00));
		products.add(catalog.createProduct("Trousers", 25.00));
		products.add(catalog.createProduct("Hat", 7.00));

		// Successful
		return SUCCESS;
	}

}