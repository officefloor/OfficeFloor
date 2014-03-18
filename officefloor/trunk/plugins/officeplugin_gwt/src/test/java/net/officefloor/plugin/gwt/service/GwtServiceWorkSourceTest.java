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
package net.officefloor.plugin.gwt.service;

import java.lang.reflect.Method;
import java.util.Map;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireManagement;
import net.officefloor.autowire.AutoWireSection;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.SectionTask;
import net.officefloor.compile.spi.section.SectionWork;
import net.officefloor.compile.spi.section.TaskFlow;
import net.officefloor.compile.spi.section.TaskObject;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.compile.spi.work.source.TaskFlowTypeBuilder;
import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.test.work.WorkLoaderUtil;
import net.officefloor.compile.work.TaskFlowType;
import net.officefloor.compile.work.WorkType;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.gwt.service.GwtServiceTask.Dependencies;
import net.officefloor.plugin.socket.server.http.HttpTestUtil;
import net.officefloor.plugin.web.http.server.HttpServerAutoWireOfficeFloorSource;
import net.officefloor.plugin.work.clazz.ClassWorkSource;

import org.easymock.AbstractMatcher;

import com.gdevelop.gwt.syncrpc.SyncProxy;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException;

/**
 * Tests the {@link GwtServiceWorkSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class GwtServiceWorkSourceTest extends OfficeFrameTestCase {

	/**
	 * Mock {@link CompilerIssues}.
	 */
	private final CompilerIssues issues = this.createMock(CompilerIssues.class);

	@Override
	protected void tearDown() throws Exception {
		// Ensure stop server
		AutoWireManagement.closeAllOfficeFloors();
	}

	/**
	 * Validate specification.
	 */
	public void testSpecification() {
		WorkLoaderUtil.validateSpecification(GwtServiceWorkSource.class,
				GwtServiceWorkSource.PROPERTY_GWT_ASYNC_SERVICE_INTERFACE,
				"Interface");
	}

	/**
	 * Validate type with valid Async interface methods.
	 */
	public void testType_ValidMethods() {
		this.doTypeTest(GwtServiceInterfaceAsync.class.getName(),
				new TaskFlowTypeLoader() {
					@Override
					public void loadTasks(
							TaskTypeBuilder<Dependencies, Indexed> task) {
						TaskFlowTypeBuilder<Indexed> call = task.addFlow();
						call.setLabel("call");
						TaskFlowTypeBuilder<Indexed> generic = task.addFlow();
						generic.setLabel("generic");
						TaskFlowTypeBuilder<Indexed> parameter = task.addFlow();
						parameter.setLabel("parameter");
						parameter.setArgumentType(Long.class);
						TaskFlowTypeBuilder<Indexed> raw = task.addFlow();
						raw.setLabel("raw");
					}
				}, null);
	}

	/**
	 * GWT Service Interface for testing type.
	 */
	public static interface GwtServiceInterfaceAsync {

		void call(AsyncCallback<Integer[]> callback);

		void parameter(Long parameter, AsyncCallback<String> callback);

		@SuppressWarnings("rawtypes")
		void raw(AsyncCallback callback);

		void generic(AsyncCallback<Map<String, Integer>> callback);
	}

	/**
	 * Ensure issue if duplicate method name on Async interface.
	 */
	public void testType_DuplicateMethodName() {

		// Record duplicate service method
		String errorMessage = "Duplicate GWT Service Async method name 'DuplicateMethodNameAsync.duplicate(...)'. "
				+ "Method names must be unique per GWT service interface.";

		// Test
		this.doTypeTest(DuplicateMethodNameAsync.class.getName(), null,
				errorMessage);
	}

	/**
	 * GWT Service Interface with duplicate method name.
	 */
	public static interface DuplicateMethodNameAsync {

		void duplicate(String parameter, AsyncCallback<String> callback);

		void duplicate(Integer parameter, AsyncCallback<Integer> callback);
	}

	/**
	 * Ensure issue if invalid method on Async interface.
	 */
	public void testType_InvalidMethod() throws Exception {

		// Obtain the expected error message
		Method method = InvalidMethodAsync.class.getMethod("invalid");
		GwtAsyncMethodMetaData metaData = new GwtAsyncMethodMetaData(method);
		final String expectedErrorMessage = "Invalid async method InvalidMethodAsync.invalid: "
				+ metaData.getError();

		// Test
		this.doTypeTest(InvalidMethodAsync.class.getName(), null,
				expectedErrorMessage);
	}

	/**
	 * GWT Service Interface with invalid method.
	 */
	public static interface InvalidMethodAsync {

		void invalid();
	}

	/**
	 * Validate invoking GWT service.
	 */
	public void testInvokeGwtService() throws Exception {

		// Start the Server
		MockGwtServiceInterface service = this
				.startServer(MockGwtServiceInterface.class);

		// Ensure services request
		String result = service.service(new Integer(1));
		assertEquals("Incorrect response", "SUCCESS", result);
	}

	/**
	 * Validate handle unknown GWT service.
	 */
	public void testUnknownGwtService() throws Exception {

		// Start the Server
		UnknownGwtServiceInterface service = this
				.startServer(UnknownGwtServiceInterface.class);

		// Ensure services request
		try {
			service.unknown("UNKNOWN");
			fail("Should not be successful on unknown method");
		} catch (IncompatibleRemoteServiceException ex) {
			String causeMessage = ex.getMessage();
			assertTrue(
					"Incorrect exception: " + causeMessage,
					causeMessage
							.endsWith("( Unknown service method 'unknown(...)' )"));
		}
	}

	/**
	 * Undertakes the type test.
	 * 
	 * @param gwtServiceInterfaceName
	 *            Name of the GWT Service Async Interface.
	 * @param flows
	 *            {@link TaskFlowTypeLoader}.
	 */
	private void doTypeTest(String gwtServiceAsyncInterfaceName,
			TaskFlowTypeLoader flows, final String expectedErrorMessage) {

		// Determine if error message
		if (expectedErrorMessage != null) {
			// Record expected error message
			this.issues.addIssue(LocationType.SECTION, null, AssetType.WORK,
					null,
					"Failed to source WorkType definition from WorkSource "
							+ GwtServiceWorkSource.class.getName(), null);
			this.control(this.issues).setMatcher(new AbstractMatcher() {
				@Override
				public boolean matches(Object[] expected, Object[] actual) {
					for (int i = 0; i < 5; i++) {
						assertEquals("Incorrect parameter " + i, expected[i],
								actual[i]);
					}
					IllegalArgumentException cause = (IllegalArgumentException) actual[5];
					assertEquals("Incorrect cause", expectedErrorMessage,
							cause.getMessage());
					return true;
				}
			});
		}

		// Create the expected type
		GwtServiceTask factory = new GwtServiceTask(
				new GwtAsyncMethodMetaData[0]);
		WorkTypeBuilder<GwtServiceTask> type = WorkLoaderUtil
				.createWorkTypeBuilder(factory);

		// Add task to service request
		TaskTypeBuilder<Dependencies, Indexed> task = type.addTaskType(
				"service", factory, Dependencies.class, Indexed.class);

		// Task requires GWT RPC connection
		task.addObject(ServerGwtRpcConnection.class).setKey(
				Dependencies.SERVER_GWT_RPC_CONNECTION);

		// Provide flows to service each interface method
		if (flows != null) {
			flows.loadTasks(task);
		}

		// Validate type
		this.replayMockObjects();
		if (expectedErrorMessage != null) {
			// Error, therefore allow play back
			OfficeFloorCompiler compiler = OfficeFloorCompiler
					.newOfficeFloorCompiler(null);
			compiler.setCompilerIssues(this.issues);

			// Load work type as expecting it not to be loaded
			WorkType<GwtServiceTask> workType = WorkLoaderUtil.loadWorkType(
					GwtServiceWorkSource.class, compiler,
					GwtServiceWorkSource.PROPERTY_GWT_ASYNC_SERVICE_INTERFACE,
					gwtServiceAsyncInterfaceName);
			assertNull("Should not load WorkType if expecting error", workType);

		} else {
			// Validate work (as expect to load successfully)
			WorkLoaderUtil.validateWorkType(type, GwtServiceWorkSource.class,
					GwtServiceWorkSource.PROPERTY_GWT_ASYNC_SERVICE_INTERFACE,
					gwtServiceAsyncInterfaceName);
		}
		this.verifyMockObjects();
	}

	/**
	 * Loads the expected {@link TaskFlowType} instances.
	 */
	private static interface TaskFlowTypeLoader {

		/**
		 * Loads the {@link TaskFlowType} instances.
		 * 
		 * @param flows
		 *            {@link TaskTypeBuilder}.
		 */
		void loadTasks(TaskTypeBuilder<Dependencies, Indexed> task);
	}

	/**
	 * Starts the server and returns service to server.
	 * 
	 * @return {@link MockGwtServiceInterface},
	 */
	@SuppressWarnings("unchecked")
	private <T> T startServer(Class<T> serviceInterfaceType) throws Exception {

		// Configure the server for GWT service
		final int PORT = HttpTestUtil.getAvailablePort();
		HttpServerAutoWireOfficeFloorSource source = new HttpServerAutoWireOfficeFloorSource(
				PORT);

		// Configure GWT service
		AutoWireSection section = source.addSection("SECTION",
				MockGwtServiceSection.class.getName(), "LOCATION");
		source.linkUri("template/GwtServicePath", section, "service");
		source.addManagedObject(ServerGwtRpcConnectionManagedObjectSource.class
				.getName(), null, new AutoWire(ServerGwtRpcConnection.class),
				new AutoWire(AsyncCallback.class));

		// Start the server
		source.openOfficeFloor();

		// Create proxy to the GWT Service
		T service = (T) SyncProxy.newProxyInstance(serviceInterfaceType,
				"http://localhost:" + PORT + "/template/", "GwtServicePath");

		// Return the service
		return service;
	}

	/**
	 * {@link SectionSource} for testing the GWT service.
	 */
	public static class MockGwtServiceSection extends AbstractSectionSource {

		/*
		 * =================== SectionSource =======================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No properties required
		}

		@Override
		public void sourceSection(SectionDesigner designer,
				SectionSourceContext context) throws Exception {

			// Create the service task
			SectionWork work = designer.addSectionWork("GWT",
					GwtServiceWorkSource.class.getName());
			work.addProperty(
					GwtServiceWorkSource.PROPERTY_GWT_ASYNC_SERVICE_INTERFACE,
					MockGwtServiceInterfaceAsync.class.getName());
			SectionTask task = work.addSectionTask("service", "service");

			// Link for handling GWT Service
			SectionInput input = designer.addSectionInput("service", null);
			designer.link(input, task);

			// Add Server GWT RPC Connection
			TaskObject taskConnection = task
					.getTaskObject("SERVER_GWT_RPC_CONNECTION");
			SectionObject sectionConnection = designer.addSectionObject(
					"SERVER_GWT_RPC_CONNECTION",
					ServerGwtRpcConnection.class.getName());
			designer.link(taskConnection, sectionConnection);

			// Add servicing method
			SectionWork classWork = designer.addSectionWork("CLASS",
					ClassWorkSource.class.getName());
			classWork.addProperty(ClassWorkSource.CLASS_NAME_PROPERTY_NAME,
					Service.class.getName());
			SectionTask classTask = classWork
					.addSectionTask("handle", "handle");
			TaskObject classObject = classTask
					.getTaskObject(AsyncCallback.class.getName());
			designer.link(classObject, sectionConnection);

			// Link servicing
			TaskFlow serviceFlow = task.getTaskFlow("service");
			designer.link(serviceFlow, classTask,
					FlowInstigationStrategyEnum.SEQUENTIAL);
		}
	}

	/**
	 * Services the GWT RPC request.
	 */
	public static class Service {
		public void handle(AsyncCallback<String> callback) {
			callback.onSuccess("SUCCESS");
		}
	}

}