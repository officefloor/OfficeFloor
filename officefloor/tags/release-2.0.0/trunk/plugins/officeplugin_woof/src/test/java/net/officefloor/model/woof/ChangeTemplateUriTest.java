/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

		// Change template to unique URI
		Change<WoofTemplateModel> change = this.operations.changeTemplateUri(
				this.template, "path");

		// Validate the change
		this.assertChange(change, this.template, "Change Template URI", true);
	}

	/**
	 * Ensure can change to unique URI.
	 */
	public void testUniqueUri() {

		// Change template to unique URI
		Change<WoofTemplateModel> change = this.operations.changeTemplateUri(
				this.template, "unique/uri");

		// Validate the change
		this.assertChange(change, this.template, "Change Template URI", true);
	}

	/**
	 * Ensure can change to non-unique URI.
	 */
	public void testNonUniqueUri() {

		// Change template to unique URI
		Change<WoofTemplateModel> change = this.operations.changeTemplateUri(
				this.template, "same/uri");

		// Validate the change
		this.assertChange(change, this.template, "Change Template URI", true);
	}

	/**
	 * Ensure can clear URI.
	 */
	public void testClearUri() {

		// Change template to unique URI
		Change<WoofTemplateModel> change = this.operations.changeTemplateUri(
				this.template, null);

		// Validate the change
		this.assertChange(change, this.template, "Change Template URI", true);
	}

}