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

package net.officefloor.model.impl.section;

import java.sql.Connection;

import net.officefloor.model.change.Change;
import net.officefloor.model.section.ExternalManagedObjectModel;

/**
 * Tests adding an {@link ExternalManagedObjectModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class AddExternalManagedObjectTest extends
		AbstractSectionChangesTestCase {

	/**
	 * Ensures that can add an {@link ExternalManagedObjectModel}.
	 */
	public void testAddExternalManagedObject() {
		Change<ExternalManagedObjectModel> change = this.operations
				.addExternalManagedObject("OBJECT", Connection.class.getName());
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
		Change<ExternalManagedObjectModel> changeB = this.operations
				.addExternalManagedObject("OBJECT_B", Integer.class.getName());
		Change<ExternalManagedObjectModel> changeA = this.operations
				.addExternalManagedObject("OBJECT_A", String.class.getName());
		Change<ExternalManagedObjectModel> changeC = this.operations
				.addExternalManagedObject("OBJECT_C", Connection.class
						.getName());
		this.assertChanges(changeB, changeA, changeC);
	}

}
