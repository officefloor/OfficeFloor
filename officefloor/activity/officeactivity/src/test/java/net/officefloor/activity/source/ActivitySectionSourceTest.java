/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
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
package net.officefloor.activity.source;

import java.sql.SQLException;

import net.officefloor.activity.ActivitySectionSource;
import net.officefloor.activity.impl.procedure.ClassProcedureSource;
import net.officefloor.activity.procedure.Procedure;
import net.officefloor.activity.procedure.build.ProcedureArchitect;
import net.officefloor.activity.procedure.build.ProcedureEmployer;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeEscalation;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.managedobject.singleton.Singleton;

/**
 * Tests the {@link ActivitySectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ActivitySectionSourceTest extends OfficeFrameTestCase {

	/**
	 * Ensure can execute {@link Procedure}.
	 */
	public void testProcedure() throws Throwable {

		// Compile in the activity
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		compiler.office((context) -> context.getOfficeArchitect().addOfficeSection("ACTIVITY",
				ActivitySectionSource.class.getName(), this.getLocation("Procedure.activity.xml")));

		// Undertake test
		try (OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor()) {
			ExampleProcedure.isProcedureRun = false;
			CompileOfficeFloor.invokeProcess(officeFloor, "ACTIVITY.PROCEDURE.procedure", null);
			assertTrue("Procedure should be executed", ExampleProcedure.isProcedureRun);
		}
	}

	/**
	 * Ensure can wire in {@link SectionInput} and {@link SectionOutput}.
	 */
	public void testInputThroughToOutput() throws Throwable {

		// Compile the activity
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		compiler.office((context) -> {
			OfficeArchitect officeArchitect = context.getOfficeArchitect();

			// Add the input and output procedures
			ProcedureArchitect<OfficeSection> procedureArchitect = ProcedureEmployer
					.employProcedureArchitect(officeArchitect, context.getOfficeSourceContext());
			OfficeSection inputProcedure = procedureArchitect.addProcedure("INPUT", ExampleProcedure.class.getName(),
					ClassProcedureSource.SOURCE_NAME, "passThrough", true, null);
			OfficeSection outputProcedure = procedureArchitect.addProcedure("OUTPUT", ExampleProcedure.class.getName(),
					ClassProcedureSource.SOURCE_NAME, "result", false, null);

			// Add the activity and link input / output
			OfficeSection activity = officeArchitect.addOfficeSection("ACTIVITY", ActivitySectionSource.class.getName(),
					this.getLocation("InputToOutput.activity.xml"));

			// Link input through to output
			officeArchitect.link(inputProcedure.getOfficeSectionOutput(ProcedureArchitect.NEXT_OUTPUT_NAME),
					activity.getOfficeSectionInput("INPUT"));
			officeArchitect.link(activity.getOfficeSectionOutput("OUTPUT"),
					outputProcedure.getOfficeSectionInput(ProcedureArchitect.INPUT_NAME));
		});

		// Undertake test
		try (OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor()) {
			ExampleProcedure.result = null;
			final String ARGUMENT = "TEST";
			CompileOfficeFloor.invokeProcess(officeFloor, "INPUT.procedure", ARGUMENT);
			assertSame("Should pass through value in/out of activity", ARGUMENT, ExampleProcedure.result);
		}
	}

	/**
	 * Ensure can auto-wire in the necessary objects.
	 */
	public void testInjectObject() throws Throwable {

		final String OBJECT = "TEST";

		// Compile the activity
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		compiler.office((context) -> {

			// Provide object for auto-wiring
			Singleton.load(context.getOfficeArchitect(), OBJECT);

			// Activity requiring object
			context.getOfficeArchitect().addOfficeSection("ACTIVITY", ActivitySectionSource.class.getName(),
					this.getLocation("InjectObject.activity.xml"));
		});

		// Undertake test
		try (OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor()) {
			ExampleProcedure.result = null;
			CompileOfficeFloor.invokeProcess(officeFloor, "ACTIVITY.INJECT_OBJECT.procedure", null);
			assertSame("Should inject the object", OBJECT, ExampleProcedure.result);
		}
	}

	/**
	 * Ensure can handle {@link Escalation} within {@link ActivitySectionSource}.
	 */
	public void testHandleEscalation() throws Throwable {

		final SQLException escalation = new SQLException("TEST");

		// Compile the activity
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		compiler.office((context) -> {

			// Activity
			context.getOfficeArchitect().addOfficeSection("ACTIVITY", ActivitySectionSource.class.getName(),
					this.getLocation("HandleEscalation.activity.xml"));
		});

		// Undertake test
		try (OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor()) {
			ExampleProcedure.failure = null;
			CompileOfficeFloor.invokeProcess(officeFloor, "ACTIVITY.PROPAGATE.procedure", escalation);
			assertSame("Should handle escalation within activity", escalation, ExampleProcedure.failure);
		}
	}

	/**
	 * Ensure can propagate the {@link Escalation} to {@link Office}.
	 */
	public void testPropagateEscalation() throws Throwable {

		final SQLException failure = new SQLException("TEST");

		// Compile the activity
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		compiler.office((context) -> {
			OfficeArchitect officeArchitect = context.getOfficeArchitect();

			// Activity
			officeArchitect.addOfficeSection("ACTIVITY", ActivitySectionSource.class.getName(),
					this.getLocation("Propagate.activity.xml"));

			// Handle escalation by office
			ProcedureArchitect<OfficeSection> procedureArchitect = ProcedureEmployer
					.employProcedureArchitect(officeArchitect, context.getOfficeSourceContext());
			OfficeSection handler = procedureArchitect.addProcedure("HANDLER", ExampleProcedure.class.getName(),
					ClassProcedureSource.SOURCE_NAME, "handleEscalation", false, null);
			OfficeEscalation escalation = officeArchitect.addOfficeEscalation(SQLException.class.getName());
			officeArchitect.link(escalation, handler.getOfficeSectionInput(ProcedureArchitect.INPUT_NAME));
		});

		// Undertake test
		try (OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor()) {
			ExampleProcedure.failure = null;
			CompileOfficeFloor.invokeProcess(officeFloor, "ACTIVITY.PROPAGATE.procedure", failure);
			assertSame("Should handle escalation within activity", failure, ExampleProcedure.failure);
		}
	}

	/**
	 * Obtains the location of the {@link ConfigurationItem}.
	 * 
	 * @param fileName Name of configuration file.
	 * @return Location of the {@link ConfigurationItem}.
	 */
	private String getLocation(String fileName) {
		return this.getClass().getPackage().getName().replace('.', '/') + "/" + fileName;
	}

}