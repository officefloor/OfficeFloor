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

package net.officefloor.compile.spi.section;

import net.officefloor.compile.internal.structure.SectionNode;
import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.section.TypeQualification;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.team.Team;

/**
 * {@link ManagedObject} within the {@link SectionNode}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SectionManagedObject extends SectionDependencyObjectNode {

	/**
	 * Obtains the name of this {@link SectionManagedObject}.
	 * 
	 * @return Name of this {@link SectionManagedObject}.
	 */
	String getSectionManagedObjectName();

	/**
	 * <p>
	 * Adds an {@link TypeQualification} for this {@link SectionManagedObject}.
	 * <p>
	 * This enables distinguishing {@link SectionManagedObject} instances to
	 * enable, for example, dynamic {@link Team} assignment.
	 * 
	 * @param qualifier
	 *            Qualifier. May be <code>null</code> if no qualification.
	 * @param type
	 *            Type (typically the fully qualified type).
	 */
	void addTypeQualification(String qualifier, String type);

	/**
	 * Obtains the {@link SectionManagedObjectDependency} for the
	 * {@link ManagedObjectDependencyType}.
	 * 
	 * @param managedObjectDependencyName
	 *            Name of the {@link ManagedObjectDependencyType}.
	 * @return {@link SectionManagedObjectDependency}.
	 */
	SectionManagedObjectDependency getSectionManagedObjectDependency(String managedObjectDependencyName);

}
