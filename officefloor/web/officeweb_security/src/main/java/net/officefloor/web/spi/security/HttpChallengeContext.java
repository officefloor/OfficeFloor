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

import net.officefloor.server.http.HttpHeader;

/**
 * Context for the {@link HttpChallenge}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpChallengeContext {

	/**
	 * <p>
	 * Sets the {@link HttpChallenge}.
	 * <p>
	 * This should be used instead of directly adding the {@link HttpHeader}, so
	 * that can potentially include multiple {@link HttpChallenge} instances.
	 * 
	 * @param authenticationScheme
	 *            Authentication scheme.
	 * @param realm
	 *            Realm.
	 * @return {@link HttpChallenge}.
	 */
	HttpChallenge setChallenge(String authenticationScheme, String realm);

}
