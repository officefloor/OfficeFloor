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
package net.officefloor.compile.integrate.escalation;

import java.io.IOException;

import net.officefloor.autowire.AutoWireApplication;
import net.officefloor.autowire.AutoWireOfficeFloor;
import net.officefloor.autowire.impl.AutoWireOfficeFloorSource;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.escalate.EscalationHandler;
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

		AutoWireApplication source = new AutoWireOfficeFloorSource();

		// Add escalation handler
		MockEscalationHandler handler = new MockEscalationHandler();
		source.getOfficeFloorCompiler().setEscalationHandler(handler);

		// Add the failing service
		source.addSection("FAILURE", ClassSectionSource.class.getName(), FailingService.class.getName());

		// Open OfficeFloor
		AutoWireOfficeFloor officeFloor = source.openOfficeFloor();

		// Ensure handled escalation
		IOException failure = new IOException("TEST");
		officeFloor.invokeFunction("FAILURE.NAMESPACE.service", failure);
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