/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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

package net.officefloor.compile.internal.structure;

import net.officefloor.compile.spi.office.ObjectDependency;
import net.officefloor.compile.spi.section.TaskObject;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.manage.Office;

/**
 * {@link TaskObject} node.
 * 
 * @author Daniel Sagenschneider
 */
public interface TaskObjectNode extends TaskObject, ObjectDependency,
		LinkObjectNode {

	/**
	 * Adds the context of the {@link Office} containing this
	 * {@link ObjectDependency}.
	 * 
	 * @param officeLocation
	 *            Location of the {@link Office}.
	 */
	void addOfficeContext(String officeLocation);

	/**
	 * Indicates if this {@link TaskObject} is a parameter to the {@link Task}.
	 * 
	 * @return <code>true</code> if this {@link TaskObject} is a parameter to
	 *         the {@link Task}.
	 */
	boolean isParameter();

}