/*-
 * #%L
 * Web Security
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.web.security;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpRequest;

/**
 * Dependency interface allowing the application to check if the HTTP client is
 * authenticated.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpAuthentication<C> {

	/**
	 * Indicates if authenticated.
	 * 
	 * @return <code>true</code> if authenticated.
	 * @throws HttpException
	 *             If authentication has been attempted but there were failures
	 *             in undertaking authentication.
	 */
	boolean isAuthenticated() throws HttpException;

	/**
	 * Obtains the type of credentials.
	 * 
	 * @return Type of credentials.
	 */
	Class<C> getCredentialsType();

	/**
	 * Triggers to undertake authentication.
	 *
	 * @param credentials
	 *            Credentials. May be <code>null</code> if no credentials are
	 *            required, or they are pulled from the {@link HttpRequest}.
	 * @param authenticateRequest
	 *            {@link AuthenticateRequest}.
	 */
	void authenticate(C credentials, AuthenticateRequest authenticateRequest);

	/**
	 * Obtains the {@link HttpAccessControl}.
	 * 
	 * @return {@link HttpAccessControl}.
	 * @throws AuthenticationRequiredException
	 *             If not authenticated.
	 * @throws HttpException
	 *             If failure occurred in authentication.
	 */
	HttpAccessControl getAccessControl() throws AuthenticationRequiredException, HttpException;

	/**
	 * Undertakes logging out.
	 * 
	 * @param logoutRequest
	 *            {@link LogoutRequest}.
	 */
	void logout(LogoutRequest logoutRequest);

}
