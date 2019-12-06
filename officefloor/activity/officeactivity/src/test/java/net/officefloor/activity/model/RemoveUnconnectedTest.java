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
 * Tests removing from a {@link ActivityModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RemoveUnconnectedTest extends AbstractActivityChangesTestCase {

	/**
	 * Ensure able to remove the {@link ActivityInputModel}.
	 */
	public void testRemoveInput() {

		// Obtain the Input to remove
		ActivityInputModel input = this.model.getActivityInputs().get(0);

		// Remove the Input
		Change<ActivityInputModel> change = this.operations.removeInput(input);
		this.assertChange(change, input, "Remove input INPUT", true);
	}

	/**
	 * Ensure able to remove the {@link ActivityProcedureModel}.
	 */
	public void testRemoveProcedure() {

		// Obtain the procedure to remove
		ActivityProcedureModel procedure = this.model.getActivityProcedures().get(0);

		// Remove the procedure
		Change<ActivityProcedureModel> change = this.operations.removeProcedure(procedure);
		this.assertChange(change, procedure, "Remove procedure PROCEDURE", true);
	}

	/**
	 * Ensure able to remove the {@link ActivitySectionModel}.
	 */
	public void testRemoveSection() {

		// Obtain the section to remove
		ActivitySectionModel section = this.model.getActivitySections().get(0);

		// Remove the section
		Change<ActivitySectionModel> change = this.operations.removeSection(section);
		this.assertChange(change, section, "Remove section SECTION", true);
	}

	/**
	 * Ensure able to remove the {@link ActivityExceptionModel}.
	 */
	public void testRemoveException() {

		// Obtain the exception to remove
		ActivityExceptionModel exception = this.model.getActivityExceptions().get(0);

		// Remove the exception
		Change<ActivityExceptionModel> change = this.operations.removeException(exception);
		this.assertChange(change, exception, "Remove exception java.lang.Exception", true);
	}

	/**
	 * Ensure able to remove the {@link ActivityOutputModel}.
	 */
	public void testRemoveOutput() {

		// Obtain the output to remove
		ActivityOutputModel output = this.model.getActivityOutputs().get(0);

		// Remove the output
		Change<ActivityOutputModel> change = this.operations.removeOutput(output);
		this.assertChange(change, output, "Remove output OUTPUT", true);
	}

}