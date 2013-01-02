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
package net.officefloor.plugin.socket.server.http.request.config;

/**
 * HTTP header parameter.
 * 
 * @author Daniel Sagenschneider
 */
public class HeaderConfig {

	/**
	 * Header parameter name.
	 */
	public String name;

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Header parameter value.
	 */
	public String value;

	public void setValue(String value) {
		this.value = value;
	}
}
