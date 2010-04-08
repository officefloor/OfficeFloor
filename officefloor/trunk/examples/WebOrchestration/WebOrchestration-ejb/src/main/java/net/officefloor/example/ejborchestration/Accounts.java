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

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * Accounts {@link Stateless} bean.
 * 
 * @author Daniel Sagenschneider
 */
@Stateless
public class Accounts implements AccountsLocal {

	/**
	 * Seed to create the {@link Account}.
	 */
	public static class AccountSeed {

		/**
		 * {@link Customer}.
		 */
		public final Customer customer;

		/**
		 * Allow only {@link Accounts} to seed the {@link Account}.
		 * 
		 * @param customer
		 *            {@link Customer} for the {@link Account}.
		 */
		private AccountSeed(Customer customer) {
			this.customer = customer;
		}
	}

	/**
	 * Seed to create the {@link Quote}.
	 */
	public static class QuoteSeed {

		/**
		 * {@link Customer}.
		 */
		public final Customer customer;

		/**
		 * Allow only {@link Accounts} to seed the {@link Quote}.
		 * 
		 * @param customer
		 *            {@link Customer} for the {@link Quote}.
		 */
		private QuoteSeed(Customer customer) {
			this.customer = customer;
		}
	}

	/**
	 * Seed to create the {@link Invoice}.
	 */
	public static class InvoiceSeed {

		/**
		 * {@link Customer}.
		 */
		public final Customer customer;

		/**
		 * {@link Account}.
		 */
		public final Account account;

		/**
		 * Allow only {@link Accounts} to seed the {@link Invoice}.
		 * 
		 * @param customer
		 *            {@link Customer}.
		 * @param account
		 *            {@link Account}.
		 */
		private InvoiceSeed(Customer customer, Account account) {
			this.customer = customer;
			this.account = account;
		}
	}

	/**
	 * {@link EntityManager}.
	 */
	@PersistenceContext(unitName = "product-unit")
	private EntityManager entityManager;

	/*
	 * ================== AccountsLocal =====================
	 */

	@Override
	public Account retrieveAccount(Customer customer) {

		// Attempt to find account
		Query query = this.entityManager
				.createQuery("SELECT a FROM Account a WHERE :customer member of a.customers");
		query.setParameter("customer", customer);
		List<Account> accounts = this.retrieveAccounts(query);

		// Determine if have account
		if (accounts.size() > 0) {
			// Have the account
			return accounts.get(0);
		}

		// No account, so create and return account
		Account account = new Account(new AccountSeed(customer));
		this.entityManager.persist(account);
		return account;
	}

	@Override
	public Quote createQuote(Customer customer,
			List<? extends InvoiceableItem> invoiceableItems) {

		// Create the quote
		Quote quote = new Quote(new QuoteSeed(customer));

		// Add Item for each invoiceable item
		List<QuoteItem> quoteItems = quote.getQuoteItems();
		for (InvoiceableItem item : invoiceableItems) {

			// Obtain the product
			Product product = item.getProduct();

			// Obtain the price
			double price = product.getPrice();

			// Obtain the quantity
			int quantity = item.getQuantity();

			// Calculate the item price to cents
			double itemPrice = Math.round((price * quantity) * 100) / 100;

			// Create and add the item
			quoteItems.add(new QuoteItem(product, price, quantity, itemPrice));
		}

		// Store the Quote
		this.entityManager.persist(quote);

		// Return the Quote
		return quote;
	}

	@Override
	public Quote retrieveQuote(Long quoteId) {
		return this.entityManager.find(Quote.class, quoteId);
	}

	@Override
	public Invoice createInvoice(Quote quote) {

		// Obtain the Account
		Customer customer = quote.getCustomer();
		Account account = this.retrieveAccount(customer);

		// Create the Invoice
		Invoice invoice = new Invoice(new InvoiceSeed(customer, account));

		// Add line items for invoice
		List<InvoiceLineItem> lineItems = invoice.getInvoiceLineItems();
		double invoicePrice = 0;
		for (QuoteItem item : quote.getQuoteItems()) {

			// Obtain quote item price
			double itemPrice = item.getQuoteItemPrice();
			invoicePrice += itemPrice;

			// Create and add the item
			lineItems.add(new InvoiceLineItem(item.getProduct(), item
					.getProductPrice(), item.getQuantity(), itemPrice));
		}

		// Store the Invoice
		this.entityManager.persist(invoice);

		// Debit the Account for the Invoice
		account.setBalance(account.getBalance() - invoicePrice);

		// Return the Invoice
		return invoice;
	}

	@Override
	public Invoice retrieveInvoice(Long invoiceId) {
		return this.entityManager.find(Invoice.class, invoiceId);
	}

	@Override
	public void creditAccount(Account account, double dollars) {
		// TODO implement AccountsLocal.creditAccount
		throw new UnsupportedOperationException(
				"TODO implement AccountsLocal.creditAccount");
	}

	/**
	 * Retrieves the {@link Account} instances for the {@link Query}.
	 * 
	 * @param query
	 *            {@link Query}.
	 * @return Listing of {@link Account} instances.
	 */
	@SuppressWarnings("unchecked")
	private List<Account> retrieveAccounts(Query query) {
		return (List<Account>) query.getResultList();
	}

}