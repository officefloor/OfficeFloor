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
package net.officefloor.frame.api.build;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * Builds details of a {@link ManagedObjectSource} being managed by an
 * {@link Office}.
 *
 * @author Daniel Sagenschneider
 */
public interface ManagingOfficeBuilder<F extends Enum<F>> {

	/**
	 * Specifies the name to bind the input {@link ManagedObject} within the
	 * {@link ProcessState} of the {@link Office}.
	 *
	 * @param inputManagedObjectName
	 *            Name to bind the input {@link ManagedObject} within the
	 *            {@link ProcessState} of the {@link Office}.
	 * @return {@link DependencyMappingBuilder} to map the dependencies of the
	 *         {@link ManagedObject}.
	 */
	DependencyMappingBuilder setInputManagedObjectName(String inputManagedObjectName);

	/**
	 * Links the {@link Flow} for the {@link ManagedObjectSource} to a
	 * {@link ManagedFunction} within the managing {@link Office}.
	 *
	 * @param key
	 *            Key identifying the {@link Flow} instigated by the
	 *            {@link ManagedObjectSource}.
	 * @param functionName
	 *            Name of the {@link ManagedFunction}.
	 */
	void linkProcess(F key, String functionName);

	/**
	 * Links the {@link Flow} for the {@link ManagedObjectSource} to a
	 * {@link ManagedFunction} within the managing {@link Office}.
	 *
	 * @param flowIndex
	 *            Index identifying the {@link Flow} instigated by the
	 *            {@link ManagedObjectSource}.
	 * @param functionName
	 *            Name of the {@link ManagedFunction}.
	 */
	void linkProcess(int flowIndex, String functionName);

}