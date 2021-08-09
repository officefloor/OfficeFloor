/*-
 * #%L
 * Web Plug-in
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

package net.officefloor.web.session.spi;

import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.session.HttpSession;

/**
 * Newly created {@link HttpSession} requiring a session Id.
 *
 * @author Daniel Sagenschneider
 */
public interface FreshHttpSession {

	/**
	 * <p>
	 * Obtains the {@link ServerHttpConnection} requiring a new
	 * {@link HttpSession}.
	 * <p>
	 * Typically this should not be required to generate session Ids.
	 *
	 * @return {@link ServerHttpConnection} requiring a new {@link HttpSession}.
	 */
	ServerHttpConnection getConnection();

	/**
	 * Specifies the Id generated for the {@link HttpSession}.
	 *
	 * @param sessionId
	 *            Id generated for the {@link HttpSession}.
	 */
	void setSessionId(String sessionId);

	/**
	 * Flags failure in generating the {@link HttpSession} Id.
	 *
	 * @param failure
	 *            Failure in generating the {@link HttpSession} Id.
	 */
	void failedToGenerateSessionId(Throwable failure);

}
