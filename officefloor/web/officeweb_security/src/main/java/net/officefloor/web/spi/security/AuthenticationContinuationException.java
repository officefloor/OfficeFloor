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

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.build.HttpUrlContinuation;
import net.officefloor.web.security.HttpAuthentication;

/**
 * <p>
 * Indicates {@link HttpUrlContinuation} failure in {@link HttpAuthentication}.
 * <p>
 * Typically this occurs because the original request
 * {@link HttpUrlContinuation} state could not be obtained to continue
 * processing after authentication.
 * 
 * @author Daniel Sagenschneider
 */
public class AuthenticationContinuationException extends HttpException {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiate.
	 * 
	 * @param entity Entity.
	 */
	public AuthenticationContinuationException(String entity) {
		super(HttpStatus.INTERNAL_SERVER_ERROR, null, entity);
	}

}
