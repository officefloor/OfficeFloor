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

import java.util.Arrays;
import java.util.List;

import net.officefloor.example.weborchestration.Account;
import net.officefloor.example.weborchestration.Accounts;
import net.officefloor.example.weborchestration.AccountsLocal;
import net.officefloor.example.weborchestration.Customer;
import net.officefloor.example.weborchestration.Invoice;
import net.officefloor.example.weborchestration.InvoiceLineItem;
import net.officefloor.example.weborchestration.Product;
import net.officefloor.example.weborchestration.ProductCatalog;
import net.officefloor.example.weborchestration.ProductCatalogLocal;
import net.officefloor.example.weborchestration.Quote;
import net.officefloor.example.weborchestration.QuoteItem;
import net.officefloor.example.weborchestration.Sales;
import net.officefloor.example.weborchestration.SalesLocal;
import net.officefloor.example.weborchestration.ShoppingCartItem;

/**
 * Tests the {@link Accounts}.
 * 
 * @author Daniel Sagenschneider
 */
public class AccountsTest extends EjbTestCase {

	/**
	 * {@link Accounts} to test.
	 */
	private final AccountsLocal accounts = this.lookup(AccountsLocal.class);

	/**
	 * {@link Sales}.
	 */
	private final SalesLocal sales = this.lookup(SalesLocal.class);

	/**
	 * {@link ProductCatalog}.
	 */
	private final ProductCatalogLocal catalog = this
			.lookup(ProductCatalogLocal.class);

	/**
	 * Ensure able to retrieve {@link Account}.
	 */
	public void testRetreiveAccount() throws Exception {

		// Create the customer
		Customer customer = this.sales.createCustomer("test@officefloor.net",
				"Daniel");

		// Retrieve the account
		Account account = this.accounts.retrieveAccount(customer);
		assertNotNull("Ensure have account", account);
		assertNotNull("Ensure have account Id", account.getAccountId());
		List<Customer> customersForAccount = account.getCustomers();
		assertEquals("Incorrect number of customers in account", 1,
				customersForAccount.size());
		assertEquals("Incorrect customer", customer.getEmail(),
				customersForAccount.get(0).getEmail());

		// Retrieve the account again to ensure same Account
		Account retrievedAgain = this.accounts.retrieveAccount(customer);
		assertEquals("Incorrect account retrieved", account.getAccountId(),
				retrievedAgain.getAccountId());
	}

	/**
	 * Ensure able to create an {@link Quote}.
	 */
	public void testCreateQuote() throws Exception {

		// Create the Customer
		Customer customer = this.sales.createCustomer("test@officefloor.net",
				"Daniel");

		double price = 1.00;
		int quantity = 2;

		// Create the product
		Product product = this.catalog.createProduct("Test", price);

		// Create the item to quote
		ShoppingCartItem shoppingCartItem = new ShoppingCartItem(product,
				quantity);

		// Create the Quote
		Quote createdQuote = this.accounts.createQuote(customer, Arrays
				.asList(shoppingCartItem));
		this.validateQuote(createdQuote, product, price, quantity);

		// Retrieve the Quote
		Quote retrievedQuote = this.accounts.retrieveQuote(createdQuote
				.getQuoteId());
		this.validateQuote(retrievedQuote, product, price, quantity);
		assertNotSame("Should be different instance retrieved", createdQuote,
				retrievedQuote);
	}

	/**
	 * Validates the {@link Quote}.
	 */
	private void validateQuote(Quote quote, Product product, double price,
			int quantity) {

		// Validate the quote
		assertNotNull("Must have Quote", quote);
		assertNotNull("Must have quote identifier", quote.getQuoteId());

		// Validate the Quote Items
		List<QuoteItem> items = quote.getQuoteItems();
		assertEquals("Incorrect number of quote items", 1, items.size());
		QuoteItem item = items.get(0);
		assertEquals("Incorrect item", product.getProductId(), item
				.getProduct().getProductId());
		assertEquals("Incorrect quantity", quantity, item.getQuantity());
		assertEquals("Incorrect product price", price, item.getProductPrice(),
				0.001);
		assertEquals("Incorrect item price", (price * quantity), item
				.getQuoteItemPrice(), 0.001);
	}

	/**
	 * Tests creating the {@link Invoice}.
	 */
	public void testCreateInvoice() throws Exception {

		// Create the Quote
		Customer customer = this.sales.createCustomer("test@officefloor.net",
				"Daniel");
		double price = 1.00;
		int quantity = 2;
		Product product = this.catalog.createProduct("Test", price);
		Quote quote = this.accounts.createQuote(customer, Arrays
				.asList(new ShoppingCartItem(product, quantity)));
		assertNotNull("Must have Quote", quote);

		// Create the Invoice
		Invoice createdInvoice = this.accounts.createInvoice(quote);
		this.validateInvoice(createdInvoice, product, price, quantity);

		// Retrieve the Invoice
		Invoice retrievedInvoice = this.accounts.retrieveInvoice(createdInvoice
				.getInvoiceId());
		this.validateInvoice(retrievedInvoice, product, price, quantity);
		assertNotSame("Should be different instance retrieved", createdInvoice,
				retrievedInvoice);

		// Retrieve the Account
		Account account = this.accounts.retrieveAccount(customer);
		assertEquals("Did not debit account", -(price * quantity), account
				.getBalance(), 0.001);
	}

	/**
	 * Validates the {@link Invoice}.
	 */
	private void validateInvoice(Invoice invoice, Product product,
			double price, int quantity) {

		// Validate the Invoice
		assertNotNull("Must have Invoice", invoice);
		assertNotNull("Must have invoice identifier", invoice.getInvoiceId());

		// Validate the Invoice Items
		List<InvoiceLineItem> items = invoice.getInvoiceLineItems();
		assertEquals("Incorrect number of invoice line items", 1, items.size());
		InvoiceLineItem item = items.get(0);
		assertEquals("Incorrect item", product.getProductId(), item
				.getProduct().getProductId());
		assertEquals("Incorrect quantity", quantity, item.getQuantity());
		assertEquals("Incorrect product price", price, item.getProductPrice(),
				0.001);
		assertEquals("Incorrect line item price", (price * quantity), item
				.getInvoiceLineItemPrice(), 0.001);
	}

}