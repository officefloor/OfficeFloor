/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.compile.spi.office;

import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectFlowType;
import net.officefloor.compile.managedobject.ManagedObjectTeamType;
import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.compile.spi.managedobject.ManagedObjectTeam;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.ManagedObject;
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
	 * @param timeout
	 *            Timeout for the {@link ManagedObject}.
	 */
	void setTimeout(long timeout);

	/**
	 * Obtains the {@link OfficeManagedObjectFlow} for the
	 * {@link ManagedObjectFlowType}.
	 * 
	 * @param managedObjectSourceFlowName
	 *            Name of the {@link ManagedObjectFlowType}.
	 * @return {@link OfficeManagedObjectFlow}.
	 */
	OfficeManagedObjectFlow getOfficeManagedObjectFlow(String managedObjectSourceFlowName);

	/**
	 * Obtains the {@link ManagedObjectTeam} for the
	 * {@link ManagedObjectTeamType}.
	 * 
	 * @param managedObjectSourceTeamName
	 *            Name of the {@link ManagedObjectTeamType}.
	 * @return {@link ManagedObjectTeam}.
	 */
	ManagedObjectTeam getManagedObjectTeam(String managedObjectSourceTeamName);

	/**
	 * Obtains the {@link OfficeManagedObjectDependency} for the
	 * {@link ManagedObjectDependencyType} for the Input {@link OfficeManagedObject}.
	 * 
	 * @param managedObjectDependencyName
	 *            Name of the {@link ManagedObjectDependencyType}.
	 * @return {@link OfficeManagedObjectDependency}.
	 */
	OfficeManagedObjectDependency getInputOfficeManagedObjectDependency(String managedObjectDependencyName);

	/**
	 * Obtains the {@link OfficeManagedObject} representing an instance use of a
	 * {@link ManagedObject} from the {@link ManagedObjectSource}.
	 * 
	 * @param managedObjectName
	 *            Name of the {@link OfficeManagedObject}. Typically this will
	 *            be the name under which the {@link ManagedObject} will be
	 *            registered to the {@link Office}.
	 * @param managedObjectScope
	 *            {@link ManagedObjectScope} of the {@link OfficeManagedObject}
	 *            within the {@link Office}.
	 * @return {@link OfficeManagedObject}.
	 */
	OfficeManagedObject addOfficeManagedObject(String managedObjectName, ManagedObjectScope managedObjectScope);

}