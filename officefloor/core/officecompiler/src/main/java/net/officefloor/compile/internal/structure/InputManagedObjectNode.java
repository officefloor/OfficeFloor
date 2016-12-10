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

import net.officefloor.compile.spi.officefloor.OfficeFloorInputManagedObject;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Node representing an instance use of an Input {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface InputManagedObjectNode extends LinkObjectNode,
		BoundManagedObjectNode, OfficeFloorInputManagedObject {

	/**
	 * {@link Node} type.
	 */
	static String TYPE = "Input Managed Object";

	/**
	 * Initialises the {@link InputManagedObjectNode}.
	 */
	void initialise();

	/**
	 * Obtains the bound {@link ManagedObjectSourceNode} for this
	 * {@link InputManagedObjectNode}.
	 * 
	 * @return Bound {@link ManagedObjectSourceNode} for this
	 *         {@link InputManagedObjectNode}.
	 */
	ManagedObjectSourceNode getBoundManagedObjectSourceNode();

	/**
	 * Obtains the {@link GovernanceNode} instances providing {@link Governance}
	 * over this {@link InputManagedObjectNode}.
	 * 
	 * @param managingOffice
	 *            {@link OfficeNode} managing the {@link InputManagedObjectNode}
	 *            , which ensures that {@link Governance} does not extend beyond
	 *            the particular {@link OfficeNode}.
	 * @return {@link GovernanceNode} instances providing {@link Governance}
	 *         over this {@link InputManagedObjectNode}.
	 */
	GovernanceNode[] getGovernances(OfficeNode managingOffice);

}