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

package net.officefloor.compile.spi.office;

import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectFlowType;
import net.officefloor.compile.managedobject.ManagedObjectTeamType;
import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFunctionDependency;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;

/**
 * {@link ManagedObjectSource} within the {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeManagedObjectSource extends PropertyConfigurable {

	/**
	 * Obtains the name of this {@link OfficeManagedObjectSource}.
	 * 
	 * @return Name of this {@link OfficeManagedObjectSource}.
	 */
	String getOfficeManagedObjectSourceName();

	/**
	 * Specifies the timeout for the {@link ManagedObject}.
	 * 
	 * @param timeout Timeout for the {@link ManagedObject}.
	 */
	void setTimeout(long timeout);

	/**
	 * Obtains the {@link OfficeManagedObjectFlow} for the
	 * {@link ManagedObjectFlowType}.
	 * 
	 * @param managedObjectSourceFlowName Name of the {@link ManagedObjectFlowType}.
	 * @return {@link OfficeManagedObjectFlow}.
	 */
	OfficeManagedObjectFlow getOfficeManagedObjectFlow(String managedObjectSourceFlowName);

	/**
	 * Obtains the {@link OfficeManagedObjectTeam} for the
	 * {@link ManagedObjectTeamType}.
	 * 
	 * @param managedObjectSourceTeamName Name of the {@link ManagedObjectTeamType}.
	 * @return {@link OfficeManagedObjectTeam}.
	 */
	OfficeManagedObjectTeam getOfficeManagedObjectTeam(String managedObjectSourceTeamName);

	/**
	 * Obtains the {@link OfficeManagedObjectDependency} for the
	 * {@link ManagedObjectDependencyType} for the Input
	 * {@link OfficeManagedObject}.
	 * 
	 * @param managedObjectDependencyName Name of the
	 *                                    {@link ManagedObjectDependencyType}.
	 * @return {@link OfficeManagedObjectDependency}.
	 */
	OfficeManagedObjectDependency getInputOfficeManagedObjectDependency(String managedObjectDependencyName);

	/**
	 * Obtains the {@link OfficeManagedObjectFunctionDependency} for the
	 * {@link ManagedObjectFunctionDependency} of the {@link ManagedObjectSource}.
	 * 
	 * @param managedObjectFunctionDependencyName Name of the
	 *                                            {@link ManagedObjectFunctionDependency}.
	 * @return {@link OfficeManagedObjectFunctionDependency}.
	 */
	OfficeManagedObjectFunctionDependency getOfficeManagedObjectFunctionDependency(
			String managedObjectFunctionDependencyName);

	/**
	 * Obtains the {@link OfficeManagedObject} representing an instance use of a
	 * {@link ManagedObject} from the {@link ManagedObjectSource}.
	 * 
	 * @param managedObjectName  Name of the {@link OfficeManagedObject}. Typically
	 *                           this will be the name under which the
	 *                           {@link ManagedObject} will be registered to the
	 *                           {@link Office}.
	 * @param managedObjectScope {@link ManagedObjectScope} of the
	 *                           {@link OfficeManagedObject} within the
	 *                           {@link Office}.
	 * @return {@link OfficeManagedObject}.
	 */
	OfficeManagedObject addOfficeManagedObject(String managedObjectName, ManagedObjectScope managedObjectScope);

}
