/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
package net.officefloor.plugin.servlet.bridge;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.autowire.AutoWireObject;
import net.officefloor.plugin.autowire.AutoWireOfficeFloor;
import net.officefloor.plugin.autowire.AutoWireOfficeFloorSource;
import net.officefloor.plugin.autowire.ManagedObjectSourceWirer;
import net.officefloor.plugin.autowire.ManagedObjectSourceWirerContext;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.servlet.bridge.ServletBridgeManagedObjectSource.FlowKeys;
import net.officefloor.plugin.servlet.bridge.spi.ServletServiceBridger;

/**
 * Tests the {@link ServletBridgeManagedObjectSource}
 * 
 * @author Daniel Sagenschneider
 */
public class ServletBridgeManagedObjectSourceTest extends OfficeFrameTestCase {

	/**
	 * Validate specification.
	 */
	public void testSpecification() {
		ManagedObjectLoaderUtil.validateSpecification(
				ServletBridgeManagedObjectSource.class,
				ServletBridgeManagedObjectSource.PROPERTY_INSTANCE_IDENTIFIER,
				"Instance");
	}

	/**
	 * Validate type.
	 */
	public void testType() {

		// Create expected type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil
				.createManagedObjectTypeBuilder();
		type.setObjectClass(ServletBridge.class);
		type.addFlow(FlowKeys.SERVICE, null, null, null);

		// Validate type
		ManagedObjectLoaderUtil.validateManagedObjectType(type,
				ServletBridgeManagedObjectSource.class,
				ServletBridgeManagedObjectSource.PROPERTY_INSTANCE_IDENTIFIER,
				"1");
	}

	/**
	 * Ensure able to service {@link HttpServletRequest}.
	 */
	public void testService() throws Exception {

		final HttpServletRequest request = this
				.createMock(HttpServletRequest.class);
		final HttpServletResponse response = this
				.createMock(HttpServletResponse.class);

		// Create the servlet
		MockHttpServlet servlet = new MockHttpServlet();
		servlet.dependency = this.createMock(ServletDependency.class);
		servlet.ejb = this.createMock(ServletEjbLocal.class);

		// Create the servicer for servicing
		ServletServiceBridger<MockHttpServlet> servicer = ServletBridgeManagedObjectSource
				.createServletServiceBridger(MockHttpServlet.class);

		// Configure the OfficeFloor
		AutoWireOfficeFloorSource autoWire = new AutoWireOfficeFloorSource();
		AutoWireObject servletBridgeMo = autoWire.addManagedObject(
				ServletBridgeManagedObjectSource.class,
				new ManagedObjectSourceWirer() {
					@Override
					public void wire(ManagedObjectSourceWirerContext context) {
						context.setInput(true);
						context.mapFlow(FlowKeys.SERVICE.name(), "SECTION",
								"service");
					}
				}, ServletBridge.class);
		servletBridgeMo.addProperty(
				ServletBridgeManagedObjectSource.PROPERTY_INSTANCE_IDENTIFIER,
				servicer.getInstanceIdentifier());
		autoWire.addSection("SECTION", ClassSectionSource.class,
				SectionClass.class.getName());

		// Open the OfficeFloor
		AutoWireOfficeFloor officefloor = autoWire.openOfficeFloor();
		try {
			// Ensure appropriately service
			SectionClass.servletBridge = null;
			servicer.service(servlet, request, response);

			// Ensure completed servicing
			assertNotNull("Ensure servlet bridge provided",
					SectionClass.servletBridge);

			// Ensure correct request and response
			assertSame("Incorrect request", request,
					SectionClass.servletBridge.getRequest());
			assertSame("Incorrect response", response,
					SectionClass.servletBridge.getResponse());

			// Ensure obtain dependency injection on Servlet
			ServletDependency servletDependency = SectionClass.servletBridge
					.getObject(ServletDependency.class);
			assertSame("Must obtain dependency", servlet.dependency,
					servletDependency);
			ServletEjbLocal servletEjb = SectionClass.servletBridge
					.getObject(ServletEjbLocal.class);
			assertSame("Must obtain EJB", servlet.ejb, servletEjb);

		} finally {
			// Ensure close
			officefloor.closeOfficeFloor();
		}
	}

	/**
	 * Mock {@link HttpServlet}.
	 */
	public static class MockHttpServlet extends HttpServlet {

		@Resource
		private ServletDependency dependency;

		@EJB
		private ServletEjbLocal ejb;
	}

	/**
	 * {@link Servlet} Dependency.
	 */
	private static interface ServletDependency {
	}

	/**
	 * {@link Servlet} EJB.
	 */
	private static interface ServletEjbLocal {
	}

	/**
	 * Section class.
	 */
	public static class SectionClass {

		/**
		 * {@link ServletBridge}.
		 */
		public static volatile ServletBridge servletBridge = null;

		/**
		 * Service.
		 * 
		 * @param bridge
		 *            {@link ServletBridge}.
		 */
		public void service(ServletBridge bridge) {
			servletBridge = bridge;
		}
	}

}