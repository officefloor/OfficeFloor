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
package net.officefloor.woof.model.woof;

import net.officefloor.model.change.Change;
import net.officefloor.woof.model.woof.WoofResourceModel;

/**
 * Tests changing the {@link WoofResourceModel} resource path.
 * 
 * @author Daniel Sagenschneider
 */
public class ChangeResourcePathTest extends AbstractWoofChangesTestCase {

	/**
	 * {@link WoofResourceModel}.
	 */
	private WoofResourceModel resource;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.resource = this.model.getWoofResources().get(0);
	}

	/**
	 * Ensure able make no change to resource path.
	 */
	public void testNotChangeResourcePath() {

		// Change resource path to be same
		Change<WoofResourceModel> change = this.operations.changeResourcePath(
				this.resource, "path");

		// Validate the change
		this.assertChange(change, this.resource, "Change Resource Path", true);
	}

	/**
	 * Ensure can change to unique resource path.
	 */
	public void testUniqueResourcePath() {

		// Change template to unique path
		Change<WoofResourceModel> change = this.operations.changeResourcePath(
				this.resource, "unique/path");

		// Validate the change
		this.assertChange(change, this.resource, "Change Resource Path", true);
	}

	/**
	 * Ensure can change to non-unique resource path.
	 */
	public void testNonUniqueResourcePath() {

		// Change template to non-unique path
		Change<WoofResourceModel> change = this.operations.changeResourcePath(
				this.resource, "same/path");

		// Validate the change
		this.assertChange(change, this.resource, "Change Resource Path", true);
	}

	/**
	 * Ensure no change if attempt to clear resource path.
	 */
	public void testClearResourcePath() {

		// Change to attempting to clear resource path
		Change<WoofResourceModel> change = this.operations.changeResourcePath(
				this.resource, null);

		// Validate the change
		this.assertChange(change, this.resource, "Change Resource Path", true,
				"Must provide resource path");
	}

}