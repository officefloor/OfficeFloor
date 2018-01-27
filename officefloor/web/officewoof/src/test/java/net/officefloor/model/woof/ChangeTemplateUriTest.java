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
 * Tests changing the {@link WoofTemplateModel} URI.
 * 
 * @author Daniel Sagenschneider
 */
public class ChangeTemplateUriTest extends AbstractWoofChangesTestCase {

	/**
	 * {@link WoofTemplateModel}.
	 */
	private WoofTemplateModel template;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.template = this.model.getWoofTemplates().get(0);
	}

	/**
	 * Ensure able make no change to URI.
	 */
	public void testNotChangeUri() {

		// Test
		this.replayMockObjects();

		// Change template to unique URI
		Change<WoofTemplateModel> change = this.operations.changeTemplateUri(
				this.template, "path", this.getWoofTemplateChangeContext());

		// Validate the change
		this.assertChange(change, this.template, "Change Template URI", true);

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Ensure can change to unique URI.
	 */
	public void testUniqueUri() {

		// Test
		this.replayMockObjects();

		// Change template to unique URI
		Change<WoofTemplateModel> change = this.operations.changeTemplateUri(
				this.template, "unique/uri",
				this.getWoofTemplateChangeContext());

		// Validate the change
		this.assertChange(change, this.template, "Change Template URI", true);

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Ensure can change to non-unique URI.
	 */
	public void testNonUniqueUri() {

		// Test
		this.replayMockObjects();

		// Change template to unique URI
		Change<WoofTemplateModel> change = this.operations.changeTemplateUri(
				this.template, "same/uri", this.getWoofTemplateChangeContext());

		// Validate the change
		this.assertChange(change, this.template, "Change Template URI", true);

		// Verify
		this.verifyMockObjects();
	}

}