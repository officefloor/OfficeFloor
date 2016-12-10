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
package net.officefloor.plugin.socket.server.http.parse.impl;

import java.io.Serializable;

import net.officefloor.plugin.socket.server.http.HttpHeader;

/**
 * {@link HttpHeader} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpHeaderImpl implements HttpHeader, Serializable {

	/**
	 * Name.
	 */
	private final String name;

	/**
	 * Value.
	 */
	private final String value;

	/**
	 * Initiate.
	 * 
	 * @param name
	 *            Name.
	 * @param value
	 *            Value.
	 */
	public HttpHeaderImpl(String name, String value) {
		this.name = name;
		this.value = value;
	}

	/*
	 * =================== HttpHeader ============================
	 */

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getValue() {
		return this.value;
	}

}