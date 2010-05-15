package net.officefloor.example.weborchestration.login;

import net.officefloor.example.weborchestration.Customer;
import net.officefloor.example.weborchestration.CustomerExistsException;
import net.officefloor.example.weborchestration.SalesLocal;
import net.officefloor.example.weborchestration.WebUtil;
import net.officefloor.plugin.socket.server.http.session.HttpSession;
import net.officefloor.plugin.socket.server.http.session.object.HttpSessionObject;
import net.officefloor.plugin.work.clazz.FlowInterface;

/**
 * Creates the {@link Customer}.
 * 
 * @author daniel
 */
public class CustomerCreator {

	/**
	 * Flows for the {@link CustomerCreator}.
	 */
	@FlowInterface
	public static interface CustomerCreatorFlows {

		/**
		 * Indicates the {@link Customer} was not created.
		 * 
		 * @param details
		 *            Details of {@link Customer} not being created.
		 */
		void customerNotCreated(CustomerDetails details);
	}

	/**
	 * Creates the {@link Customer}.
	 * 
	 * @param details
	 *            Details to create the {@link Customer}.
	 * @param flows
	 *            {@link CustomerCreatorFlows}.
	 * @param sessionObject
	 *            {@link HttpSessionObject} to load {@link Customer} into the
	 *            {@link HttpSession}.
	 * @return Created {@link Customer}.
	 * @throws CustomerExistsException
	 *             If {@link Customer} already exists.
	 */
	public Customer createCustomer(CustomerDetails details,
			CustomerCreatorFlows flows,
			HttpSessionObject<Customer> sessionObject)
			throws CustomerExistsException {

		// Ensure all details provided
		String name = details.getName();
		String email = details.getEmail();
		String password = details.getPassword();
		if (WebUtil.isBlank(name) || WebUtil.isBlank(email)
				|| WebUtil.isBlank(password)) {
			// Must have all details
			flows.customerNotCreated(new CustomerDetails(name, email,
					"All fields are required"));
			return null; // not created
		}

		// Determine if Customer already exists
		SalesLocal sales = WebUtil.lookupService(SalesLocal.class);
		Customer customer = sales.retrieveCustomer(email);
		if (customer != null) {
			// Customer already exists (clear email as exists)
			flows.customerNotCreated(new CustomerDetails(name, null,
					"Customer already exists for email " + email));
			return null; // not created
		}

		// Create the customer
		customer = sales.createCustomer(email, name);

		// Register the customer into the session
		sessionObject.setSessionObject(customer);

		// Successfully created the customer
		return customer;
	}

	/**
	 * Transforms the {@link CustomerExistsException} into a
	 * {@link CustomerDetails}.
	 * 
	 * @param exception
	 *            {@link CustomerExistsException}.
	 * @return {@link CustomerDetails}.
	 */
	public CustomerDetails transformCustomerExistsException(
			CustomerExistsException exception) {
		// Indicate customer already exists
		return new CustomerDetails(null, null, exception.getMessage());
	}

}