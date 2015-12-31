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
package net.officefloor.compile.officefloor;

import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * <code>Type definition</code> of an input to the {@link OfficeFloor}.
 *
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorInputType {

	/**
	 * Obtains the name of the input to the {@link OfficeFloor}.
	 * 
	 * @return Name of the input to the {@link OfficeFloor}.
	 */
	String getOfficeFloorInputName();

	/**
	 * Obtains the fully qualified class name of the parameter type to this
	 * input.
	 * 
	 * @return Fully qualified class name of the parameter type to this input.
	 */
	String getParameterType();

	/**
	 * Should this input be synchronous, this returns the corresponding
	 * {@link OfficeFloorOutputType}.
	 * 
	 * @return {@link OfficeFloorOutputType} if synchronous input. Otherwise,
	 *         <code>null</code> to indicate no synchronous response.
	 */
	OfficeFloorOutputType getResponseOfficeFloorOutputType();

}