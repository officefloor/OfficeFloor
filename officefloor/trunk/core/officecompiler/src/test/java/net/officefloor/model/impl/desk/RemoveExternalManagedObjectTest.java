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
package net.officefloor.model.impl.desk;

import net.officefloor.model.change.Change;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.ExternalManagedObjectModel;

/**
 * Tests removing an {@link ExternalManagedObjectModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RemoveExternalManagedObjectTest extends
		AbstractDeskChangesTestCase {

	/**
	 * Initiate to use specific setup {@link DeskModel}.
	 */
	public RemoveExternalManagedObjectTest() {
		super(true);
	}

	/**
	 * Tests attempting to remove an {@link ExternalManagedObjectModel} not on
	 * the {@link DeskModel}.
	 */
	public void testRemoveExternalManagedObjectNotOnDesk() {
		ExternalManagedObjectModel extMo = new ExternalManagedObjectModel(
				"NOT_ON_DESK", null);
		Change<ExternalManagedObjectModel> change = this.operations
				.removeExternalManagedObject(extMo);
		this.assertChange(change, extMo,
				"Remove external managed object NOT_ON_DESK", false,
				"External managed object NOT_ON_DESK not on desk");
	}

	/**
	 * Ensure can remove the {@link ExternalManagedObjectModel} from the
	 * {@link DeskModel} when other {@link ExternalManagedObjectModel} instances
	 * on the {@link DeskModel}.
	 */
	public void testRemoveExternalManagedObjectWhenOtherExternalManagedObjects() {
		ExternalManagedObjectModel extMo = this.model
				.getExternalManagedObjects().get(1);
		Change<ExternalManagedObjectModel> change = this.operations
				.removeExternalManagedObject(extMo);
		this.assertChange(change, extMo,
				"Remove external managed object OBJECT_B", true);
	}

	/**
	 * Ensure can remove the connected {@link ExternalManagedObjectModel} from
	 * the {@link DeskModel}.
	 */
	public void testRemoveExternalManagedObjectWithConnections() {
		ExternalManagedObjectModel extMo = this.model
				.getExternalManagedObjects().get(0);
		Change<ExternalManagedObjectModel> change = this.operations
				.removeExternalManagedObject(extMo);
		this.assertChange(change, extMo,
				"Remove external managed object OBJECT", true);
	}

}