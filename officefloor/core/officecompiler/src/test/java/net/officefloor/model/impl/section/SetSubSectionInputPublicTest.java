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
import net.officefloor.model.section.SubSectionInputModel;
import net.officefloor.model.section.SubSectionModel;

/**
 * Tests setting the {@link SubSectionInputModel} private/public.
 * 
 * @author Daniel Sagenschneider
 */
public class SetSubSectionInputPublicTest extends
		AbstractSectionChangesTestCase {

	/**
	 * Private {@link SubSectionInputModel}.
	 */
	private SubSectionInputModel inputPrivate;

	/**
	 * Public {@link SubSectionInputModel}.
	 */
	private SubSectionInputModel inputPublic;

	/**
	 * Public {@link SubSectionInputModel} with public name.
	 */
	private SubSectionInputModel inputPublicWithName;

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.model.impl.AbstractOperationsTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Obtain the setup inputs
		SubSectionModel subSection = this.model.getSubSections().get(0);
		this.inputPrivate = subSection.getSubSectionInputs().get(0);
		this.inputPublic = subSection.getSubSectionInputs().get(1);
		this.inputPublicWithName = subSection.getSubSectionInputs().get(2);
	}

	/**
	 * Ensure no change if the {@link SubSectionModel} not in the
	 * {@link SectionModel}.
	 */
	public void testSubSectionInputNotInSection() {
		SubSectionInputModel input = new SubSectionInputModel("NOT_IN_SECTION",
				Object.class.getName(), false, null);
		Change<SubSectionInputModel> change = this.operations
				.setSubSectionInputPublic(true, null, input);
		this.assertChange(change, input,
				"Set sub section input NOT_IN_SECTION public", false,
				"Sub section input NOT_IN_SECTION not in section");
	}

	/**
	 * Ensures can set a {@link SubSectionInputModel} to be public.
	 */
	public void testSetSubSectionInputPublic() {
		Change<SubSectionInputModel> changeA = this.operations
				.setSubSectionInputPublic(true, "PUBLIC_NAME",
						this.inputPrivate);
		Change<SubSectionInputModel> changeB = this.operations
				.setSubSectionInputPublic(true, null, this.inputPublicWithName);
		this.assertChanges(changeA, changeB);
	}

	/**
	 * Ensures can set a {@link SubSectionInputModel} to be private.
	 */
	public void testSetSubSectionInputPrivate() {
		Change<SubSectionInputModel> changeA = this.operations
				.setSubSectionInputPublic(false, null, this.inputPublic);
		Change<SubSectionInputModel> changeB = this.operations
				.setSubSectionInputPublic(false, "IGNORED",
						this.inputPublicWithName);
		this.assertChanges(changeA, changeB);
	}
}
