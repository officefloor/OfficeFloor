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

import java.io.Serializable;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;

/**
 * {@link ListedTask} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ListedTaskImpl implements ListedTask, Serializable {

	/**
	 * {@link Serializable} version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Name of the {@link Office}.
	 */
	private final String officeName;

	/**
	 * Name of the {@link Work}.
	 */
	private final String workName;

	/**
	 * Name of the {@link Task}.
	 */
	private final String taskName;

	/**
	 * Fully qualified class name of the parameter.
	 */
	private final String paramaterType;

	/**
	 * Initiate.
	 * 
	 * @param officeName
	 *            Name of the {@link Office}.
	 * @param workName
	 *            Name of the {@link Work}.
	 * @param taskName
	 *            Name of the {@link Task}.
	 * @param paramaterType
	 *            Fully qualified class name of the parameter. May be
	 *            <code>null</code>.
	 */
	public ListedTaskImpl(String officeName, String workName, String taskName,
			String paramaterType) {
		this.officeName = officeName;
		this.workName = workName;
		this.taskName = taskName;
		this.paramaterType = paramaterType;
	}

	/*
	 * ======================= ListedTask ===============================
	 */

	@Override
	public String getOfficeName() {
		return this.officeName;
	}

	@Override
	public String getWorkName() {
		return this.workName;
	}

	@Override
	public String getTaskName() {
		return this.taskName;
	}

	@Override
	public String getParameterType() {
		return this.paramaterType;
	}

}