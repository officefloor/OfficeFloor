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

import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.state.HttpRequestState;

/**
 * Generic context for {@link HttpSecurity} actions.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecurityActionContext {

	/**
	 * Obtains the {@link ServerHttpConnection}.
	 * 
	 * @return {@link ServerHttpConnection}.
	 */
	ServerHttpConnection getConnection();

	/**
	 * <p>
	 * Qualifies the attribute name to this {@link HttpSecurity} instance.
	 * <p>
	 * Multiple {@link HttpSecuritySource} instances may be registered for the
	 * application. Potentially, some even of the same implementation - likely just
	 * configured differently for different needs.
	 * <p>
	 * Therefore, may use this method to provide a namespace on the attribute to
	 * keep its value isolated to just this instance use of the
	 * {@link HttpSecurity}.
	 * 
	 * @param attributeName Name of the attribute.
	 * @return Qualified attribute name to the {@link HttpSecurity} instance.
	 */
	String getQualifiedAttributeName(String attributeName);

	/**
	 * Obtains the {@link HttpSession}.
	 * 
	 * @return {@link HttpSession}.
	 */
	HttpSession getSession();

	/**
	 * Obtains the {@link HttpRequestState}.
	 * 
	 * @return {@link HttpRequestState}.
	 */
	HttpRequestState getRequestState();

}
