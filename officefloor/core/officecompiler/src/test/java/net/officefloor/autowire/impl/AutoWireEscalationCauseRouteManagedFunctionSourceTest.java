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
import net.officefloor.autowire.impl.AutoWireEscalationCauseRouteManagedFunctionSource.AutoWireEscalationCauseRouteManagedFunction;
import net.officefloor.autowire.impl.AutoWireEscalationCauseRouteManagedFunctionSource.Dependencies;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionFlowTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.test.managedfunction.ManagedFunctionLoaderUtil;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.plugin.section.managedfunction.ManagedFunctionSectionSource;

/**
 * Tests the {@link AutoWireEscalationCauseRouteManagedFunctionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWireEscalationCauseRouteManagedFunctionSourceTest extends OfficeFrameTestCase {

	/**
	 * {@link MockHandlerState}.
	 */
	private final MockHandlerState state = new MockHandlerState();

	/**
	 * {@link Escalation} from the {@link EscalationHandler}.
	 */
	private Throwable escalation = null;

	/**
	 * {@link EscalationHandler}.
	 */
	private final EscalationHandler handler = new EscalationHandler() {
		@Override
		public void handleEscalation(Throwable escalation) throws Throwable {
			AutoWireEscalationCauseRouteManagedFunctionSourceTest.this.escalation = escalation;
		}
	};

	/**
	 * {@link AutoWireOfficeFloor}.
	 */
	private AutoWireOfficeFloor officeFloor;

	@Override
	protected void tearDown() throws Exception {
		// Ensure close
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Validate specification.
	 */
	public void testSpecification() {
		ManagedFunctionLoaderUtil.validateSpecification(AutoWireEscalationCauseRouteManagedFunctionSource.class);
	}

	/**
	 * Validate type with no handling configured.
	 */
	public void testTypeNoHandling() {

		// Create the expected type
		AutoWireEscalationCauseRouteManagedFunction factory = new AutoWireEscalationCauseRouteManagedFunction(null);
		FunctionNamespaceBuilder type = ManagedFunctionLoaderUtil.createManagedFunctionTypeBuilder();
		ManagedFunctionTypeBuilder<Dependencies, Indexed> function = type.addManagedFunctionType("Handle", factory,
				Dependencies.class, Indexed.class);
		function.addObject(Throwable.class).setKey(Dependencies.ESCALATION);
		function.addEscalation(Throwable.class);

		// Validate the type
		ManagedFunctionLoaderUtil.validateManagedFunctionType(type,
				AutoWireEscalationCauseRouteManagedFunctionSource.class);
	}

	/**
	 * Validate type with handling configured.
	 */
	public void testTypeWithHandling() {

		// Create the expected type
		AutoWireEscalationCauseRouteManagedFunction factory = new AutoWireEscalationCauseRouteManagedFunction(null);
		FunctionNamespaceBuilder type = ManagedFunctionLoaderUtil.createManagedFunctionTypeBuilder();
		ManagedFunctionTypeBuilder<Dependencies, Indexed> function = type.addManagedFunctionType("Handle", factory,
				Dependencies.class, Indexed.class);
		function.addObject(Throwable.class).setKey(Dependencies.ESCALATION);
		this.registerFlow(function, IOException.class);
		this.registerFlow(function, SQLException.class);
		this.registerFlow(function, Exception.class);
		function.addEscalation(Throwable.class);

		// Validate the type
		ManagedFunctionLoaderUtil.validateManagedFunctionType(type,
				AutoWireEscalationCauseRouteManagedFunctionSource.class,
				AutoWireEscalationCauseRouteManagedFunctionSource.PROPERTY_PREFIX_ESCALATION_TYPE + "0",
				IOException.class.getName(),
				AutoWireEscalationCauseRouteManagedFunctionSource.PROPERTY_PREFIX_ESCALATION_TYPE + "1",
				SQLException.class.getName(),
				AutoWireEscalationCauseRouteManagedFunctionSource.PROPERTY_PREFIX_ESCALATION_TYPE + "2",
				Exception.class.getName());
	}

	/**
	 * Register the flow.
	 * 
	 * @param function
	 *            {@link ManagedFunctionTypeBuilder}.
	 * @param cause
	 *            {@link Class} of the cause.
	 */
	private void registerFlow(ManagedFunctionTypeBuilder<Dependencies, Indexed> function,
			Class<? extends Throwable> cause) {
		ManagedFunctionFlowTypeBuilder<Indexed> flow = function.addFlow();
		flow.setArgumentType(cause);
		flow.setLabel(cause.getName());
	}

	/**
	 * Ensure can handle {@link IOException} cause.
	 */
	public void testHandleIOException() throws Exception {

		// Open
		this.openOfficeFloor();

		// Ensure cause of escalation routed
		final IOException cause = new IOException("TEST");
		this.officeFloor.invokeFunction("HANDLER.NAMESPACE.escalate", new Error(cause));
		assertSame("Incorrect cause handled", cause, this.state.cause);
	}

	/**
	 * Ensure can handle {@link SQLException} cause.
	 */
	public void testHandleSQLException() throws Exception {

		// Open
		this.openOfficeFloor();

		// Ensure cause of escalation routed
		final SQLException cause = new SQLException("TEST");
		this.officeFloor.invokeFunction("HANDLER.NAMESPACE.escalate", new Error(cause));
		assertSame("Incorrect cause handled", cause, this.state.cause);
	}

	/**
	 * Ensure can handle {@link Exception} cause.
	 */
	public void testHandleException() throws Exception {

		// Open
		this.openOfficeFloor();

		// Ensure cause of escalation routed
		final Exception cause = new Exception("TEST");
		this.officeFloor.invokeFunction("HANDLER.NAMESPACE.escalate", new Error(cause));
		assertSame("Incorrect cause handled", cause, this.state.cause);
	}

	/**
	 * Ensure can handle unknown cause.
	 */
	public void testHandleUnknownCause() throws Exception {

		// Open
		this.openOfficeFloor();

		// Ensure escalation propagated
		final Error escalation = new Error("TEST", new Error());
		this.officeFloor.invokeFunction("HANDLER.NAMESPACE.escalate", escalation);
		assertSame("Escalation should be propagated", escalation, this.escalation);
	}

	/**
	 * Ensure can handle no cause.
	 */
	public void testHandleNoCause() throws Exception {

		// Open
		this.openOfficeFloor();

		// Ensure escalation propagated
		final Error escalation = new Error("TEST");
		this.officeFloor.invokeFunction("HANDLER.NAMESPACE.escalate", escalation);
		assertSame("Escalation should be propagated", escalation, this.escalation);
	}

	/**
	 * Opens the {@link AutoWireOfficeFloor}.
	 */
	private void openOfficeFloor() throws Exception {

		// Create the source
		AutoWireApplication source = new AutoWireOfficeFloorSource();

		// Provide state to retrieve handled cause
		source.addObject(this.state, new AutoWire(MockHandlerState.class));

		// Capture propagation of escalation
		source.getOfficeFloorCompiler().setEscalationHandler(this.handler);

		// Add the escalation cause router
		AutoWireSection router = source.addSection("ROUTE", ManagedFunctionSectionSource.class.getName(),
				AutoWireEscalationCauseRouteManagedFunctionSource.class.getName());
		router.addProperty(ManagedFunctionSectionSource.PROPERTY_PARAMETER_PREFIX + "Handle", "1");
		source.linkEscalation(Error.class, router, "Handle");

		// Add the escalation handlers
		AutoWireSection handler = source.addSection("HANDLER", ClassSectionSource.class.getName(),
				MockEscalationHandler.class.getName());

		// Configure the escalation cause handling
		// (Escalations should be ordered - loading out of order to ensure)
		linkEscalationCauseHandling(Exception.class, router, handler, source);
		linkEscalationCauseHandling(IOException.class, router, handler, source);
		linkEscalationCauseHandling(SQLException.class, router, handler, source);

		// Open the OfficeFloor
		this.officeFloor = source.openOfficeFloor();
	}

	/**
	 * Links the {@link Escalation} cause handling.
	 * 
	 * @param causeType
	 *            {@link Escalation} cause type.
	 * @param router
	 *            {@link AutoWireSection} for the
	 *            {@link AutoWireEscalationCauseRouteManagedFunctionSource}.
	 * @param handler
	 *            Handling {@link AutoWireSection}.
	 * @param source
	 *            {@link AutoWireApplication}.
	 */
	private static void linkEscalationCauseHandling(Class<? extends Throwable> causeType, AutoWireSection router,
			AutoWireSection handler, AutoWireApplication source) {
		AutoWireEscalationCauseRouteManagedFunctionSource.configureEscalationCause(router, causeType);
		source.link(router, causeType.getName(), handler, "handle" + causeType.getSimpleName());
	}

	/**
	 * Provides state for the handlers.
	 */
	public static class MockHandlerState {

		/**
		 * {@link Throwable} being handled.
		 */
		public Throwable cause = null;
	}

	/**
	 * Mock {@link Escalation} handler.
	 */
	public static class MockEscalationHandler {

		public void escalate(@Parameter Error escalation) throws Error {
			throw escalation;
		}

		public void handleIOException(@Parameter IOException cause, MockHandlerState state) {
			state.cause = cause;
		}

		public void handleSQLException(@Parameter SQLException cause, MockHandlerState state) {
			state.cause = cause;
		}

		public void handleException(@Parameter Exception cause, MockHandlerState state) {
			assertFalse("Ensure not IOException", (cause instanceof IOException));
			assertFalse("Ensure not SQLException", (cause instanceof SQLException));
			state.cause = cause;
		}
	}

}