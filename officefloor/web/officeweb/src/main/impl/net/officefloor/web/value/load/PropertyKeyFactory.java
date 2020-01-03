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

/**
 * Factory for a {@link PropertyKey}.
 * 
 * @author Daniel Sagenschneider
 */
public class PropertyKeyFactory {

	/**
	 * Indicates if case insensitive match.
	 */
	private final boolean isCaseInsensitive;

	/**
	 * Initiate.
	 * 
	 * @param isCaseInsensitive
	 *            Indicates if case insensitive match.
	 */
	public PropertyKeyFactory(boolean isCaseInsensitive) {
		this.isCaseInsensitive = isCaseInsensitive;
	}

	/**
	 * Creates the {@link PropertyKey}.
	 * 
	 * @param propertyName
	 *            Property name.
	 * @return {@link PropertyKey}.
	 */
	public PropertyKey createPropertyKey(String propertyName) {
		return new PropertyKey(propertyName, this.isCaseInsensitive);
	}

}
