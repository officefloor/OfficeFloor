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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Utility functions for the application.
 * 
 * @author Daniel Sagenschneider
 */
public class WebUtil {

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
	private WebUtil() {
	}

}