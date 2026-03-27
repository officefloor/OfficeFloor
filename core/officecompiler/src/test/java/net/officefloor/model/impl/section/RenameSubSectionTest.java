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

import net.officefloor.model.change.Change;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.SubSectionModel;

/**
 * Tests renaming the {@link SubSectionModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RenameSubSectionTest extends AbstractSectionChangesTestCase {

	/**
	 * Ensures handles {@link SubSectionModel} not being in the
	 * {@link SectionModel}.
	 */
	public void testRenameSubSectionNotInSection() {
		SubSectionModel subSection = new SubSectionModel("NOT_IN_SECTION",
				null, null);
		Change<SubSectionModel> change = this.operations.renameSubSection(
				subSection, "NEW_NAME");
		this.assertChange(change, subSection,
				"Rename sub section NOT_IN_SECTION to NEW_NAME", false,
				"Sub section NOT_IN_SECTION not in section");
	}

	/**
	 * Ensure can rename the {@link SubSectionModel}.
	 */
	public void testRenameSubSection() {
		SubSectionModel subSection = this.model.getSubSections().get(0);
		Change<SubSectionModel> change = this.operations.renameSubSection(
				subSection, "NEW_NAME");
		this.assertChange(change, subSection,
				"Rename sub section OLD_NAME to NEW_NAME", true);
	}

	/**
	 * Ensures on renaming the {@link SubSectionModel} that order is maintained.
	 */
	public void testRenameSubSectionCausingSubSectionOrderChange() {
		this.useTestSetupModel();
		SubSectionModel subSection = this.model.getSubSections().get(0);
		Change<SubSectionModel> change = this.operations.renameSubSection(
				subSection, "SUB_SECTION_C");
		this.assertChange(change, subSection,
				"Rename sub section SUB_SECTION_A to SUB_SECTION_C", true);
	}

}
