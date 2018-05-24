/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.plugin.servlet.security;

import java.security.Principal;

import javax.servlet.http.HttpServlet;

import net.officefloor.plugin.web.http.application.WebArchitect;
import net.officefloor.plugin.web.http.security.HttpAuthenticationRequiredException;

/**
 * <p>
 * HTTP Security interface for the {@link HttpServlet} container.
 * <p>
 * As {@link HttpServlet} always requires depending on HTTP Security, this
 * interface is provided to differentiate the {@link HttpServlet} so that
 * authentication is not always required. This enables mixing
 * {@link HttpServlet} security with {@link WebArchitect} security
 * (i.e. not escalate {@link HttpAuthenticationRequiredException} and just
 * provide <code>null</code> instance).
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpServletSecurity {
	
	/**
	 * Obtains the authentication scheme used.
	 * 
	 * @return Authentication scheme.
	 */
	String getAuthenticationScheme();

	/**
	 * Obtains the {@link Principal} for the user.
	 * 
	 * @return {@link Principal} for the user.
	 */
	Principal getUserPrincipal();

	/**
	 * Name of the user.
	 * 
	 * @return Name of the user.
	 */
	String getRemoteUser();

	/**
	 * Indicates if the user supports the role.
	 * 
	 * @param role
	 *            Role to check if user supports.
	 * @return <code>true</code> if the user supports the role.
	 */
	boolean isUserInRole(String role);

}