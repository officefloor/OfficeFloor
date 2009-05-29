/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.model.impl.section;

import net.officefloor.model.change.Change;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.section.ExternalManagedObjectModel;

/**
 * Tests removing the {@link ExternalManagedObjectModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RemoveExternalManagedObjectTest extends
		AbstractSectionChangesTestCase {

	/**
	 * Initiate to use specific setup {@link DeskModel}.
	 */
	public RemoveExternalManagedObjectTest() {
		super(true);
	}

	/**
	 * Tests attempting to remove an {@link ExternalManagedObjectModel} not in
	 * the {@link SectionModel}.
	 */
	public void testRemoveExternalManagedObjectNotInSection() {
		ExternalManagedObjectModel extMo = new ExternalManagedObjectModel(
				"NOT_IN_SECTION", null);
		Change<ExternalManagedObjectModel> change = this.operations
				.removeExternalManagedObject(extMo);
		this.assertChange(change, extMo,
				"Remove external managed object NOT_IN_SECTION", false,
				"External managed object NOT_IN_SECTION not in section");
	}

	/**
	 * Ensure can remove the {@link ExternalManagedObjectModel} from the
	 * {@link SectionModel} when other {@link ExternalManagedObjectModel}
	 * instances on the {@link SectionModel}.
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
	 * the {@link SectionModel}.
	 */
	public void testRemoveExternalManagedObjectWithConnections() {
		ExternalManagedObjectModel extMo = this.model
				.getExternalManagedObjects().get(0);
		Change<ExternalManagedObjectModel> change = this.operations
				.removeExternalManagedObject(extMo);
		this.assertChange(change, extMo, "Remove external managed object MO",
				true);
	}

}