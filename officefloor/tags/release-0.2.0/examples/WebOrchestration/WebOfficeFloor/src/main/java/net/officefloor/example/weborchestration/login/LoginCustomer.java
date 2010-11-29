package net.officefloor.example.weborchestration.login;

import net.officefloor.example.weborchestration.Customer;
import net.officefloor.example.weborchestration.SalesLocal;
import net.officefloor.example.weborchestration.WebUtil;
import net.officefloor.plugin.socket.server.http.session.object.HttpSessionObject;
import net.officefloor.plugin.work.clazz.FlowInterface;

/**
 * Login the {@link Customer}.
 * 
 * @author daniel
 */
public class LoginCustomer {

	/**
	 * Flows for the {@link LoginCustomer}.
	 */
	@FlowInterface
	public static interface LoginCustomerFlows {

		/**
		 * Re-login.
		 * 
		 * @param credentials
		 *            {@link LoginCredentials}.
		 */
		void doReLogin(LoginCredentials credentials);

		/**
		 * {@link Customer} logged in.
		 * 
		 * @param customer
		 *            {@link Customer}.
		 */
		void loggedIn(Customer customer);
	}

	/**
	 * Logins the {@link Customer}.
	 * 
	 * @param credentials
	 *            {@link LoginCredentials}.
	 * @param flows
	 *            {@link LoginCustomerFlows}.
	 * @param sessionObject
	 *            {@link HttpSessionObject} for the {@link Customer}.
	 */
	public void login(LoginCredentials credentials, LoginCustomerFlows flows,
			HttpSessionObject<Customer> sessionObject) {

		// Obtain details
		String email = credentials.getEmail();
		String password = credentials.getPassword();

		// Ensure have email and password
		if (WebUtil.isBlank(email) || WebUtil.isBlank(password)) {
			// Flag to re-login
			flows.doReLogin(new LoginCredentials(email,
					"Must provide email and password to login"));
			return; // re-login
		}

		// Attempt to obtain the Customer
		SalesLocal sales = WebUtil.lookupService(SalesLocal.class);
		Customer customer = sales.retrieveCustomer(email);
		if (customer == null) {
			// Unknown customer, re-login
			flows.doReLogin(new LoginCredentials(email, "Unknown customer "
					+ email));
			return; // re-login
		}

		// Load the Customer into the Session
		sessionObject.setSessionObject(customer);

		// Logged in
		flows.loggedIn(customer);
	}

}