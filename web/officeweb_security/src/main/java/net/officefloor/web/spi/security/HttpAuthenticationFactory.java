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
import net.officefloor.web.security.HttpAuthentication;

/**
 * Factory for the creation of the {@link HttpAuthentication}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpAuthenticationFactory<A, C> {

	/**
	 * Creates {@link HttpAuthentication} from the custom authentication.
	 * 
	 * @param authentication
	 *            Custom authentication.
	 * @return {@link HttpAuthentication} adapting the custom access control.
	 */
	HttpAuthentication<C> createHttpAuthentication(A authentication) throws HttpException;

}
