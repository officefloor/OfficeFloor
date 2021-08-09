/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.compile.spi.officefloor;

import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectExecutionStrategyType;
import net.officefloor.compile.managedobject.ManagedObjectFlowType;
import net.officefloor.compile.managedobject.ManagedObjectFunctionDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectTeamType;
import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;

/**
 * {@link ManagedObjectSource} on the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorManagedObjectSource extends PropertyConfigurable {

	/**
	 * Obtains the name of this {@link OfficeFloorManagedObjectSource}.
	 * 
	 * @return Name of this {@link OfficeFloorManagedObjectSource}.
	 */
	String getOfficeFloorManagedObjectSourceName();

	/**
	 * Specifies the timeout for the {@link ManagedObject}.
	 * 
	 * @param timeout Timeout for the {@link ManagedObject}.
	 */
	void setTimeout(long timeout);

	/**
	 * Obtains the {@link ManagingOffice} for this
	 * {@link OfficeFloorManagedObjectSource}.
	 * 
	 * @return {@link ManagingOffice}.
	 */
	ManagingOffice getManagingOffice();

	/**
	 * Obtains the {@link OfficeFloorManagedObjectFlow} for he
	 * {@link ManagedObjectFlowType}.
	 * 
	 * @param managedObjectSourceFlowName Name of the {@link ManagedObjectFlowType}.
	 * @return {@link OfficeFloorManagedObjectFlow}.
	 */
	OfficeFloorManagedObjectFlow getOfficeFloorManagedObjectFlow(String managedObjectSourceFlowName);

	/**
	 * Obtains the {@link OfficeFloorManagedObjectTeam} for the
	 * {@link ManagedObjectTeamType}.
	 * 
	 * @param managedObjectSourceTeamName Name of the {@link ManagedObjectTeamType}.
	 * @return {@link OfficeFloorManagedObjectTeam}.
	 */
	OfficeFloorManagedObjectTeam getOfficeFloorManagedObjectTeam(String managedObjectSourceTeamName);

	/**
	 * Obtains the {@link OfficeFloorManagedObjectExecutionStrategy} for the
	 * {@link ManagedObjectExecutionStrategyType}.
	 * 
	 * @param managedObjectExecutionStrategyName Name of the
	 *                                           {@link ManagedObjectExecutionStrategyType}.
	 * @return {@link OfficeFloorManagedObjectExecutionStrategy}.
	 */
	OfficeFloorManagedObjectExecutionStrategy getOfficeFloorManagedObjectExecutionStrategy(
			String managedObjectExecutionStrategyName);

	/**
	 * Obtains the {@link OfficeFloorManagedObjectDependency} for the
	 * {@link ManagedObjectDependencyType} for the Input {@link ManagedObject}.
	 * 
	 * @param managedObjectDependencyName Name of the
	 *                                    {@link ManagedObjectDependencyType}.
	 * @return {@link OfficeFloorManagedObjectDependency}.
	 */
	OfficeFloorManagedObjectDependency getInputOfficeFloorManagedObjectDependency(String managedObjectDependencyName);

	/**
	 * Obtains the {@link OfficeFloorManagedObjectFunctionDependency} for the
	 * {@link ManagedObjectFunctionDependencyType} for the {@link ManagedObject}.
	 * 
	 * @param managedObjectFunctionDependencyName Name of the
	 *                                            {@link ManagedObjectFunctionDependencyType}.
	 * @return {@link OfficeFloorManagedObjectFunctionDependency}.
	 */
	OfficeFloorManagedObjectFunctionDependency getOfficeFloorManagedObjectFunctionDependency(
			String managedObjectFunctionDependencyName);

	/**
	 * Obtains the {@link OfficeFloorManagedObject} representing an instance use of
	 * a {@link ManagedObject} from the {@link ManagedObjectSource}.
	 * 
	 * @param managedObjectName  Name of the {@link OfficeFloorManagedObject}.
	 *                           Typically this will be the name under which the
	 *                           {@link ManagedObject} will be registered to the
	 *                           {@link Office}.
	 * @param managedObjectScope {@link ManagedObjectScope} of the
	 *                           {@link OfficeFloorManagedObject} within the
	 *                           {@link Office}.
	 * @return {@link OfficeFloorManagedObject}.
	 */
	OfficeFloorManagedObject addOfficeFloorManagedObject(String managedObjectName,
			ManagedObjectScope managedObjectScope);

}
