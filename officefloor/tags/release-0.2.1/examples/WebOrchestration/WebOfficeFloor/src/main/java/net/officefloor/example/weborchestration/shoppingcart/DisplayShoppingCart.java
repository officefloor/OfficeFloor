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