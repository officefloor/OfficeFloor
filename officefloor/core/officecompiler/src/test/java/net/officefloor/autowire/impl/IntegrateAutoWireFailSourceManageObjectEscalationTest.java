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
package net.officefloor.autowire.impl;

import java.io.IOException;
import java.sql.SQLException;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireApplication;
import net.officefloor.autowire.AutoWireOfficeFloor;
import net.officefloor.autowire.AutoWireSection;
import net.officefloor.frame.api.escalate.FailedToSourceManagedObjectEscalation;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.Parameter;

/**
 * Integration tests automatically handling causes of
 * {@link FailedToSourceManagedObjectEscalation}.
 * 
 * @author Daniel Sagenschneider
 */
public class IntegrateAutoWireFailSourceManageObjectEscalationTest extends OfficeFrameTestCase {

	/**
	 * {@link AutoWireOfficeFloor}.
	 */
	private AutoWireOfficeFloor officeFloor;

	@Override
	protected void tearDown() throws Exception {
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Ensure automatically handle {@link FailedToSourceManagedObjectEscalation}
	 * for {@link IOException} cause.
	 */
	public void testHandleIOCause() throws Exception {

		// Start
		this.openOfficeFloor(false);

		// Trigger escalation
		final IOException escalation = new IOException();
		MockObject.initialiseFailure = escalation;
		Handler.cause = null;
		this.officeFloor.invokeFunction("Servicer.NAMESPACE.service", null);
		assertSame("Incorrect escalation", escalation, Handler.cause);
	}

	/**
	 * Ensure automatically handle {@link FailedToSourceManagedObjectEscalation}
	 * for {@link SQLException} cause.
	 */
	public void testHandleSQLCause() throws Exception {

		// Start
		this.openOfficeFloor(false);

		// Trigger escalation
		final SQLException escalation = new SQLException();
		MockObject.initialiseFailure = escalation;
		Handler.cause = null;
		this.officeFloor.invokeFunction("Servicer.NAMESPACE.service", null);
		assertSame("Incorrect escalation", escalation, Handler.cause);
	}

	/**
	 * Ensure can manually handle {@link FailedToSourceManagedObjectEscalation}.
	 */
	public void testManuallyHandle() throws Exception {

		// Start
		this.openOfficeFloor(true);

		// Trigger escalation
		final IOException escalation = new IOException();
		MockObject.initialiseFailure = escalation;
		Handler.cause = null;
		this.officeFloor.invokeFunction("Servicer.NAMESPACE.service", null);
		assertTrue("Incorrect escalation", (Handler.cause instanceof FailedToSourceManagedObjectEscalation));
		assertSame("Incorrect cause", escalation, Handler.cause.getCause());
	}

	/**
	 * Opens the {@link AutoWireOfficeFloor}.
	 * 
	 * @param isManual
	 *            If manually handling
	 *            {@link FailedToSourceManagedObjectEscalation}.
	 */
	private void openOfficeFloor(boolean isManual) throws Exception {

		// Configure the application
		AutoWireApplication source = new AutoWireOfficeFloorSource();

		// Configure the managed object causing escalation
		source.addManagedObject(ClassManagedObjectSource.class.getName(), null, new AutoWire(MockObject.class))
				.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, MockObject.class.getName());

		// Configure servicer to trigger escalation
		source.addSection("Servicer", ClassSectionSource.class.getName(), Servicer.class.getName());

		// Configure handling of escalation
		AutoWireSection handler = source.addSection("Handler", ClassSectionSource.class.getName(),
				Handler.class.getName());
		source.linkEscalation(IOException.class, handler, "handle");
		source.linkEscalation(SQLException.class, handler, "handle");
		if (isManual) {
			source.linkEscalation(FailedToSourceManagedObjectEscalation.class, handler, "handle");
		}

		// Start the application
		this.officeFloor = source.openOfficeFloor();
	}

	/**
	 * Mock servicer.
	 */
	public static class Servicer {
		public void service(MockObject object) {
		}
	}

	/**
	 * Mock handler.
	 */
	public static class Handler {

		public static Throwable cause;

		public void handle(@Parameter Throwable cause) {
			Handler.cause = cause;
		}
	}

	/**
	 * Mock object triggering {@link FailedToSourceManagedObjectEscalation}.
	 */
	public static class MockObject {

		public static Throwable initialiseFailure;

		public MockObject() throws Throwable {
			throw initialiseFailure;
		}
	}

}