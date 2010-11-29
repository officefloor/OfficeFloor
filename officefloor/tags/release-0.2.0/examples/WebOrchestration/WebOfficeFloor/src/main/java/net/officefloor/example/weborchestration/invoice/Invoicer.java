package net.officefloor.example.weborchestration.invoice;

import net.officefloor.example.weborchestration.AccountsLocal;
import net.officefloor.example.weborchestration.Customer;
import net.officefloor.example.weborchestration.Invoice;
import net.officefloor.example.weborchestration.InvoiceLineItem;
import net.officefloor.example.weborchestration.ProductWarehouseLocal;
import net.officefloor.example.weborchestration.Quote;
import net.officefloor.example.weborchestration.SalesLocal;
import net.officefloor.example.weborchestration.ShoppingCart;
import net.officefloor.example.weborchestration.WebUtil;
import net.officefloor.plugin.socket.server.http.session.object.HttpSessionObject;
import net.officefloor.plugin.work.clazz.FlowInterface;

/**
 * Invoices the {@link Quote}.
 * 
 * @author daniel
 */
public class Invoicer {

	/**
	 * Purchases the {@link ShoppingCart}.
	 * 
	 * @param customerSessionObject
	 *            {@link Customer}.
	 * @return {@link Invoice} of the purchased {@link ShoppingCart}.
	 */
	public Invoice purchaseShoppingCart(
			HttpSessionObject<Customer> customerSessionObject) {

		// Obtain the customer
		Customer customer = customerSessionObject.getSessionObject();

		// Obtain the Shopping Cart
		SalesLocal sales = WebUtil.lookupService(SalesLocal.class);
		ShoppingCart shoppingCart = sales.retrieveShoppingCart(customer);

		// Create the Quote
		AccountsLocal accounts = WebUtil.lookupService(AccountsLocal.class);
		Quote quote = accounts.createQuote(customer, shoppingCart
				.getShoppingCartItems());

		// Purchase the Quote
		return this.purchase(quote, accounts);
	}

	/**
	 * Purchases the {@link Quote}.
	 * 
	 * @param input
	 *            {@link InvoiceInput}.
	 * @return {@link Invoice} of the purchased {@link Quote}.
	 */
	public Invoice purchaseQuote(InvoiceInput input) {

		// Obtain the quote Id
		Long quoteId = Long.valueOf(input.getQuoteId());

		// Obtain the quote
		AccountsLocal accounts = WebUtil.lookupService(AccountsLocal.class);
		Quote quote = accounts.retrieveQuote(quoteId);

		// Purchase the quote
		return this.purchase(quote, accounts);
	}

	/**
	 * Purchases the {@link Quote}.
	 * 
	 * @param quote
	 *            {@link Quote}.
	 * @param accounts
	 *            {@link AccountsLocal}.
	 * @return {@link Invoice} for the purchases {@link Quote}.
	 */
	private Invoice purchase(Quote quote, AccountsLocal accounts) {

		// Create the invoice the quote
		Invoice invoice = accounts.createInvoice(quote);

		// Allocate the products for the invoice
		ProductWarehouseLocal warehouse = WebUtil
				.lookupService(ProductWarehouseLocal.class);
		warehouse.allocateProduct(invoice);

		// Return the invoice
		return invoice;
	}

	/**
	 * Flows to display the {@link Invoice}.
	 * 
	 * @author daniel
	 */
	@FlowInterface
	public static interface DisplayFlows {

		/**
		 * Displays the head.
		 * 
		 * @param invoice
		 *            {@link Invoice}.
		 */
		void displayHead(Invoice invoice);

		/**
		 * Displays the {@link InvoiceLineItem}.
		 * 
		 * @param lineItem
		 *            {@link InvoiceLineItem}.
		 */
		void displayInvoiceLineItem(InvoiceLineItem lineItem);

		/**
		 * Displays the tail.
		 */
		void displayTail();
	}

	/**
	 * Displays the {@link Invoice}.
	 * 
	 * @param invoice
	 *            {@link Invoice}.
	 * @param flows
	 *            {@link DisplayFlows}.
	 */
	public void displayInvoice(Invoice invoice, DisplayFlows flows) {

		// Display the head
		flows.displayHead(invoice);

		// Display the line items
		for (InvoiceLineItem lineItem : invoice.getInvoiceLineItems()) {
			flows.displayInvoiceLineItem(lineItem);
		}

		// Displays the tail
		flows.displayTail();
	}

}