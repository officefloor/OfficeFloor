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

package net.officefloor.example.weborchestration.initialstate;

import java.util.List;

import net.officefloor.example.weborchestration.Customer;
import net.officefloor.example.weborchestration.Product;
import net.officefloor.example.weborchestration.TestResetLocal;
import net.officefloor.example.weborchestration.WebUtil;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.work.clazz.FlowInterface;

public class InitialStateDisplay {

	/**
	 * Flows for the rendering.
	 */
	@FlowInterface
	public static interface Flow {

		void displayHeader(Customer customer);

		void displayProduct(Product product);

		void displayTail();

	}

	/**
	 * Handles displaying the Initial State.
	 */
	public void display(Flow flow, ServerHttpConnection connection)
			throws Exception {
		try {
			
			// Lookup the setup
			TestResetLocal setup = WebUtil.lookupService(TestResetLocal.class);

			// Setup the customer and products
			setup.reset();
			Customer customer = setup.setupCustomer();
			List<Product> products = setup.setupProducts();

			// Display the page
			flow.displayHeader(customer);
			for (Product product : products) {
				flow.displayProduct(product);
			}
			flow.displayTail();

		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

}