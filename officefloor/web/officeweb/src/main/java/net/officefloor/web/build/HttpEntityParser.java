/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.web.build;

import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * Parses the {@link ServerHttpConnection}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpEntityParser {

	/**
	 * <code>Content-Type</code> {@link HttpHeader} name.
	 */
	static final String CONTENT_TYPE = "content-type";

	/**
	 * Obtains the <code>Content-Type</code> handled by this
	 * {@link HttpEntityArgumentParser}.
	 * 
	 * @return <code>Content-Type</code> handled by this {@link HttpEntityArgumentParser}.
	 */
	String getContentType();

	/**
	 * Indicates whether can handle the {@link ServerHttpConnection}.
	 * 
	 * @param connection
	 *            {@link ServerHttpConnection}.
	 * @return <code>true</code> if able to handle parsing the entity of the
	 *         {@link ServerHttpConnection}.
	 */
	default boolean isHandle(ServerHttpConnection connection) {
		HttpHeader header = connection.getHttpRequest().getHttpHeaders().getHeader(CONTENT_TYPE);
		if (header != null) {
			String handledContentType = this.getContentType();
			String requestContentType = header.getValue();
			if (handledContentType.equalsIgnoreCase(requestContentType)) {
				return true; // handling content
			}
		}

		// As here, not handle
		return false;
	}

}