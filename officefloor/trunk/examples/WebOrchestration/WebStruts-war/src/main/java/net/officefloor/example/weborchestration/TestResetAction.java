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
	public static List<Product> products;

	/*
	 * ================= ActionSupport =======================
	 */

	@Override
	public String execute() throws Exception {

		// Reset the for testing
		TestResetLocal setup = ActionUtil.lookupService(TestResetLocal.class);
		setup.reset();

		// Create customer
		customer = setup.setupCustomer();

		// Create the products
		products = setup.setupProducts();

		// Successful
		return SUCCESS;
	}

}