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

import net.officefloor.model.ConnectionModel;
import net.officefloor.model.change.Change;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.SubSectionModel;

/**
 * Tests removing the {@link SubSectionModel}.
 * 
 * @author Daniel
 */
public class RemoveSubSectionTest extends AbstractSectionOperationsTestCase {

	/**
	 * Use specific setup file.
	 */
	public RemoveSubSectionTest() {
		super(true);
	}

	/**
	 * Ensure no change if {@link SubSectionModel} not in the
	 * {@link SectionModel}.
	 */
	public void testRemoveSubSectionNotInSection() {
		SubSectionModel subSection = new SubSectionModel("NOT_IN_SECTION",
				"net.example.ExampleSectionSource", "LOCATION");
		Change<SubSectionModel> change = this.operations
				.removeSubSection(subSection);
		this.assertChange(change, subSection,
				"Remove sub section NOT_IN_SECTION", false,
				"Sub section NOT_IN_SECTION not in section");
	}

	/**
	 * Ensure can remove {@link SubSectionModel} when other
	 * {@link SubSectionModel} instances.
	 */
	public void testRemoveSubSectionWhenOtherSubSections() {
		SubSectionModel subSection = this.model.getSubSections().get(1);
		Change<SubSectionModel> change = this.operations
				.removeSubSection(subSection);
		this.assertChange(change, subSection,
				"Remove sub section SUB_SECTION_B", true);
	}

	/**
	 * Ensure can remove {@link SubSectionModel} with {@link ConnectionModel}
	 * instances.
	 */
	public void testRemoveSubSectionWithConnections() {
		SubSectionModel subSection = this.model.getSubSections().get(0);
		Change<SubSectionModel> change = this.operations
				.removeSubSection(subSection);
		this.assertChange(change, subSection,
				"Remove sub section SUB_SECTION_A", true);
	}

}