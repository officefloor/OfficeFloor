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
package net.officefloor.eclipse.extension.worksource;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.frame.api.execute.Task;

/**
 * Context for obtaining {@link Task} documentation.
 *
 * @author Daniel Sagenschneider
 */
public interface TaskDocumentationContext {

	/**
	 * Obtains the name of the {@link Task} to obtain documentation.
	 *
	 * @return Name of the {@link Task} to obtain documentation.
	 */
	String getTaskName();

	/**
	 * Obtains the {@link PropertyList} containing the properties for the
	 * {@link WorkSource}.
	 *
	 * @return {@link PropertyList} containing the properties for the
	 *         {@link WorkSource}.
	 */
	PropertyList getPropertyList();

	/**
	 * Obtains the {@link ClassLoader}.
	 *
	 * @return {@link ClassLoader}.
	 */
	ClassLoader getClassLoader();

}