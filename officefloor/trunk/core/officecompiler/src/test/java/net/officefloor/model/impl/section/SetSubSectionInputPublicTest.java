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