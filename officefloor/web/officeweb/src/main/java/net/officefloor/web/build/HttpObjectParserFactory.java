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

/**
 * Factory for the creation of {@link HttpObjectParser} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpObjectParserFactory {

	/**
	 * Obtains the <code>Content-Type</code> supported by the create
	 * {@link HttpObjectParser} instances.
	 * 
	 * @return <code>Content-Type</code>.
	 */
	String getContentType();

	/**
	 * Creates the {@link HttpObjectParser} for the {@link Object}.
	 * 
	 * @param <T>
	 *            Object type.
	 * @param objectClass
	 *            {@link Object} {@link Class}.
	 * @return {@link HttpObjectParser} for the {@link Object}. May return
	 *         <code>null</code> if does not support parsing out the particular
	 *         {@link Object}.
	 * @throws Exception
	 *             If fails to create the {@link HttpObjectParser} for the
	 *             {@link Object}.
	 */
	<T> HttpObjectParser<T> createHttpObjectParser(Class<T> objectClass) throws Exception;

}