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
import net.officefloor.compile.section.TypeQualification;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.team.Team;

/**
 * {@link ManagedObject} within the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorManagedObject extends OfficeFloorDependencyObjectNode {

	/**
	 * Obtains the name of this {@link OfficeFloorManagedObject}.
	 * 
	 * @return Name of this {@link OfficeFloorManagedObject}.
	 */
	String getOfficeFloorManagedObjectName();

	/**
	 * <p>
	 * Adds an {@link TypeQualification} for this
	 * {@link OfficeFloorManagedObject}.
	 * <p>
	 * This enables distinguishing {@link OfficeFloorManagedObject} instances to
	 * enable, for example, dynamic {@link Team} assignment.
	 * 
	 * @param qualifier
	 *            Qualifier. May be <code>null</code> if no qualification.
	 * @param type
	 *            Type (typically the fully qualified type).
	 */
	void addTypeQualification(String qualifier, String type);

	/**
	 * Obtains the {@link OfficeFloorManagedObjectDependency} for the
	 * {@link ManagedObjectDependencyType}.
	 * 
	 * @param managedObjectDependencyName
	 *            Name of the {@link ManagedObjectDependencyType}.
	 * @return {@link OfficeFloorManagedObjectDependency}.
	 */
	OfficeFloorManagedObjectDependency getOfficeFloorManagedObjectDependency(String managedObjectDependencyName);

}
