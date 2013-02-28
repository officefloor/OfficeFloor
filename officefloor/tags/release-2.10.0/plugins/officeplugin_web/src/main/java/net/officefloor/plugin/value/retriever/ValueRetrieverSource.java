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
package net.officefloor.plugin.value.retriever;

/**
 * Sources a {@link ValueRetriever}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ValueRetrieverSource {

	/**
	 * Initialises.
	 * 
	 * @param isCaseInsensitive
	 *            Indicates if property name comparison is case insensitive.
	 */
	void init(boolean isCaseInsensitive) throws Exception;

	/**
	 * Sources the {@link ValueRetriever} for the type.
	 * 
	 * @param type
	 *            Type.
	 * @return {@link ValueRetriever} for the <code>type</code>.
	 * @throws Exception
	 *             If fails to obtain the {@link ValueRetriever}.
	 */
	<T> ValueRetriever<T> sourceValueRetriever(Class<T> type) throws Exception;

}