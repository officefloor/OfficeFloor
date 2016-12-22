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
package net.officefloor.plugin.socket.server.http.integrate;

import java.io.IOException;
import java.io.StringWriter;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.OfficeEnhancer;
import net.officefloor.frame.api.build.OfficeEnhancerContext;
import net.officefloor.frame.api.build.ManagedFunctionFactory;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.execute.ManagedFunctionContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.spi.TestSource;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.recycle.RecycleManagedObjectParameter;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectTaskBuilder;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectWorkBuilder;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.test.MockTeamSource;
import net.officefloor.frame.test.ReflectiveWorkBuilder;
import net.officefloor.frame.test.ReflectiveWorkBuilder.ReflectiveTaskBuilder;
import net.officefloor.plugin.socket.server.http.HttpTestUtil;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.server.HttpServicerTask;
import net.officefloor.plugin.socket.server.http.server.MockHttpServer;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

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
		try (CloseableHttpClient client = HttpClientBuilder.create().build()) {

			// Create the request
			HttpGet method = new HttpGet(this.getServerUrl() + "/path");

			// Obtain the response
			HttpResponse response = client.execute(method);
			int status = response.getStatusLine().getStatusCode();
			assertEquals("Incorrect status", 200, status);

			// Read in the body of the response
			String responseEntity = HttpTestUtil.getEntityBody(response);
			assertEquals("Incorrect response entity", "No cleanup escalations",
					responseEntity);
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
		try (CloseableHttpClient client = HttpClientBuilder.create().build()) {

			// Create the request
			HttpGet method = new HttpGet(this.getServerUrl() + "/path");

			// Obtain the response (should be server error)
			HttpResponse response = client.execute(method);
			int status = response.getStatusLine().getStatusCode();
			assertEquals("Incorrect status", 500, status);

			// Read in the body of the response
			String responseEntity = HttpTestUtil.getEntityBody(response);
			StringWriter expectedEntity = new StringWriter();
			expectedEntity.write("Cleanup of object type "
					+ RecycleEscalationManagedObjectSource.class.getName()
					+ ": TEST (" + escalation.getClass().getSimpleName()
					+ ")\n");
			assertEquals("Incorrect response entity",
					expectedEntity.toString(), responseEntity);
		}
	}

	/*
	 * ================== HttpServicerBuilder ===============================
	 */

	@Override
	public HttpServicerTask buildServicer(String managedObjectName,
			MockHttpServer server) throws Exception {

		// Register team to do the work
		server.constructTeam("WORKER",
				MockTeamSource.createOnePersonTeam("WORKER"));

		// Register the managed object
		ManagedObjectBuilder<None> builder = this.getOfficeFloorBuilder()
				.addManagedObject("ESCALATE",
						RecycleEscalationManagedObjectSource.class);
		builder.setManagingOffice(this.getOfficeName());
		this.getOfficeBuilder().addThreadManagedObject("MO_ESCALATE",
				"ESCALATE");
		this.getOfficeBuilder().registerManagedObjectSource("ESCALATE",
				"ESCALATE");

		// Configure the teams for recycling the managed object
		this.getOfficeBuilder().addOfficeEnhancer(new OfficeEnhancer() {
			@Override
			public void enhanceOffice(OfficeEnhancerContext context) {
				context.getFlowNodeBuilder("ESCALATE", "#recycle#", "cleanup")
						.setTeam("WORKER");
			}
		});

		// Register the work to process messages
		MockWork work = new MockWork();
		ReflectiveWorkBuilder workBuilder = server.constructWork(work,
				"servicer", "service");
		ReflectiveTaskBuilder taskBuilder = workBuilder.buildTask("service",
				"WORKER");
		taskBuilder.buildObject(managedObjectName);
		taskBuilder.buildObject("MO_ESCALATE");

		// Return the reference to the service task
		return new HttpServicerTask("servicer", "service");
	}

	/**
	 * {@link ManagedObjectSource} that has a recycle {@link Escalation}.
	 *
	 * @author Daniel Sagenschneider
	 */
	@TestSource
	public static class RecycleEscalationManagedObjectSource extends
			AbstractManagedObjectSource<None, None> implements ManagedObject,
			WorkFactory<Work>, Work, ManagedFunctionFactory<Work, Indexed, None>,
			ManagedFunction<Work, Indexed, None> {

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
		protected void loadMetaData(MetaDataContext<None, None> context)
				throws Exception {

			// Configure
			context.setObjectClass(this.getClass());

			// Provide recycle task
			ManagedObjectSourceContext<None> mos = context
					.getManagedObjectSourceContext();
			ManagedObjectWorkBuilder<Work> cleanup = mos.getRecycleWork(this);
			ManagedObjectTaskBuilder<Indexed, None> recycleTask = cleanup
					.addTask("cleanup", this);
			recycleTask.linkParameter(0, RecycleManagedObjectParameter.class);
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
		 * ===================== WorkFactory =================================
		 */

		@Override
		public Work createWork() {
			return this;
		}

		/*
		 * ===================== TaskFactory =================================
		 */

		@Override
		public ManagedFunction<Work, Indexed, None> createManagedFunction(Work work) {
			return this;
		}

		/*
		 * ======================== Task =====================================
		 */

		@Override
		public Object execute(ManagedFunctionContext<Work, Indexed, None> context)
				throws Throwable {

			// Trigger escalation if provided
			Throwable cleanupFailure = RecycleEscalationManagedObjectSource.escalation;
			if (cleanupFailure != null) {
				throw cleanupFailure;
			}

			// No further tasks
			return null;
		}
	}

	/**
	 * Mock {@link Work}.
	 */
	public static class MockWork {

		/**
		 * Service method.
		 * 
		 * @param connection
		 *            {@link ServerHttpConnection}.
		 * @param managedObject
		 *            {@link RecycleEscalationManagedObjectSource}.
		 */
		public void service(ServerHttpConnection connection,
				RecycleEscalationManagedObjectSource managedObject)
				throws IOException {
			connection.getHttpResponse().getEntityWriter()
					.write("No cleanup escalations");
		}
	}

}