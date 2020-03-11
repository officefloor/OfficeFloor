/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.plugin.section.clazz;

/**
 * {@link PropertyValue} annotation.
 * 
 * @author Daniel Sagenschneider
 */
public class PropertyValueAnnotation {

	/**
	 * Name of the property.
	 */
	private final String name;

	/**
	 * Value of the property.
	 */
	private final String value;

	/**
	 * Instantiate.
	 * 
	 * @param name  Name of the property.
	 * @param value Value of the property.
	 */
	public PropertyValueAnnotation(String name, String value) {
		this.name = name;
		this.value = value;
	}

	/**
	 * Instantiate.
	 * 
	 * @param property {@link PropertyValue}.
	 */
	public PropertyValueAnnotation(PropertyValue property) {
		this(property.name(),
				Void.class.equals(property.valueClass()) ? property.valueClass().getName() : property.value());
	}

	/**
	 * Obtains the name of the property.
	 * 
	 * @return Name of the property.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Obtains the value of the property.
	 * 
	 * @return Value of the property.
	 */
	public String getValue() {
		return this.value;
	}

}
