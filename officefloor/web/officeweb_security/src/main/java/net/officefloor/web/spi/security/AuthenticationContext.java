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
	void register(AccessControlListener<? super AC> accessControlListener);

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
