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
package net.officefloor.server.http.impl;

import java.io.Serializable;

import net.officefloor.server.http.HttpHeader;

/**
 * {@link Serializable} {@link HttpHeader}.
 * 
 * @author Daniel Sagenschneider
 */
public class SerialisableHttpHeader implements HttpHeader, Serializable {

	/**
	 * {@link Serializable} version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Name.
	 */
	private final String name;

	/**
	 * Value.
	 */
	private final String value;

	/**
	 * Instantiate.
	 * 
	 * @param name
	 *            Name.
	 * @param value
	 *            Value.
	 */
	public SerialisableHttpHeader(String name, String value) {
		this.name = name;
		this.value = value;
	}

	/*
	 * ================= HttpHeader =====================
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
