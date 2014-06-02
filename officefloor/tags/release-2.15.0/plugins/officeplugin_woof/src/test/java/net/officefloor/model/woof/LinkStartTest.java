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
 * Tests linking from the {@link WoofStartModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class LinkStartTest extends AbstractWoofChangesTestCase {

	/**
	 * Index of start A.
	 */
	private static final int A = 0;

	/**
	 * Index of start B.
	 */
	private static final int B = 1;

	/**
	 * Ensure can link to {@link WoofSectionInputModel}.
	 */
	public void testLinkToSectionInput() {
		this.doLinkToSectionInput(A);
	}

	/**
	 * Ensure link overrides other links for
	 * {@link WoofStartToWoofSectionInputModel}.
	 */
	public void testLinkOverrideToSectionInput() {
		this.doLinkToSectionInput(B);
	}

	/**
	 * Undertakes linking to a {@link WoofSectionInputModel}.
	 * 
	 * @param startIndex
	 *            {@link WoofStartModel} index.
	 */
	private void doLinkToSectionInput(int startIndex) {

		// Obtain the items to link
		WoofStartModel start = this.model.getWoofStarts().get(startIndex);
		WoofSectionInputModel sectionInput = this.model.getWoofSections()
				.get(B).getInputs().get(0);

		// Link the start to section input
		Change<WoofStartToWoofSectionInputModel> change = this.operations
				.linkStartToSectionInput(start, sectionInput);

		// Validate change
		this.assertChange(change, null, "Link Start to Section Input", true);
	}

	/**
	 * Ensure can remove the {@link WoofStartToWoofSectionInputModel}.
	 */
	public void testRemoveToSectionInputLink() {

		// Obtain the link to remove
		WoofStartToWoofSectionInputModel link = this.model.getWoofStarts()
				.get(B).getWoofSectionInput();

		// Remove the link
		Change<WoofStartToWoofSectionInputModel> change = this.operations
				.removeStartToSectionInput(link);

		// Validate change
		this.assertChange(change, null, "Remove Start to Section Input", true);
	}

}