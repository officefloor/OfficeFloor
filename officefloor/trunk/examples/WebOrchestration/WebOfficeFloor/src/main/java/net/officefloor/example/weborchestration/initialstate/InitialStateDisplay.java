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