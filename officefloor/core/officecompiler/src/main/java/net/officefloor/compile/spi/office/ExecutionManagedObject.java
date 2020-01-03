/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.compile.spi.office;

import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectFlowType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * {@link ManagedObject} available to the {@link ExecutionExplorer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ExecutionManagedObject {

	/**
	 * Obtains the name of the {@link ManagedObject}.
	 * 
	 * @return Name of the {@link ManagedObject}.
	 */
	String getManagedObjectName();

	/**
	 * Obtains the {@link ManagedObjectType} for the {@link ManagedObject}.
	 * 
	 * @return {@link ManagedObjectType} for the {@link ManagedObject}.
	 */
	ManagedObjectType<?> getManagedObjectType();

	/**
	 * Obtains the {@link ExecutionManagedObject} for the
	 * {@link ManagedObjectDependencyType}.
	 * 
	 * @param dependencyType
	 *            {@link ManagedObjectDependencyType}.
	 * @return {@link ExecutionManagedObject} for the
	 *         {@link ManagedObjectDependencyType}.
	 */
	ExecutionManagedObject getManagedObject(ManagedObjectDependencyType<?> dependencyType);

	/**
	 * Obtains the {@link ExecutionManagedFunction} for the
	 * {@link ManagedObjectFlowType}.
	 * 
	 * @param flowType
	 *            {@link ManagedObjectFlowType}.
	 * @return {@link ExecutionManagedFunction} for the
	 *         {@link ManagedObjectFlowType}.
	 */
	ExecutionManagedFunction getManagedFunction(ManagedObjectFlowType<?> flowType);

}
