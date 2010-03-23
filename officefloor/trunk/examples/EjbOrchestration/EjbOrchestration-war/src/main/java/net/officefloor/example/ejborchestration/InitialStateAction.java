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

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionSupport;

/**
 * {@link Action} for initial state.
 * 
 * @author Daniel Sagenschneider
 */
public class InitialStateAction extends ActionSupport {

	/**
	 * Obtains the {@link Customer} name.
	 * 
	 * @return {@link Customer} name.
	 */
	public String getCustomerName() {
		return StartupServlet.customer.getName();
	}

	/**
	 * Obtains the {@link Customer} email.
	 * 
	 * @return {@link Customer} email.
	 */
	public String getCustomerEmail() {
		return StartupServlet.customer.getEmail();
	}

	/**
	 * Obtains the {@link Product} listing.
	 * 
	 * @return {@link Product} listing.
	 */
	public List<Product> getProducts() {
		return StartupServlet.products;
	}

}