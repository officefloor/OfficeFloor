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
