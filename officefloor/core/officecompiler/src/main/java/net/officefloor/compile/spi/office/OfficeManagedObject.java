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
import net.officefloor.compile.section.TypeQualification;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.team.Team;

/**
 * {@link ManagedObject} for the {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeManagedObject extends OfficeDependencyObjectNode, DependentManagedObject,
		AdministerableManagedObject, GovernerableManagedObject {

	/**
	 * Obtains the name of the {@link ManagedObject}.
	 * 
	 * @return Name of the {@link ManagedObject}.
	 */
	String getOfficeManagedObjectName();

	/**
	 * <p>
	 * Adds an {@link TypeQualification} for this {@link OfficeManagedObject}.
	 * <p>
	 * This enables distinguishing {@link OfficeManagedObject} instances to enable,
	 * for example, dynamic {@link Team} assignment.
	 * 
	 * @param qualifier Qualifier. May be <code>null</code> if no qualification.
	 * @param type      Type (typically the fully qualified type).
	 */
	void addTypeQualification(String qualifier, String type);

	/**
	 * Obtains the {@link OfficeManagedObjectDependency} for the
	 * {@link ManagedObjectDependencyType}.
	 * 
	 * @param managedObjectDependencyName Name of the
	 *                                    {@link ManagedObjectDependencyType}.
	 * @return {@link OfficeManagedObjectDependency}.
	 */
	OfficeManagedObjectDependency getOfficeManagedObjectDependency(String managedObjectDependencyName);

	/**
	 * <p>
	 * Adds an {@link OfficeAdministration} to be done before attempting load this
	 * {@link ManagedObject}.
	 * <p>
	 * The order that the {@link OfficeAdministration} instances are added is the
	 * order they will be done.
	 * 
	 * @param administration {@link OfficeAdministration} to be done before
	 *                       attempting load this {@link ManagedObject}.
	 */
	void addPreLoadAdministration(OfficeAdministration administration);

	/**
	 * Adds an {@link ExecutionObjectExplorer} for the execution tree from this
	 * {@link OfficeManagedObject}.
	 * 
	 * @param executionObjectExplorer {@link ExecutionObjectExplorer}.
	 */
	void addExecutionExplorer(ExecutionObjectExplorer executionObjectExplorer);

}
