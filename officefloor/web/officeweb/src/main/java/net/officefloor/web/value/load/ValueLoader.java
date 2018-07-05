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
package net.officefloor.web.value.load;

import net.officefloor.server.http.HttpException;
import net.officefloor.web.build.HttpValueLocation;

/**
 * Loads a value onto the Object graph.
 * 
 * @author Daniel Sagenschneider
 */
public interface ValueLoader {

	/**
	 * Loads the value onto the object graph.
	 * 
	 * @param name
	 *            Property name.
	 * @param value
	 *            Property value.
	 * @param location
	 *            {@link HttpValueLocation}.
	 * @throws HttpException
	 *             If fails to load the value.
	 */
	void loadValue(String name, String value, HttpValueLocation location) throws HttpException;

}