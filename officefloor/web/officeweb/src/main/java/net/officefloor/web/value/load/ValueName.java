/*-
 * #%L
 * Web Plug-in
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.web.value.load;

import net.officefloor.web.build.HttpValueLocation;

/**
 * Name of a value.
 * 
 * @author Daniel Sagenschneider
 */
public class ValueName {

	/**
	 * Name of value.
	 */
	private final String name;

	/**
	 * {@link HttpValueLocation}.
	 */
	private final HttpValueLocation location;

	/**
	 * Instantiate.
	 * 
	 * @param name     Name of value.
	 * @param location {@link HttpValueLocation}.
	 */
	public ValueName(String name, HttpValueLocation location) {
		this.name = name;
		this.location = location;
	}

	/**
	 * Obtains the name of the value.
	 * 
	 * @return Name of the value.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Obtains the {@link HttpValueLocation} for the value.
	 * 
	 * @return {@link HttpValueLocation} for the value. <code>null</code> to
	 *         indicate any.
	 */
	public HttpValueLocation getLocation() {
		return this.location;
	}

}
