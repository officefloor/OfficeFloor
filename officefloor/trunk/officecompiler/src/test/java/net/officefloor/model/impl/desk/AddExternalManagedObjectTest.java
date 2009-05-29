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
package net.officefloor.model.impl.desk;

import net.officefloor.model.change.Change;
import net.officefloor.model.desk.ExternalManagedObjectModel;

/**
 * Tests adding an {@link ExternalManagedObjectModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class AddExternalManagedObjectTest extends
		AbstractDeskChangesTestCase {

	/**
	 * Ensures that can add an {@link ExternalManagedObjectModel}.
	 */
	public void testAddExternalManagedObject() {
		Change<ExternalManagedObjectModel> change = this.operations
				.addExternalManagedObject("OBJECT", String.class.getName());
		this.assertChange(change, null, "Add external managed object OBJECT",
				true);
		change.apply();
		assertEquals("Incorrect target", this.model.getExternalManagedObjects()
				.get(0), change.getTarget());
	}

	/**
	 * Ensure ordering of {@link ExternalManagedObjectModel} instances.
	 */
	public void testAddMultipleExternalManagedObjectsEnsuringOrdering() {

		// Create the changes
		Change<ExternalManagedObjectModel> changeB = this.operations
				.addExternalManagedObject("OBJECT_B", Integer.class.getName());
		Change<ExternalManagedObjectModel> changeA = this.operations
				.addExternalManagedObject("OBJECT_A", String.class.getName());
		Change<ExternalManagedObjectModel> changeC = this.operations
				.addExternalManagedObject("OBJECT_C", Object.class.getName());

		// Add the external managed objects
		changeB.apply();
		changeA.apply();
		changeC.apply();
		this.validateModel();

		// Revert
		changeC.revert();
		changeA.revert();
		changeB.revert();
		this.validateAsSetupModel();
	}

}