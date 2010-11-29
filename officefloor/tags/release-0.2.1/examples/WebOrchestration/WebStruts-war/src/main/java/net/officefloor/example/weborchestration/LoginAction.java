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
package net.officefloor.example.weborchestration;

import net.officefloor.example.weborchestration.Customer;
import net.officefloor.example.weborchestration.SalesLocal;

import com.opensymphony.xwork2.ActionSupport;

/**
 * {@link ActionSupport} for login.
 * 
 * @author Daniel Sagenschneider
 */
public class LoginAction extends ActionSupport {

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
	 * Obtains the email.
	 * 
	 * @return Email.
	 */
	public String getEmail() {
		return this.email;
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
	 * Obtains the error in login.
	 * 
	 * @return Error in login.
	 */
	public String getError() {
		return this.error;
	}

	/*
	 * ======================= Action ==========================
	 */

	@Override
	public String execute() throws Exception {

		// Determine if already logged in
		Customer customer = ActionUtil.getLoggedInCustomer();
		if (customer != null) {
			// Already logged in
			return "logout";
		}

		// Ensure have email and password
		if (ActionUtil.isBlank(this.email) || ActionUtil.isBlank(this.password)) {
			// Clear the password to ensure always re-enter
			this.password = null;

			// Must have login details
			this.error = "Must provide email and password to login";
			return "re-login";
		}

		// As example only require password value to login

		// Attempt to obtain the Customer
		SalesLocal sales = ActionUtil.lookupService(SalesLocal.class);
		customer = sales.retrieveCustomer(this.email);
		if (customer == null) {
			this.error = "Unknown customer " + this.email;
			return "re-login";
		}

		// Register the customer into Session
		ActionUtil.setLoggedInCustomer(customer);

		// Successfully logged in
		return SUCCESS;
	}

}