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

import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpSession;

import net.officefloor.example.weborchestration.Customer;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionContext;

/**
 * Utility functions for an {@link Action}.
 * 
 * @author Daniel Sagenschneider
 */
public class ActionUtil {

	/**
	 * {@link HttpSession} name for {@link Customer}.
	 */
	private static final String SESSION_CUSTOMER = "customer";

	/**
	 * Determines if the {@link Customer} is logged in.
	 * 
	 * @return <code>true</code> if the {@link Customer} is logged in.
	 */
	public static boolean isCustomerLoggedIn() {
		return (getLoggedInCustomer() != null);
	}

	/**
	 * Obtains the {@link Customer}.
	 * 
	 * @param session
	 *            Session map.
	 * @return {@link Customer} or <code>null</code> if not logged in.
	 */
	public static Customer getLoggedInCustomer() {
		Map<String, Object> session = ActionContext.getContext().getSession();
		return (Customer) session.get(SESSION_CUSTOMER);
	}

	/**
	 * Specifies the {@link Customer}.
	 * 
	 * @param customer
	 *            {@link Customer}.
	 */
	public static void setLoggedInCustomer(Customer customer) {
		Map<String, Object> session = ActionContext.getContext().getSession();
		session.put(SESSION_CUSTOMER, customer);
	}

	/**
	 * Determines if the value is blank.
	 * 
	 * @param value
	 *            Value to check.
	 * @return <code>true</code> if value is <code>null</code> or blank (empty
	 *         or just white spaces).
	 */
	public static boolean isBlank(String value) {
		if (value == null) {
			return true; // null so blank
		} else {
			// Return based on whether white spacing
			return (value.trim().length() == 0);
		}
	}

	/**
	 * Looks up the service by type.
	 * 
	 * @param serviceType
	 *            Type of service.
	 * @return Service.
	 * @throws ServiceLookupException
	 *             If fails to lookup the service.
	 */
	@SuppressWarnings("unchecked")
	public static <S> S lookupService(Class<S> serviceType)
			throws ServiceLookupException {
		try {
			Context context = new InitialContext();
			Object service = context.lookup(serviceType.getSimpleName());
			return (S) service;
		} catch (NamingException ex) {
			throw new ServiceLookupException(ex);
		}
	}

	/**
	 * All access via static methods.
	 */
	private ActionUtil() {
	}

}