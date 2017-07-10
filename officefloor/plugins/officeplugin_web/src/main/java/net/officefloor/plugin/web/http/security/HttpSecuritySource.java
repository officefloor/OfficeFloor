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
package net.officefloor.plugin.web.http.security;

import java.io.IOException;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.team.Team;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.web.http.session.HttpSession;

/**
 * <p>
 * Source for obtaining HTTP security.
 * <p>
 * As security is specific to applications, both the security object and
 * credentials are specified by the application.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecuritySource<S, C, D extends Enum<D>, F extends Enum<F>> {

	/**
	 * <p>
	 * Obtains the specification for this.
	 * <p>
	 * This will be called before any other methods, therefore this method must
	 * be able to return the specification immediately after a default
	 * constructor instantiation.
	 * 
	 * @return Specification of this.
	 */
	HttpSecuritySourceSpecification getSpecification();

	/**
	 * Called only once after the {@link HttpSecuritySource} is instantiated.
	 * 
	 * @param context
	 *            {@link HttpSecuritySourceContext} to use in initialising.
	 * @throws Exception
	 *             Should the {@link HttpSecuritySource} fail to configure
	 *             itself from the input properties.
	 */
	void init(HttpSecuritySourceContext context) throws Exception;

	/**
	 * <p>
	 * Obtains the meta-data to describe this.
	 * <p>
	 * This is called after the {@link #init(HttpSecuritySourceContext)} method
	 * and therefore may use the configuration.
	 * <p>
	 * This should always return non-null. If there is a problem due to
	 * incorrect configuration, the {@link #init(HttpSecuritySourceContext)}
	 * should indicate this via an exception.
	 * 
	 * @return Meta-data to describe this.
	 */
	HttpSecuritySourceMetaData<S, C, D, F> getMetaData();

	/**
	 * <p>
	 * Ratifies whether enough information is available to undertake
	 * authentication.
	 * <p>
	 * As authentication will likely require communication with external
	 * services (LDAP store, database, etc), this method allows checking whether
	 * enough information is available to undertake the authentication. The
	 * purpose is to avoid the {@link ManagedFunction} depending on
	 * {@link HttpAuthentication} and inherit the dependencies of authentication
	 * subsequently causing execution by the authentication {@link Team} -
	 * especially as the majority of {@link HttpRequest} servicing will have the
	 * HTTP security cached in the {@link HttpSession} and not require the
	 * authentication dependencies causing the swap in {@link Team}.
	 * 
	 * @param context
	 *            {@link HttpRatifyContext}.
	 * @return <code>true</code> should enough information be available to
	 *         undertake authentication. <code>false</code> if not enough
	 *         information is available for authentication.
	 */
	boolean ratify(HttpRatifyContext<S, C> context);

	/**
	 * Undertakes authentication.
	 * 
	 * @param context
	 *            {@link HttpAuthenticateContext}.
	 * @throws IOException
	 *             If failure in communicating to necessary security services.
	 */
	void authenticate(HttpAuthenticateContext<S, C, D> context) throws IOException;

	/**
	 * Triggers the authentication challenge to the client.
	 * 
	 * @param context
	 *            {@link HttpChallengeContext}.
	 * @throws IOException
	 *             If failure in communicating to necessary security services.
	 */
	void challenge(HttpChallengeContext<D, F> context) throws IOException;

	/**
	 * Logs out.
	 * 
	 * @param context
	 *            {@link HttpLogoutContext}.
	 * @throws IOException
	 *             If failure in communicating to necessary security services.
	 */
	void logout(HttpLogoutContext<D> context) throws IOException;

}