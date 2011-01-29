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
package net.officefloor.example.pageflowhttpserver;

import net.officefloor.plugin.work.clazz.FlowInterface;

/**
 * Example logic for the template.
 * 
 * @author Daniel Sagenschneider
 */
public class TemplateLogic {
	
	@FlowInterface
	public static interface PageFlows {
				
		void NoItems();
		
		void EndItems();
	}

	private ShoppingCartItem[] shoppingCartItems = new ShoppingCartItem[] {
			new ShoppingCartItem("Book", 10.53, 3),
			new ShoppingCartItem("Magazine", 5.51, 1) };

	public Customer getCustomer() {
		return new Customer("Daniel");
	}

	public ShoppingCartItem[] getShoppingCartItems() {
		return this.shoppingCartItems;
	}

	public void purchase() {
		// Mock shopping cart items being bought
		this.shoppingCartItems = null;
	}

}