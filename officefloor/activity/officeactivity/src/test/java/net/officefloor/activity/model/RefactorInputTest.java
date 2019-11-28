/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.activity.model;

import net.officefloor.model.change.Change;

/**
 * Tests refactoring the {@link ActivityInputModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RefactorInputTest extends AbstractActivityChangesTestCase {

	/**
	 * {@link ActivityInputModel}.
	 */
	private ActivityInputModel input;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.input = this.model.getActivityInputs().get(0);
	}

	/**
	 * Ensure handle no change.
	 */
	public void testNoChange() {

		// Refactor with same details
		Change<ActivityInputModel> change = this.operations.refactorInput(this.input, "INPUT", String.class.getName());

		// Validate change
		this.assertChange(change, null, "Refactor Input", true);
	}

	/**
	 * Ensure handle change to all details.
	 */
	public void testChange() {

		// Refactor with changes
		Change<ActivityInputModel> change = this.operations.refactorInput(this.input, "CHANGE", null);

		// Validate change
		this.assertChange(change, null, "Refactor Input", true);
	}

	/**
	 * Ensure not able to refactor to an existing {@link ActivityInputModel}.
	 */
	public void testInputAlreadyExists() {

		// Change to a unique input
		Change<ActivityInputModel> change = this.operations.refactorInput(this.input, "EXISTS", null);

		// Validate no change (as input is already handled)
		this.assertChange(change, this.input, "Refactor Input", false, "Input already exists for 'EXISTS'");
	}

}