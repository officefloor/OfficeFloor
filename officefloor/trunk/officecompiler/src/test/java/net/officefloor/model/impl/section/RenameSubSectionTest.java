/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.model.impl.section;

import net.officefloor.model.change.Change;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.SubSectionModel;

/**
 * Tests renaming the {@link SubSectionModel}.
 * 
 * @author Daniel
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