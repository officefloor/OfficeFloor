package net.officefloor.compile.integrate.escalation;

import java.io.IOException;
import java.sql.SQLException;

import net.officefloor.compile.integrate.AbstractCompileTestCase;
import net.officefloor.compile.spi.office.OfficeEscalation;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.plugin.managedfunction.clazz.ClassManagedFunctionSource;

/**
 * Tests compiling the {@link Office} {@link Escalation}.
 * 
 * @author Daniel Sagenschneider
 */
public class CompileOfficeEscalationTest extends AbstractCompileTestCase {

	/**
	 * Test an {@link OfficeEscalation}.
	 */
	public void testSimpleEscalation() {

		// Record creating section type
		this.issues.recordCaptureIssues(false);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeBuilder_addFunction("SECTION", "INPUT").linkParameter(0, Throwable.class);
		this.record_officeBuilder_addEscalation(Exception.class, "SECTION.INPUT");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Test an multiple {@link OfficeEscalation} instances.
	 */
	public void testMultipleEscalationOrdering() {

		// Record creating section type
		this.issues.recordCaptureIssues(false);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeBuilder_addFunction("SECTION", "INPUT").linkParameter(0, Throwable.class);
		this.record_officeBuilder_addEscalation(IOException.class, "SECTION.INPUT");
		this.record_officeBuilder_addEscalation(SQLException.class, "SECTION.INPUT");
		this.record_officeBuilder_addEscalation(Exception.class, "SECTION.INPUT");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Class for {@link ClassManagedFunctionSource}.
	 */
	public static class EscalationClass {
		public void handle(Throwable parameter) {
		}
	}

}