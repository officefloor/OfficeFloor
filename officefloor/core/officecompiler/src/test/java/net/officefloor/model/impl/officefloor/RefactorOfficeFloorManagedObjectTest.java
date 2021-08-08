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

package net.officefloor.model.impl.officefloor;

import net.officefloor.model.change.Change;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectModel;
import net.officefloor.model.officefloor.TypeQualificationModel;

/**
 * Refactors the {@link OfficeFloorManagedObjectModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RefactorOfficeFloorManagedObjectTest extends AbstractOfficeFloorChangesTestCase {

	/**
	 * Ensure can add {@link TypeQualificationModel}.
	 */
	public void testAddTypeQualification() {
		OfficeFloorManagedObjectModel officeMo = this.model.getOfficeFloorManagedObjects().get(0);
		Change<TypeQualificationModel> change = this.operations.addOfficeFloorManagedObjectTypeQualification(officeMo,
				"QUALIFIER", "TYPE");
		this.assertChange(change, change.getTarget(), "Add Managed Object Type Qualification", true);
	}

	/**
	 * Ensure can remove {@link TypeQualificationModel}.
	 */
	public void testRemoveTypeQualification() {
		OfficeFloorManagedObjectModel officeMo = this.model.getOfficeFloorManagedObjects().get(0);
		TypeQualificationModel typeQualification = officeMo.getTypeQualifications().get(0);
		Change<TypeQualificationModel> change = this.operations
				.removeOfficeFloorManagedObjectTypeQualification(typeQualification);
		this.assertChange(change, typeQualification, "Remove Managed Object Type Qualification", true);
	}

}
