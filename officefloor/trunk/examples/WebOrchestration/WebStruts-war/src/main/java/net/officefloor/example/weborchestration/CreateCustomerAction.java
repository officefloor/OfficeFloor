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

import net.officefloor.example.weborchestration.Customer;
import net.officefloor.example.weborchestration.SalesLocal;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionSupport;

/**
 * {@link Action} to create the {@link Customer}.
 * 
 * @author Daniel Sagenschneider
 */
public class CreateCustomerAction extends ActionSupport {

	/**
	 * Name.
	 */
	private String name;

	/**
	 * Email.
	 */
	private String email;

	/**
	 * Password.
	 */
	private String password;

	/**
	 * Error.
	 */
	private String error;

	/**
	 * HTTP parameter for name.
	 * 
	 * @param name
	 *            Name.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * HTTP parameter for email.
	 * 
	 * @param email
	 *            Email.
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * HTTP parameter for password.
	 * 
	 * @param password
	 *            Password.
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Obtains the error.
	 * 
	 * @return Error.
	 */
	public String getError() {
		return this.error;
	}

	@Override
	public String execute() throws Exception {

		// Ensure all details provided
		if (ActionUtil.isBlank(this.name) || ActionUtil.isBlank(this.email)
				|| ActionUtil.isBlank(this.password)) {
			// Must have all details
			this.error = "All fields are required";
			return "re-create";
		}

		// Determine if Customer already exists
		SalesLocal sales = ActionUtil.lookupService(SalesLocal.class);
		Customer customer = sales.retrieveCustomer(this.email);
		if (customer != null) {
			// Customer already exists
			this.error = "Customer already exists for email " + this.email;
			this.email = null; // clear as already exists
			return "re-create";
		}

		// Create the customer
		customer = sales.createCustomer(this.email, this.name);

		// Register the customer
		ActionUtil.setLoggedInCustomer(customer);

		// Successfully created the customer
		return SUCCESS;
	}

}