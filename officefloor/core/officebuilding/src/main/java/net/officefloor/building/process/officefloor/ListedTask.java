/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2014 Daniel Sagenschneider
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
package net.officefloor.building.process.officefloor;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Listed {@link Task} within the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ListedTask {

	/**
	 * Obtains the name of the {@link Office}.
	 * 
	 * @return Name of the {@link Office}.
	 */
	String getOfficeName();

	/**
	 * Obtains the {@link Work} name.
	 * 
	 * @return Name of the {@link Work}.
	 */
	String getWorkName();

	/**
	 * Obtains the {@link Task} name.
	 * 
	 * @return Name of the {@link Task}.
	 */
	String getTaskName();

	/**
	 * Obtains the fully qualified class name of the parameter type.
	 * 
	 * @return Fully qualified class name of the parameter type. May be
	 *         <code>null</code> if no parameter.
	 */
	String getParameterType();

}