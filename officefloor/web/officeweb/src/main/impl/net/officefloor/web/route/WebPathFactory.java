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
package net.officefloor.web.route;

import net.officefloor.web.HttpPathFactory;

/**
 * Factory for the creation of the {@link HttpPathFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WebPathFactory {

	/**
	 * Indicates if the path contains parameters.
	 * 
	 * @return <code>true</code> if the path contains parameters.
	 */
	boolean isPathParameters();

	/**
	 * Creates the web path.
	 * 
	 * @param valuesType
	 *            Type of object that will be provided to retrieve values from.
	 * @return {@link HttpPathFactory}.
	 * @throws Exception
	 *             If unable to create {@link HttpPathFactory} from the values
	 *             type.
	 */
	<T> HttpPathFactory<T> createHttpPathFactory(Class<T> valuesType) throws Exception;

}