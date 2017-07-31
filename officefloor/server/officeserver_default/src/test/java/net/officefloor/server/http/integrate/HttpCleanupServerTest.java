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
package net.officefloor.server.http.integrate;

import java.io.IOException;
import java.io.StringWriter;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.recycle.RecycleManagedObjectParameter;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.server.http.HttpServicerFunction;
import net.officefloor.server.http.HttpTestUtil;
import net.officefloor.server.http.MockHttpServer;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * Tests appropriately handles cleanup.
 *
 * @author Daniel Sagenschneider
 */
public class HttpCleanupServerTest extends MockHttpServer {

	/**
	 * <p>
	 * Ensures can handle a GET request.
	 * <p>
	 * This validates the server is running.
	 */
	public void testGetRequest() throws Exception {

		// No escalation
		RecycleEscalationManagedObjectSource.escalation = null;

		// Undertake request
		try (CloseableHttpClient client = this.createHttpClient()) {

			// Create the request
			HttpGet method = new HttpGet(this.getServerUrl() + "/path");

			// Obtain the response
			HttpResponse response = client.execute(method);
			int status = response.getStatusLine().getStatusCode();
			assertEquals("Incorrect status", 200, status);

			// Read in the body of the response
			String responseEntity = HttpTestUtil.getEntityBody(response);
			assertEquals("Incorrect response entity", "No cleanup escalations", responseEntity);
		}
	}

	/**
	 * Ensure report cleanup {@link Escalation}.
	 */
	public void testCleanupEscalation() throws Exception {

		// Specify the escalations
		final Throwable escalation = new Throwable("TEST");
		RecycleEscalationManagedObjectSource.escalation = escalation;

		// Undertake request
		try (CloseableHttpClient client = this.createHttpClient()) {

			// Create the request
			HttpGet method = new HttpGet(this.getServerUrl() + "/path");

			// Obtain the response (should be server error)
			HttpResponse response = client.execute(method);
			int status = response.getStatusLine().getStatusCode();
			assertEquals("Incorrect status", 500, status);

			// Read in the body of the response
			String responseEntity = HttpTestUtil.getEntityBody(response);
			StringWriter expectedEntity = new StringWriter();
			expectedEntity.write("Cleanup of object type " + RecycleEscalationManagedObjectSource.class.getName()
					+ ": TEST (" + escalation.getClass().getSimpleName() + ")");
			assertEquals("Incorrect response entity", expectedEntity.toString(), responseEntity);
		}
	}

	/*
	 * ================== HttpServicerBuilder ===============================
	 */

	@Override
	public HttpServicerFunction buildServicer(String managedObjectName, MockHttpServer server) throws Exception {

		// Register the managed object
		ManagedObjectBuilder<None> builder = this.getOfficeFloorBuilder().addManagedObject("ESCALATE",
				RecycleEscalationManagedObjectSource.class);
		builder.setManagingOffice(this.getOfficeName());
		this.getOfficeBuilder().addThreadManagedObject("MO_ESCALATE", "ESCALATE");
		this.getOfficeBuilder().registerManagedObjectSource("ESCALATE", "ESCALATE");

		// Register the servicer to process messages
		MockServicer servicer = new MockServicer();
		ReflectiveFunctionBuilder functionBuilder = server.constructFunction(servicer, "service");
		functionBuilder.buildObject(managedObjectName);
		functionBuilder.buildObject("MO_ESCALATE");

		// Return the reference to the service function
		return new HttpServicerFunction("service");
	}

	/**
	 * {@link ManagedObjectSource} that has a recycle {@link Escalation}.
	 *
	 * @author Daniel Sagenschneider
	 */
	@TestSource
	public static class RecycleEscalationManagedObjectSource extends AbstractManagedObjectSource<None, None>
			implements ManagedObject, ManagedFunctionFactory<Indexed, None>, ManagedFunction<Indexed, None> {

		/**
		 * {@link Escalation} on cleanup.
		 */
		public static volatile Throwable escalation = null;

		/*
		 * ===================== ManagedObjectSource =======================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No specification required
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {

			// Configure
			context.setObjectClass(this.getClass());

			// Provide recycle function
			ManagedObjectSourceContext<None> mos = context.getManagedObjectSourceContext();
			mos.getRecycleFunction(this).linkParameter(0, RecycleManagedObjectParameter.class);
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			return this;
		}

		/*
		 * ===================== ManagedObjectSource =======================
		 */

		@Override
		public Object getObject() throws Throwable {
			return this;
		}

		/*
		 * =================== ManagedFunctionFactory ======================
		 */

		@Override
		public ManagedFunction<Indexed, None> createManagedFunction() {
			return this;
		}

		/*
		 * ===================== ManagedFunction ============================
		 */

		@Override
		public Object execute(ManagedFunctionContext<Indexed, None> context) throws Throwable {

			// Trigger escalation if provided
			Throwable cleanupFailure = RecycleEscalationManagedObjectSource.escalation;
			if (cleanupFailure != null) {
				throw cleanupFailure;
			}

			// No further functions
			return null;
		}
	}

	/**
	 * Mock servicer.
	 */
	public static class MockServicer {

		/**
		 * Service method.
		 * 
		 * @param connection
		 *            {@link ServerHttpConnection}.
		 * @param managedObject
		 *            {@link RecycleEscalationManagedObjectSource}.
		 */
		public void service(ServerHttpConnection connection, RecycleEscalationManagedObjectSource managedObject)
				throws IOException {
			connection.getHttpResponse().getEntityWriter().write("No cleanup escalations");
		}
	}

}