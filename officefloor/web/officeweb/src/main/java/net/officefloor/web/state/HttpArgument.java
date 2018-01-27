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
package net.officefloor.web.state;

import java.io.Serializable;

import net.officefloor.web.build.HttpValueLocation;

/**
 * HTTP argument.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpArgument implements Serializable {

	/**
	 * {@link Serializable} version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Argument name.
	 */
	public final String name;

	/**
	 * Argument value.
	 */
	public final String value;

	/**
	 * Location that this {@link HttpArgument} was sourced.
	 */
	public final HttpValueLocation location;

	/**
	 * Next {@link HttpArgument}.
	 */
	public HttpArgument next = null;

	/**
	 * Instantiate.
	 * 
	 * @param name
	 *            Argument name.
	 * @param value
	 *            Argument value.
	 * @param location
	 *            {@link HttpValueLocation}.
	 */
	public HttpArgument(String name, String value, HttpValueLocation location) {
		this.name = name;
		this.value = value;
		this.location = location;
	}

}