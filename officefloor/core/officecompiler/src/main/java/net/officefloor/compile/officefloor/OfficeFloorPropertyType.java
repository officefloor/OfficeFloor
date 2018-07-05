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
package net.officefloor.compile.officefloor;

import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * <code>Type definition</code> for a {@link Property} of the
 * {@link OfficeFloor}.
 *
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorPropertyType {

	/**
	 * Obtains the name for the {@link Property}.
	 * 
	 * @return Name for the {@link Property}.
	 */
	String getName();

	/**
	 * Obtains the label to describe the {@link Property}.
	 * 
	 * @return Label to describe the {@link Property}.
	 */
	String getLabel();

	/**
	 * Obtains the default value for this {@link Property}.
	 * 
	 * @return Default value for this {@link Property}.
	 */
	String getDefaultValue();

}