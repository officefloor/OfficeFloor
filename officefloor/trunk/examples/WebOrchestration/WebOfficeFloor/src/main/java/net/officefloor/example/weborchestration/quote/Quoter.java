package net.officefloor.example.weborchestration.quote;

import net.officefloor.example.weborchestration.AccountsLocal;
import net.officefloor.example.weborchestration.Customer;
import net.officefloor.example.weborchestration.Quote;
import net.officefloor.example.weborchestration.QuoteItem;
import net.officefloor.example.weborchestration.SalesLocal;
import net.officefloor.example.weborchestration.ShoppingCart;
import net.officefloor.example.weborchestration.WebUtil;
import net.officefloor.plugin.work.clazz.FlowInterface;

/**
 * Quotes the {@link ShoppingCart}.
 * 
 * @author daniel
 */
public class Quoter {

	/**
	 * Quotes the {@link ShoppingCart}.
	 * 
	 * @param customer
	 *            {@link Customer}.
	 * @return {@link Quote}.
	 */
	public Quote quote(Customer customer) {

		// Obtain the shopping cart
		SalesLocal sales = WebUtil.lookupService(SalesLocal.class);
		ShoppingCart shoppingCart = sales.retrieveShoppingCart(customer);

		// Quote the shopping cart
		AccountsLocal accounts = WebUtil.lookupService(AccountsLocal.class);
		Quote quote = accounts.createQuote(customer, shoppingCart
				.getShoppingCartItems());

		// Return the quote
		return quote;
	}

	/**
	 * Flows to display the {@link Quote}.
	 */
	@FlowInterface
	public static interface DisplayFlows {

		/**
		 * Displays the head of template.
		 * 
		 * @param quote
		 *            {@link Quote}.
		 */
		void displayHead(Quote quote);

		/**
		 * Displays the {@link QuoteItem}.
		 * 
		 * @param item
		 *            {@link QuoteItem}.
		 */
		void displayQuoteItem(QuoteItem item);

		/**
		 * Displays the tail of the template.
		 * 
		 * @param quote
		 *            {@link Quote}.
		 */
		void displayTail(Quote quote);
	}

	/**
	 * Displays the {@link Quote}.
	 * 
	 * @param quote
	 *            {@link Quote}.
	 */
	public void displayQuote(Quote quote, DisplayFlows flows) {

		// Display the head
		flows.displayHead(quote);

		// Display the quote items
		for (QuoteItem item : quote.getQuoteItems()) {
			flows.displayQuoteItem(item);
		}

		// Display the tail
		flows.displayTail(quote);
	}

}