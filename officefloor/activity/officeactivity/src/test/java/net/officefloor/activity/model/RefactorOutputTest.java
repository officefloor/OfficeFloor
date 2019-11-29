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
 * Tests refactoring the {@link ActivityOutputModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RefactorOutputTest extends AbstractActivityChangesTestCase {

	/**
	 * {@link ActivityOutputModel}.
	 */
	private ActivityOutputModel output;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.output = this.model.getActivityOutputs().get(1);
	}

	/**
	 * Ensure can refactor.
	 */
	public void testRefactor() {
		
		// Refactor template to change path
		Change<ActivityOutputModel> change = this.operations.refactorOutput(this.output, "CHANGE",
				String.class.getName());

		// Validate the change
		this.assertChange(change, this.output, "Refactor Output", true);
	}

	/**
	 * Ensure not able to refactor to an existing {@link ActivityOutputModel}.
	 */
	public void testOutputAlreadyExists() {

		// Change to a existing output
		Change<ActivityOutputModel> change = this.operations.refactorOutput(this.output, "EXISTS", null);

		// Validate the change
		this.assertChange(change, this.output, "Refactor Output", true);
	}

}