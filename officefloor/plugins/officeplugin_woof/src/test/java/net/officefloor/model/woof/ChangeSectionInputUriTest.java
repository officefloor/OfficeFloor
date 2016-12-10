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
package net.officefloor.model.woof;

import net.officefloor.model.change.Change;

/**
 * Test changing the {@link WoofSectionInputModel} URI.
 * 
 * @author Daniel Sagenschneider
 */
public class ChangeSectionInputUriTest extends AbstractWoofChangesTestCase {

	/**
	 * Ensure able to specify the URI.
	 */
	public void testSpecifyUri() {
		WoofSectionInputModel sectionInput = this.model.getWoofSections()
				.get(0).getInputs().get(0);
		Change<WoofSectionInputModel> change = this.operations
				.changeSectionInputUri(sectionInput, "uri");
		this.assertChange(change, sectionInput, "Change Section Input URI",
				true);
	}

	/**
	 * Ensure able to change the URI.
	 */
	public void testChangeUri() {
		WoofSectionInputModel sectionInput = this.model.getWoofSections()
				.get(0).getInputs().get(1);
		Change<WoofSectionInputModel> change = this.operations
				.changeSectionInputUri(sectionInput, "change");
		this.assertChange(change, sectionInput, "Change Section Input URI",
				true);
	}

	/**
	 * Ensure able to clear the URI.
	 */
	public void testClearUri() {
		WoofSectionInputModel sectionInput = this.model.getWoofSections()
				.get(0).getInputs().get(1);
		Change<WoofSectionInputModel> change = this.operations
				.changeSectionInputUri(sectionInput, null);
		this.assertChange(change, sectionInput, "Change Section Input URI",
				true);
	}

}