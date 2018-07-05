/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.web.spi.security;

import java.io.Serializable;

import net.officefloor.frame.api.managedobject.ProcessSafeOperation;
import net.officefloor.web.security.AuthenticateRequest;
import net.officefloor.web.security.LogoutRequest;

/**
 * Context for authentication.
 * 
 * @author Daniel Sagenschneider
 */
public interface AuthenticationContext<AC extends Serializable, C> {

	/**
	 * Obtains the qualifier for the {@link HttpSecurity} backing this
	 * {@link AuthenticationContext}.
	 * 
	 * @return Qualifier for the {@link HttpSecurity} backing this
	 *         {@link AuthenticationContext}.
	 */
	String getQualifier();

	/**
	 * Registers an {@link AccessControlListener}.
	 * 
	 * @param accessControlListener
	 *            {@link AccessControlListener}.
	 */
	void register(AccessControlListener<AC> accessControlListener);

	/**
	 * Undertakes authentication.
	 * 
	 * @param credentials
	 *            Credentials (if available). May be <code>null</code>.
	 * @param authenticateRequest
	 *            Optional {@link AuthenticateRequest}. May be <code>null</code>.
	 */
	void authenticate(C credentials, AuthenticateRequest authenticateRequest);

	/**
	 * Undertakes logout.
	 * 
	 * @param logoutRequest
	 *            Optional {@link LogoutRequest}. May be <code>null</code>.
	 */
	void logout(LogoutRequest logoutRequest);

	/**
	 * Undertakes a {@link ProcessSafeOperation}.
	 * 
	 * @param <R>
	 *            Return type.
	 * @param <T>
	 *            Possible {@link Exception} type.
	 * @param operation
	 *            {@link ProcessSafeOperation}.
	 * @return Return value.
	 * @throws T
	 *             Possible {@link Throwable}.
	 */
	<R, T extends Throwable> R run(ProcessSafeOperation<R, T> operation) throws T;

}