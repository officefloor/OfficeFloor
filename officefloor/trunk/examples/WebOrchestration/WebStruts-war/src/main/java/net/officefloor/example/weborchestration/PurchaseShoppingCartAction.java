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

import net.officefloor.example.weborchestration.AccountsLocal;
import net.officefloor.example.weborchestration.Customer;
import net.officefloor.example.weborchestration.Invoice;
import net.officefloor.example.weborchestration.ProductWarehouseLocal;
import net.officefloor.example.weborchestration.Quote;
import net.officefloor.example.weborchestration.SalesLocal;
import net.officefloor.example.weborchestration.ShoppingCart;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionSupport;

/**
 * {@link Action} to purchase the {@link ShoppingCart}.
 * 
 * @author Daniel Sagenschneider
 */
public class PurchaseShoppingCartAction extends ActionSupport {

	/**
	 * {@link Invoice} identifier.
	 */
	private Long invoiceId;

	/**
	 * Obtains the {@link Invoice}.
	 * 
	 * @return {@link Invoice}.
	 */
	public Invoice getInvoice() {
		AccountsLocal accounts = ActionUtil.lookupService(AccountsLocal.class);
		return accounts.retrieveInvoice(this.invoiceId);
	}

	/*
	 * =================== ActionSupport ======================
	 */

	@Override
	public String execute() {

		// Ensure logged in
		if (!ActionUtil.isCustomerLoggedIn()) {
			return LOGIN;
		}
		Customer customer = ActionUtil.getLoggedInCustomer();

		// Obtain the Shopping Cart
		SalesLocal sales = ActionUtil.lookupService(SalesLocal.class);
		ShoppingCart shoppingCart = sales.retrieveShoppingCart(customer);

		// Obtain the Quote
		AccountsLocal accounts = ActionUtil.lookupService(AccountsLocal.class);
		Quote quote = accounts.createQuote(customer, shoppingCart
				.getShoppingCartItems());

		// Create the Invoice for the Quote
		Invoice invoice = accounts.createInvoice(quote);

		// Allocate the products for the invoice
		ProductWarehouseLocal warehouse = ActionUtil
				.lookupService(ProductWarehouseLocal.class);
		warehouse.allocateProduct(invoice);

		// Specify the invoice identifier
		this.invoiceId = invoice.getInvoiceId();

		// Successful
		return SUCCESS;
	}

}