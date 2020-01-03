package net.officefloor.compile.integrate.escalation;

import java.io.IOException;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.extension.CompileOffice;
import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.Parameter;

/**
 * Tests integration of {@link EscalationHandler} being provided by
 * {@link OfficeFloorCompiler} to the {@link OfficeFrame}.
 * 
 * @author Daniel Sagenschneider
 */
public class EscalationHandlerIntegrationTest extends OfficeFrameTestCase {

	/**
	 * Ensure able to override the {@link EscalationHandler} for the
	 * {@link OfficeFrame}.
	 */
	public void testHandleEscalation() throws Exception {

		// Create compiler
		CompileOffice compile = new CompileOffice();

		// Add escalation handler
		MockEscalationHandler handler = new MockEscalationHandler();
		compile.getOfficeFloorCompiler().setEscalationHandler(handler);

		// Open OfficeFloor
		OfficeFloor officeFloor = compile.compileAndOpenOffice((architect, context) -> {

			// Add the failing service
			architect.addOfficeSection("FAILURE", ClassSectionSource.class.getName(), FailingService.class.getName());
		});

		// Ensure handled escalation
		IOException failure = new IOException("TEST");
		officeFloor.getOffice("OFFICE").getFunctionManager("FAILURE.service").invokeProcess(failure, null);
		assertSame("Failure should be handled by overridden escalation handler", failure, handler.escalation);

		// Close OfficeFloor
		officeFloor.closeOfficeFloor();
	}

	/**
	 * Mock {@link EscalationHandler}.
	 */
	private static class MockEscalationHandler implements EscalationHandler {

		/**
		 * {@link Escalation}.
		 */
		private Throwable escalation = null;

		/*
		 * =================== EscalationHandler ===================
		 */

		@Override
		public void handleEscalation(Throwable escalation) throws Throwable {
			this.escalation = escalation;
		}
	}

	/**
	 * Failing service.
	 */
	public static class FailingService {

		public void service(@Parameter IOException failure) throws IOException {
			throw failure;
		}
	}

}