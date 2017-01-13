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
package net.officefloor.compile.spi.section;

import net.officefloor.compile.internal.structure.SectionNode;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.frame.api.function.Work;

/**
 * {@link Work} within the {@link SectionNode}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SectionWork extends PropertyConfigurable {

	/**
	 * Obtains the name of this {@link SectionWork}.
	 * 
	 * @return Name of this {@link SectionWork}.
	 */
	String getSectionWorkName();

	/**
	 * Adds a {@link SectionTask}.
	 * 
	 * @param taskName
	 *            Name of the {@link SectionTask}.
	 * @param taskTypeName
	 *            Name of the {@link ManagedFunctionType} on the {@link FunctionNamespaceType}.
	 * @return {@link SectionTask}.
	 */
	SectionTask addSectionTask(String taskName, String taskTypeName);

	/**
	 * Specifies the initial {@link SectionTask} for this {@link SectionWork}.
	 * 
	 * @param task
	 *            Initial {@link SectionTask}.
	 */
	void setInitialTask(SectionTask task);

}