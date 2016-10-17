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
package net.officefloor.compile.impl.office;

import net.officefloor.compile.object.ObjectDependencyType;
import net.officefloor.compile.section.OfficeSubSectionType;
import net.officefloor.compile.section.OfficeTaskType;
import net.officefloor.compile.spi.office.OfficeTask;

/**
 * {@link OfficeTaskType} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class OfficeTaskTypeImpl implements OfficeTaskType {

	/**
	 * Name of the {@link OfficeTask}.
	 */
	private final String taskName;

	/**
	 * Containing {@link OfficeSubSectionType}.
	 */
	private final OfficeSubSectionType subSectionType;

	/**
	 * {@link ObjectDependencyType} instances of the {@link OfficeTask}.
	 */
	private final ObjectDependencyType[] dependencies;

	/**
	 * Instantiate.
	 * 
	 * @param taskName
	 *            Name of the {@link OfficeTask}.
	 * @param subSectionType
	 *            Containing {@link OfficeSubSectionType}.
	 * @param dependencies
	 *            {@link ObjectDependencyType} instances of the
	 *            {@link OfficeTask}.
	 */
	public OfficeTaskTypeImpl(String taskName,
			OfficeSubSectionType subSectionType,
			ObjectDependencyType[] dependencies) {
		this.taskName = taskName;
		this.subSectionType = subSectionType;
		this.dependencies = dependencies;
	}

	/*
	 * =============== OfficeTaskType ======================
	 */

	@Override
	public String getOfficeTaskName() {
		return this.taskName;
	}

	@Override
	public OfficeSubSectionType getOfficeSubSectionType() {
		return this.subSectionType;
	}

	@Override
	public ObjectDependencyType[] getObjectDependencies() {
		return this.dependencies;
	}

}