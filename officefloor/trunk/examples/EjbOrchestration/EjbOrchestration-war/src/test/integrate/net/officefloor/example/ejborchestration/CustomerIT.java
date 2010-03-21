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

/**
 * Tests regarding {@link Customer}.
 * 
 * @author Daniel Sagenschneider
 */
public class CustomerIT extends SeleniumTestCase {

	/**
	 * Ensure able to create a {@link Customer}.
	 */
	public void testCreateCustomer() {

		// Ensure no customer
		this.assertTextPresent("Hello(\\s+)Welcome");

		// Create the customer
		this.clickLink("Login");
		this.clickLink("create login");
		this.inputText("name", "NewUser");
		this.inputText("email", "new.user@officefloor.net");
		this.inputText("password", "password");
		this.submit("create");

		// Ensure logged in as the customer
		this.assertTextPresent("Hello NewUser(\\s+)Welcome");
	}

	/**
	 * Ensure able to login.
	 */
	public void testLogin() {

		// Ensure not logged in
		this.assertTextPresent("Hello(\\s+)Welcome");

		// Login
		this.clickLink("Login");
		this.inputText("email", this.getCustomerEmail());
		this.inputText("password", "password");
		this.submit("loginCustomer");

		// Ensure logged in
		this.assertTextPresent("Hello " + this.getCustomerName()
				+ "(\\s+)Welcome");
	}

}