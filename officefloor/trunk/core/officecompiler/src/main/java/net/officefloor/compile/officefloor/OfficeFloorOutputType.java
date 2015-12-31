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
 * <code>Type definition</code> of an output from the {@link OfficeFloor}.
 *
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorOutputType {

	/**
	 * Obtains the name of the output of the {@link OfficeFloor}.
	 * 
	 * @return Name of the output of the {@link OfficeFloor}.
	 */
	String getOfficeFloorOutputName();

	/**
	 * Obtains the fully qualified class name of the argument from this output.
	 * 
	 * @return Fully qualified class name of the argument from this output.
	 */
	String getArgumentType();

	/**
	 * Should this output be synchronous, this returns the corresponding
	 * {@link OfficeFloorInputType}.
	 * 
	 * @return {@link OfficeFloorInputType} if synchronous output. Otherwise,
	 *         <code>null</code> to indicate no synchronous response expected.
	 */
	OfficeFloorInputType getHandlingOfficeFloorInputType();

}