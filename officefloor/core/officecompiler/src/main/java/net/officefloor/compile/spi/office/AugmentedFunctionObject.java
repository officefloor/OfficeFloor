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
package net.officefloor.compile.spi.office;

import net.officefloor.compile.spi.section.FunctionObject;
import net.officefloor.frame.api.function.ManagedFunction;

/**
 * Augmented {@link FunctionObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AugmentedFunctionObject {

	/**
	 * Obtains the name of this {@link FunctionObject}.
	 * 
	 * @return Name of this {@link FunctionObject}.
	 */
	String getFunctionObjectName();

	/**
	 * Flags this {@link FunctionObject} as a parameter for the
	 * {@link ManagedFunction}.
	 */
	void flagAsParameter();

	/**
	 * Indicates if the {@link FunctionObject} is already linked.
	 * 
	 * @return <code>true</code> if already linked.
	 */
	boolean isLinked();

}