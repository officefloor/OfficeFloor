/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.web.spi.security;

import java.io.Serializable;

import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.session.HttpSession;

/**
 * Context for authentication.
 * 
 * @author Daniel Sagenschneider
 */
public interface AuthenticateContext<AC extends Serializable, O extends Enum<O>> extends AccessControlListener<AC> {

	/**
	 * Obtains the {@link ServerHttpConnection}.
	 * 
	 * @return {@link ServerHttpConnection}.
	 */
	ServerHttpConnection getConnection();

	/**
	 * Obtains the {@link HttpSession}.
	 * 
	 * @return {@link HttpSession}.
	 */
	HttpSession getSession();

	/**
	 * Obtains a dependency.
	 * 
	 * @param key
	 *            Key for the dependency.
	 * @return Dependency.
	 */
	Object getObject(O key);

}