/*-
 * #%L
 * Servlet
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.servlet;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

import net.officefloor.compile.spi.supplier.source.AvailableType;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.servlet.supply.ServletSupplierSource;
import net.officefloor.servlet.supply.extension.BeforeCompleteServletSupplierExtensionContext;
import net.officefloor.servlet.supply.extension.ServletSupplierExtension;
import net.officefloor.servlet.supply.extension.ServletSupplierExtensionServiceFactory;
import net.officefloor.woof.compile.CompileWoof;
import net.officefloor.woof.mock.MockWoofResponse;
import net.officefloor.woof.mock.MockWoofServer;

/**
 * Tests {@link ServletSupplierSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletSupplierTest extends OfficeFrameTestCase
		implements ServletSupplierExtensionServiceFactory, ServletSupplierExtension {

	/**
	 * Indicates if {@link Servlet} container was force started.
	 */
	private static boolean isForceStarted = false;

	/**
	 * Indicates if force started before completion.
	 */
	private static Boolean isStartedBeforeCompletion = null;

	/**
	 * {@link AvailableType} instances.
	 */
	private static AvailableType[] availableTypes = null;

	/**
	 * Ensure {@link Servlet} container started.
	 */
	public void testStart() throws Exception {
		isForceStarted = false;
		isStartedBeforeCompletion = null;
		availableTypes = null;
		CompileWoof compiler = new CompileWoof(true);
		compiler.web((context) -> {
			context.link(false, "/servlet", CompleteService.class);

			// Provide servlet
			Context servletContext = ServletSupplierSource.getServletManager().getContext();
			Tomcat.addServlet(servletContext, "test", MockHttpServlet.class.getName());
			servletContext.addServletMappingDecoded("/servlet", "test");
		});
		try (MockWoofServer server = compiler.open()) {
			MockWoofResponse response = server.send(MockWoofServer.mockRequest("/servlet"));
			response.assertResponse(200, "SERVLET");
		}
		assertFalse("Should start after completion", isStartedBeforeCompletion);
		assertNotNull("Should have available types", availableTypes);
	}

	public static class CompleteService {
		public void service(ServerHttpConnection connection, ManagedFunctionContext<?, ?> context,
				ServletServicer servicer) throws Exception {
			servicer.service(connection, context.getExecutor(), context.createAsynchronousFlow(), null, null);
		}
	}

	/**
	 * Ensure can force start {@link Servlet} container.
	 */
	public void testForceStart() throws Exception {
		isForceStarted = false;
		isStartedBeforeCompletion = null;
		availableTypes = null;
		CompileWoof compiler = new CompileWoof(true);
		compiler.woof((context) -> {
			context.getOfficeArchitect().addOfficeManagedObjectSource("FORCE", new ForceStartManagedObjectSource())
					.addOfficeManagedObject("FORCE", ManagedObjectScope.THREAD);
		});
		compiler.web((context) -> {
			context.link(false, "/servlet", ForceService.class);

			// Provide servlet
			Context servletContext = ServletSupplierSource.getServletManager().getContext();
			Tomcat.addServlet(servletContext, "test", MockHttpServlet.class.getName());
			servletContext.addServletMappingDecoded("/servlet", "test");
		});
		try (MockWoofServer server = compiler.open()) {
			MockWoofResponse response = server.send(MockWoofServer.mockRequest("/servlet"));
			response.assertResponse(200, "SERVLET");
		}
		assertTrue("Should force start before completion", isStartedBeforeCompletion);
		assertNotNull("Should have available types", availableTypes);
	}

	public static class ForceService {
		public void service(ServerHttpConnection connection, ManagedFunctionContext<?, ?> context,
				ForceStartManagedObjectSource managedObject) throws Exception {
			managedObject.servletServicer.service(connection, context.getExecutor(), context.createAsynchronousFlow(),
					null, null);
		}
	}

	public static class MockHttpServlet extends HttpServlet {
		private static final long serialVersionUID = 1L;

		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			resp.getWriter().write("SERVLET");
		}
	}

	@TestSource
	public static class ForceStartManagedObjectSource extends AbstractManagedObjectSource<None, None>
			implements ManagedObject {

		private ServletServicer servletServicer;

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// no specification
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
			context.setObjectClass(ForceStartManagedObjectSource.class);

			// Force start servlet container
			if (this.servletServicer == null) {
				this.servletServicer = ServletSupplierSource.forceStartServletContainer(new AvailableType[0]);
				isForceStarted = true;
			}
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			return this;
		}

		@Override
		public Object getObject() throws Throwable {
			return this;
		}
	}

	/*
	 * ============== ServletSupplierExtensionServiceFactory ==============
	 */

	@Override
	public ServletSupplierExtension createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ===================== ServletSupplierExtension =====================
	 */

	@Override
	public void beforeCompletion(BeforeCompleteServletSupplierExtensionContext context) throws Exception {
		isStartedBeforeCompletion = isForceStarted;

		// Capture the available types
		availableTypes = context.getAvailableTypes();
	}

}
