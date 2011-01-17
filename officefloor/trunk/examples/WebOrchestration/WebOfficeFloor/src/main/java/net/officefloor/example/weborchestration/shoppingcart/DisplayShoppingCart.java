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

package net.officefloor.example.weborchestration.shoppingcart;

import net.officefloor.example.weborchestration.Customer;
import net.officefloor.example.weborchestration.SalesLocal;
import net.officefloor.example.weborchestration.ShoppingCart;
import net.officefloor.example.weborchestration.ShoppingCartItem;
import net.officefloor.example.weborchestration.WebUtil;
import net.officefloor.plugin.work.clazz.FlowInterface;

/**
 * Displays the {@link ShoppingCart}.
 * 
 * @author daniel
 */
public class DisplayShoppingCart {

	/**
	 * Flows for the {@link DisplayShoppingCart}.
	 */
	@FlowInterface
	public static interface Flows {

		/**
		 * Displays the {@link ShoppingCartItem}.
		 * 
		 * @param item
		 *            {@link ShoppingCartItem}.
		 */
		void displayShoppingCartItem(ShoppingCartItem item);
	}

	/**
	 * Displays the {@link ShoppingCartItem} instances.
	 * 
	 * @param customer
	 *            {@link Customer}.
	 * @param flows
	 *            Flows for {@link DisplayShoppingCart}.
	 */
	public void displayShoppingCartItems(Customer customer, Flows flows) {

		// Obtain the sales
		SalesLocal sales = WebUtil.lookupService(SalesLocal.class);

		// Obtain the shopping cart
		ShoppingCart shoppingCart = sales.retrieveShoppingCart(customer);

		// Display the products on the shopping cart
		for (ShoppingCartItem item : shoppingCart.getShoppingCartItems()) {
			flows.displayShoppingCartItem(item);
		}
	}

}