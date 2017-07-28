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
package net.officefloor.plugin.web.http.application;

import net.officefloor.plugin.socket.server.http.ServerHttpConnection;

/**
 * Allows configuring the HTTP URI link for a {@link WebArchitect}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpUriLink {

	/**
	 * <p>
	 * Indicates whether the URI is to be secure. This allows configuring the
	 * URI to require a secure {@link ServerHttpConnection}.
	 * <p>
	 * Should the {@link ServerHttpConnection} not be appropriately secure, a
	 * redirect will be triggered.
	 * 
	 * @param isSecure
	 *            <code>true</code> should the URI require a secure
	 *            {@link ServerHttpConnection}.
	 */
	void setUriSecure(boolean isSecure);

}