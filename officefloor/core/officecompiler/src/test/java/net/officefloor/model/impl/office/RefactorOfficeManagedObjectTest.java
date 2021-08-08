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

package net.officefloor.model.impl.office;

import net.officefloor.model.change.Change;
import net.officefloor.model.office.OfficeManagedObjectModel;
import net.officefloor.model.office.TypeQualificationModel;

/**
 * Refactors the {@link OfficeManagedObjectModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RefactorOfficeManagedObjectTest extends AbstractOfficeChangesTestCase {

	/**
	 * Ensure can add {@link TypeQualificationModel}.
	 */
	public void testAddTypeQualification() {
		OfficeManagedObjectModel officeMo = this.model.getOfficeManagedObjects().get(0);
		Change<TypeQualificationModel> change = this.operations.addOfficeManagedObjectTypeQualification(officeMo,
				"QUALIFIER", "TYPE");
		this.assertChange(change, change.getTarget(), "Add Managed Object Type Qualification", true);
	}

	/**
	 * Ensure can remove {@link TypeQualificationModel}.
	 */
	public void testRemoveTypeQualification() {
		OfficeManagedObjectModel officeMos = this.model.getOfficeManagedObjects().get(0);
		TypeQualificationModel typeQualification = officeMos.getTypeQualifications().get(0);
		Change<TypeQualificationModel> change = this.operations
				.removeOfficeManagedObjectTypeQualification(typeQualification);
		this.assertChange(change, typeQualification, "Remove Managed Object Type Qualification", true);
	}

}
