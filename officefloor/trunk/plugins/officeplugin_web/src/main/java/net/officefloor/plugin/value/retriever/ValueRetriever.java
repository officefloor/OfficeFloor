/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

package net.officefloor.plugin.value.retriever;

/**
 * Retrieves a value from an object graph.
 * 
 * @author Daniel Sagenschneider
 */
public interface ValueRetriever<T> {

	/**
	 * Retrieves the value from the object graph.
	 * 
	 * @param object
	 *            Root object of the object graph.
	 * @param name
	 *            Property name.
	 * @return Property value.
	 * @throws Exception
	 *             If fails to retrieve the value.
	 */
	String retrieveValue(T object, String name) throws Exception;

	/**
	 * Indicates if name maps to getter on the object graph to actually be able
	 * to provide the value.
	 * 
	 * @param name
	 *            Property name to check is available from the object graph.
	 * @return <code>true</code> if the object graph provides a corresponding
	 *         getter for the property name.
	 * @throws Exception
	 *             If fails to determine if value is retrievable.
	 */
	boolean isValueRetrievable(String name) throws Exception;

}