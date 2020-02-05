package net.officefloor.compile.integrate.office;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeEscalation;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.managedobject.singleton.Singleton;

/**
 * Ensures order of execution (objects, functions, then escalations).
 * 
 * @author Daniel Sagenschneider
 */
public class ExecutionOrderTest extends OfficeFrameTestCase {

	/**
	 * Ensure order.
	 */
	public void testOrder() throws Exception {
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		Closure<Boolean> isObjectsExplored = new Closure<>(false);
		Closure<Boolean> isFunctionsExplored = new Closure<>(false);
		Closure<Boolean> isEscalationsExplored = new Closure<>(false);
		compiler.office((context) -> {
			// Add out of order to confirm order
			OfficeArchitect office = context.getOfficeArchitect();

			// Add escalation (to be last)
			OfficeEscalation escalation = office.addOfficeEscalation(Exception.class.getName());
			office.link(escalation, context.addSection("HANDLER", MockSection.class).getOfficeSectionInput("service"));
			office.addOfficeEscalationExplorer((explore) -> {
				assertTrue("Objects explored before escalations", isObjectsExplored.value);
				assertTrue("Functions explore before escalations", isFunctionsExplored.value);
				isEscalationsExplored.value = true;
			});

			// Add objects (to be first)
			OfficeManagedObject managedObject = Singleton.load(office, "OBJECT");
			managedObject.addExecutionExplorer((explore) -> {
				assertFalse("Functions explored after objects", isFunctionsExplored.value);
				assertFalse("Escalations explored after objects", isEscalationsExplored.value);
				isObjectsExplored.value = true;
			});

			// Add functions (to be second)
			OfficeSectionInput input = context.addSection("INPUT", MockSection.class).getOfficeSectionInput("service");
			input.addExecutionExplorer((explore) -> {
				assertTrue("Objects explored before functions", isObjectsExplored.value);
				assertFalse("Escalations explored after functions", isEscalationsExplored.value);
				isFunctionsExplored.value = true;
			});
		});
		try (OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor()) {
			assertTrue("Should execute escalations (and subsequently objects and functions)",
					isEscalationsExplored.value);
		}
	}

	public static class MockSection {
		public void service() {
			// no operation
		}
	}
}