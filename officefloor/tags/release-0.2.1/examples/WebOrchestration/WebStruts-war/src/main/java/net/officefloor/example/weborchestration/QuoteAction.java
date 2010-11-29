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

import net.officefloor.example.weborchestration.AccountsLocal;
import net.officefloor.example.weborchestration.Customer;
import net.officefloor.example.weborchestration.Quote;
import net.officefloor.example.weborchestration.SalesLocal;
import net.officefloor.example.weborchestration.ShoppingCart;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionSupport;

/**
 * {@link Action} to create a {@link Quote} for the {@link ShoppingCart}.
 * 
 * @author Daniel Sagenschneider
 */
public class QuoteAction extends ActionSupport {

	/**
	 * {@link Quote}.
	 */
	private Quote quote;

	/**
	 * Obtains the {@link Quote}.
	 * 
	 * @return {@link Quote}.
	 */
	public Quote getQuote() {
		return this.quote;
	}

	/*
	 * ==================== ActionSupport ===================
	 */

	@Override
	public String execute() throws Exception {

		// Ensure logged in
		if (!ActionUtil.isCustomerLoggedIn()) {
			return LOGIN;
		}
		Customer loggedInCustomer = ActionUtil.getLoggedInCustomer();

		// Obtain the Shopping Cart
		SalesLocal sales = ActionUtil.lookupService(SalesLocal.class);
		ShoppingCart shoppingCart = sales
				.retrieveShoppingCart(loggedInCustomer);

		// Quote the Shopping Cart
		AccountsLocal accounts = ActionUtil.lookupService(AccountsLocal.class);
		this.quote = accounts.createQuote(loggedInCustomer, shoppingCart
				.getShoppingCartItems());

		// Successful
		return SUCCESS;
	}

}