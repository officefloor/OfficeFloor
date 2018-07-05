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
package net.officefloor.web.value.retrieve;

import net.officefloor.web.HttpPathParameter;

/**
 * <p>
 * Property object type.
 * <p>
 * Provides methods for testing.
 * 
 * @author Daniel Sagenschneider
 */
public interface PropertyObject {

	/**
	 * Allows for <code>property.text</code> property name.
	 * 
	 * @return String value as per testing.
	 */
	@HttpPathParameter("test")
	String getText();

	/**
	 * Allows for <code>property.recursive.recursive.(etc)</code> property name.
	 * 
	 * @return String value as per testing.
	 */
	PropertyObject getRecursive();

}