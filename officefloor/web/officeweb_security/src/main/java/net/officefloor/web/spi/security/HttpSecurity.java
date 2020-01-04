/*-
 * #%L
 * Web Security
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.web.spi.security;

import java.io.Serializable;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.team.Team;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.web.session.HttpSession;

/**
 * HTTP security.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecurity<A, AC extends Serializable, C, O extends Enum<O>, F extends Enum<F>> {

	/**
	 * Creates the custom authentication.
	 * 
	 * @param context {@link AuthenticateContext}.
	 * @return Custom authentication.
	 */
	A createAuthentication(AuthenticationContext<AC, C> context);

	/**
	 * <p>
	 * Ratifies whether enough information is available to undertake authentication.
	 * <p>
	 * As authentication will likely require communication with external services
	 * (LDAP store, database, etc), this method allows checking whether enough
	 * information is available to undertake the authentication. The purpose is to
	 * avoid the {@link ManagedFunction} depending on dependencies of authentication
	 * subsequently causing execution by different {@link Team}. This is especially
	 * as the majority of {@link HttpRequest} servicing will use the
	 * {@link HttpSession} to cache details and not require the authentication
	 * dependencies causing the swap in {@link Team}.
	 * 
	 * @param credentials Credentials.
	 * @param context     {@link RatifyContext}.
	 * @return <code>true</code> should enough information be available to undertake
	 *         authentication. <code>false</code> if not enough information is
	 *         available for authentication.
	 */
	boolean ratify(C credentials, RatifyContext<AC> context);

	/**
	 * Undertakes authentication.
	 * 
	 * @param credentials Credentials.
	 * @param context     {@link AuthenticateContext}.
	 * @throws HttpException If failure in communicating to necessary security
	 *                       services.
	 */
	void authenticate(C credentials, AuthenticateContext<AC, O, F> context) throws HttpException;

	/**
	 * Triggers the authentication challenge to the client.
	 * 
	 * @param context {@link ChallengeContext}.
	 * @throws HttpException If failure in communicating to necessary security
	 *                       services.
	 */
	void challenge(ChallengeContext<O, F> context) throws HttpException;

	/**
	 * Logs out.
	 * 
	 * @param context {@link LogoutContext}.
	 * @throws HttpException If failure in communicating to necessary security
	 *                       services.
	 */
	void logout(LogoutContext<O, F> context) throws HttpException;

}
