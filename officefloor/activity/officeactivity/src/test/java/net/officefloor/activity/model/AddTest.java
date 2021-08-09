/*-
 * #%L
 * Activity
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.activity.model;

import java.io.IOException;
import java.sql.SQLException;

import javax.sql.DataSource;

import net.officefloor.activity.procedure.ProcedureType;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionType;
import net.officefloor.model.change.Change;

/**
 * Tests adding to a {@link ActivityModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class AddTest extends AbstractActivityChangesTestCase {

	/**
	 * Ensure able to add {@link ActivityInputModel}.
	 */
	public void testAddInput() {

		// Add the input
		Change<ActivityInputModel> change = this.operations.addInput("INPUT", String.class.getName());

		// Validate the change
		this.assertChange(change, null, "Add Input", true);

		// Ensure appropriately added
		change.apply();
		ActivityInputModel input = this.model.getActivityInputs().get(0);
		assertSame("Incorrect input", input, change.getTarget());
	}

	/**
	 * Ensure not able to add clashing {@link ActivityInputModel} names.
	 */
	public void testAddMultipleInputs() {

		// Add the inputs
		this.operations.addInput("INPUT", Integer.class.getName()).apply();
		this.operations.addInput("INPUT", String.class.getName()).apply();

		// Ensure appropriately added sections
		this.validateModel();
	}

	/**
	 * Ensure able to add {@link ActivitySectionModel}.
	 */
	public void testAddSection() {

		// Create the section type
		SectionType section = this.constructSectionType((context) -> {
			context.addSectionInput("INPUT_A", Integer.class);
			context.addSectionInput("INPUT_B", Long.class);
			context.addSectionInput("INPUT_C", null);
			context.addSectionInput("INPUT_D", null);
			context.addSectionOutput("OUTPUT_1", String.class, false);
			context.addSectionOutput("OUTPUT_2", null, false);
			context.addSectionOutput("NOT_INCLUDE_ESCALTION", IOException.class, true);
			context.addSectionObject("IGNORE_OBJECT", DataSource.class, null);
		});

		// Create the properties
		PropertyList properties = OfficeFloorCompiler.newPropertyList();
		properties.addProperty("name.one").setValue("value.one");
		properties.addProperty("name.two").setValue("value.two");

		// Add the section
		Change<ActivitySectionModel> change = this.operations.addSection("SECTION", "net.example.ExampleSectionSource",
				"SECTION_LOCATION", properties, section);
		change.getTarget().setX(100);
		change.getTarget().setY(101);

		// Validate change
		this.assertChange(change, null, "Add Section", true);

		// Ensure appropriately added section
		change.apply();
		ActivitySectionModel activitySection = this.model.getActivitySections().get(0);
		assertSame("Incorrect section", activitySection, change.getTarget());
	}

	/**
	 * Ensure able to add multiple sections with clashing names.
	 */
	public void testAddMultipleSections() {

		// Create the section type
		SectionType section = this.constructSectionType(null);

		// Add the sections
		this.operations.addSection("SECTION", "Section1", "Location1", null, section).apply();
		this.operations.addSection("SECTION", "Section2", "Location2", null, section).apply();

		// Ensure appropriately added sections
		this.validateModel();
	}

	/**
	 * Ensure able to add {@link ActivityProcedureModel}.
	 */
	public void testAddProcedure() {

		// Create the procedure type
		ProcedureType procedure = this.constructProcedureType("procedure", String.class, (context) -> {
			context.addFlowType("OUTPUT_A", String.class);
			context.addFlowType("OUTPUT_B", null);
			context.setNextArgumentType(Short.class);
		});

		// Create the properties
		PropertyList properties = OfficeFloorCompiler.newPropertyList();
		properties.addProperty("name.one").setValue("value.one");
		properties.addProperty("name.two").setValue("value.two");

		// Add the procedure
		Change<ActivityProcedureModel> change = this.operations.addProcedure("PROCEDURE", "resource", "Class",
				"procedure", properties, procedure);

		// Validate change
		this.assertChange(change, null, "Add Procedure", true);

		// Ensure appropriately added procedure
		change.apply();
		ActivityProcedureModel activityProcedure = this.model.getActivityProcedures().get(0);
		assertSame("Incorrect section", activityProcedure, change.getTarget());
	}

	/**
	 * Ensure able to add multiple procedures with clashing names.
	 */
	public void testAddMultipleProcedures() {

		// Create the procedure type
		ProcedureType procedure = this.constructProcedureType("procedure", null, null);

		// Add the procedures
		this.operations.addProcedure("PROCEDURE", "resource1", "Class", "method", null, procedure).apply();
		this.operations.addProcedure("PROCEDURE", "resource2", "JavaScript", "function", null, procedure).apply();

		// Ensure appropriately added sections
		this.validateModel();
	}

	/**
	 * Ensure able to add {@link ActivityExceptionModel}.
	 */
	public void testAddException() {

		// Validate add exception
		Change<ActivityExceptionModel> change = this.operations.addException(Exception.class.getName());
		change.getTarget().setX(100);
		change.getTarget().setY(101);

		// Validate change
		this.assertChange(change, null, "Add Exception", true);

		// Ensure appropriately added exception
		change.apply();
		ActivityExceptionModel exception = this.model.getActivityExceptions().get(0);
		assertSame("Incorrect exception", exception, change.getTarget());
	}

	/**
	 * Ensure able to add multiple {@link ActivityExceptionModel} instances with
	 * clashing classes.
	 */
	public void testAddMultipleExceptions() {

		// Add the exception
		this.operations.addException(SQLException.class.getName()).apply();
		this.validateModel();

		// Ensure can not add exception again
		Change<ActivityExceptionModel> change = this.operations.addException(SQLException.class.getName());
		assertFalse("Should not be able to add same exception", change.canApply());

		// Ensure not add
		change.apply();
		this.validateModel();
	}

	/**
	 * Ensure able to add {@link ActivityOutputModel}.
	 */
	public void testAddOutput() {

		// Add the output
		Change<ActivityOutputModel> change = this.operations.addOutput("OUTPUT", String.class.getName());

		// Validate the change
		this.assertChange(change, null, "Add Output", true);

		// Ensure appropriately added
		change.apply();
		ActivityOutputModel output = this.model.getActivityOutputs().get(0);
		assertSame("Incorrect output", output, change.getTarget());
	}

	/**
	 * Ensure not able to add clashing {@link ActivityOutputModel} names.
	 */
	public void testAddMultipleOutputs() {

		// Add the outputs
		this.operations.addOutput("OUTPUT", String.class.getName()).apply();
		this.operations.addOutput("OUTPUT", Integer.class.getName()).apply();

		// Ensure appropriately added sections
		this.validateModel();
	}

}
