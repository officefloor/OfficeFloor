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
package net.officefloor.server.http;

/**
 * {@link HttpHeader} instances for the {@link HttpRequest}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpRequestHeaders extends Iterable<HttpHeader> {

	/**
	 * Obtains the first {@link HttpHeader} by the name.
	 * 
	 * @param name
	 *            Name of the {@link HttpHeader}.
	 * @return First {@link HttpHeader} or <code>null</code> if not
	 *         {@link HttpHeader}.
	 */
	HttpHeader getHeader(CharSequence name);

	/**
	 * Obtains all the {@link HttpHeader} instances by the name.
	 * 
	 * @param name
	 *            Name of the {@link HttpHeader} instances.
	 * @return All {@link HttpHeader} instances by the name.
	 */
	Iterable<HttpHeader> getHeaders(CharSequence name);

	/**
	 * Obtains the {@link HttpHeader} at the index.
	 * 
	 * @param index
	 *            Index of the {@link HttpHeader}.
	 * @return {@link HttpHeader} at the index.
	 */
	HttpHeader headerAt(int index);

	/**
	 * Obtains the number of {@link HttpHeader} instances.
	 * 
	 * @return Number of {@link HttpHeader} instances.
	 */
	int length();

}