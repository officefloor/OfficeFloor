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
