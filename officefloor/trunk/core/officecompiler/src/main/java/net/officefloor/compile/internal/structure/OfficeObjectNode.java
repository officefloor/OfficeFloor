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
package net.officefloor.compile.internal.structure;

import net.officefloor.compile.office.OfficeManagedObjectType;
import net.officefloor.compile.spi.office.OfficeObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObject;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.spi.governance.Governance;

/**
 * {@link OfficeObject} node.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeObjectNode extends Node, OfficeManagedObjectType,
		OfficeObject, LinkObjectNode {

	/**
	 * Indicates if this {@link OfficeManagedObjectType} has been initialised.
	 * 
	 * @return <code>true</code> if initialised.
	 */
	boolean isInitialised();

	/**
	 * Initialises this {@link OfficeManagedObjectType}.
	 * 
	 * @param objectType
	 *            Object type.
	 */
	void initialise(String objectType);

	/**
	 * <p>
	 * Adds an {@link AdministratorNode} for this
	 * {@link OfficeManagedObjectType}.
	 * <p>
	 * This allows the {@link OfficeManagedObjectType} to report the extension
	 * interfaces required to be supported by the
	 * {@link OfficeFloorManagedObject} for the {@link OfficeObject}.
	 * 
	 * @param administrator
	 *            {@link AdministratorNode}.
	 */
	void addAdministrator(AdministratorNode administrator);

	/**
	 * <p>
	 * Adds a {@link GovernanceNode} providing {@link Governance} for this
	 * {@link OfficeObject}.
	 * <p>
	 * This also allows the {@link OfficeManagedObjectType} to report the
	 * extension interfaces required to be supported by the
	 * {@link OfficeFloorManagedObject} for the {@link OfficeObject}.
	 * 
	 * @param governance
	 *            {@link GovernanceNode}.
	 */
	void addGovernance(GovernanceNode governance);

	/**
	 * Obtains the {@link GovernanceNode} instances to provide
	 * {@link Governance} over {@link BoundManagedObjectNode} linked to this
	 * {@link OfficeObjectNode}.
	 * 
	 * @return {@link GovernanceNode} instances.
	 */
	GovernanceNode[] getGovernances();

	/**
	 * Adds the context of the {@link OfficeFloor} containing this
	 * {@link OfficeObject}.
	 * 
	 * @param officeFloorLocation
	 *            Location of the {@link OfficeFloor}.
	 */
	void addOfficeFloorContext(String officeFloorLocation);

}