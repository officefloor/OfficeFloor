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

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * Parses an object from the {@link ServerHttpConnection}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpObjectParser<T> extends HttpContentParser {

	/**
	 * Obtains the type of object parsed from the {@link ServerHttpConnection}.
	 * 
	 * @return Object type.
	 */
	Class<T> getObjectType();

	/**
	 * Parses the object from the {@link ServerHttpConnection}.
	 * 
	 * @param connection
	 *            {@link ServerHttpConnection}.
	 * @return Parsed object.
	 * @throws HttpException
	 *             If fails to parse the object from the
	 *             {@link ServerHttpConnection}.
	 */
	T parse(ServerHttpConnection connection) throws HttpException;

}