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

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireManagement;
import net.officefloor.autowire.AutoWireSection;
import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.plugin.gwt.service.ServerGwtRpcConnectionManagedObjectSource.Dependencies;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.HttpTestUtil;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.server.HttpServerAutoWireOfficeFloorSource;

import org.junit.Test;

import com.gdevelop.gwt.syncrpc.SyncProxy;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.InvocationException;
import com.google.gwt.user.client.rpc.StatusCodeException;
import com.google.gwt.user.server.rpc.RPCRequest;

/**
 * Tests the {@link ServerGwtRpcConnectionManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServerGwtRpcConnectionManagedObjectSourceTest extends
		OfficeFrameTestCase {

	/**
	 * Provide ability to propagate test failures within server.
	 */
	private static volatile Throwable serverFailure = null;

	@Override
	protected void setUp() throws Exception {
		// Reset for testing
		serverFailure = null;
	}

	@Override
	protected void tearDown() throws Exception {
		// Close the Servers
		AutoWireManagement.closeAllOfficeFloors();

		// Ensure propagate server failure
		if (serverFailure != null) {
			fail(serverFailure);
		}
	}

	/**
	 * Validate specification.
	 */
	@Test
	public void testSpecification() {
		ManagedObjectLoaderUtil
				.validateSpecification(ServerGwtRpcConnectionManagedObjectSource.class);
	}

	/**
	 * Validate type.
	 */
	@Test
	public void testType() {

		// Create the expected type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil
				.createManagedObjectTypeBuilder();
		type.setObjectClass(ServerGwtRpcConnection.class);
		type.addDependency(Dependencies.SERVER_HTTP_CONNECTION,
				ServerHttpConnection.class, null);

		// Validate type
		ManagedObjectLoaderUtil.validateManagedObjectType(type,
				ServerGwtRpcConnectionManagedObjectSource.class);
	}

	/**
	 * Validate source and use.
	 */
	@Test
	public void testSource() throws Throwable {

		final ServerHttpConnection httpConnection = this
				.createMock(ServerHttpConnection.class);

		// Load the managed object source
		ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();
		ServerGwtRpcConnectionManagedObjectSource source = loader
				.loadManagedObjectSource(ServerGwtRpcConnectionManagedObjectSource.class);

		// Source the managed object
		ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();
		user.mapDependency(Dependencies.SERVER_HTTP_CONNECTION, httpConnection);
		ManagedObject managedObject = user.sourceManagedObject(source);

		// Obtain the object
		Object object = managedObject.getObject();
		assertTrue("Should be a Server GWT RPC connection",
				(object instanceof ServerGwtRpcConnection));
	}

	/**
	 * Ensure able to use for successful response.
	 */
	public void testUse_success() throws Exception {

		// Start HTTP Server and service for success
		MockGwtServiceInterface service = this.startService("success");

		// Request to ensure correctly decodes/encodes success
		String result = service.service(new Integer(1));
		assertEquals("Ensure correct result", "SUCCESS", result);
	}

	/**
	 * Ensure able to use for failed response.
	 */
	public void testUse_failure() throws Exception {

		// Start HTTP Server and service for failure
		MockGwtServiceInterface service = this.startService("failure");

		// Request to ensure correctly decodes/encodes failure
		try {
			service.service(new Integer(1));
			fail("Should not be successful");
		} catch (MockGwtServiceException ex) {
			assertEquals("Incorrect failure", "TEST", ex.getMessage());
		}
	}

	/**
	 * Ensure failure on no return value.
	 */
	public void testUse_NoReturn() throws Exception {

		// Start HTTP Server and service for no return
		MockGwtServiceInterface service = this.startService("noReturn");

		// Request to ensure correctly decodes/encodes failure
		try {
			service.service(new Integer(1));
			fail("Should not be successful");
		} catch (StatusCodeException ex) {
			assertEquals("Incorrect status code for no return", 204,
					ex.getStatusCode());
		}
	}

	/**
	 * Ensure fails if incorrect response type.
	 */
	public void testUse_IncorrectResponseType() throws Exception {

		// Start HTTP Server and service for incorrect type
		MockGwtServiceInterface service = this.startService("incorrectType");

		// Request to ensure correctly decodes/encodes incorrect type
		String result = service.service(new Integer(1));
		assertEquals("Should send correct type", "CORRECT", result);
	}

	/**
	 * Class to test servicing the {@link ServerGwtRpcConnection}.
	 */
	public static class Service {

		public void success(ServerGwtRpcConnection<String> connection,
				AsyncCallback<String> callback) {
			assertService(connection, callback);
			callback.onSuccess("SUCCESS");
			assertOnlyFirstResponse(callback);
		}

		public void failure(ServerGwtRpcConnection<String> connection,
				AsyncCallback<String> callback) {
			assertService(connection, callback);
			callback.onFailure(new MockGwtServiceException("TEST"));
			assertOnlyFirstResponse(callback);
		}

		public void noReturn(ServerGwtRpcConnection<String> connection,
				AsyncCallback<String> callback) {
			assertService(connection, callback);
			// Provide no return
		}

		public void incorrectType(ServerGwtRpcConnection<Object> connection,
				AsyncCallback<Object> callback) {
			assertService(connection, callback);

			// Specify the required return type
			connection.setReturnType(CharSequence.class);

			// Ensure can not change
			try {
				connection.setReturnType(Integer.class);
				fail("Should not be successful");
			} catch (ServerGwtRpcConnectionException ex) {
				assertEquals(
						"Incorrect exception",
						"java.lang.Integer can not be set as GWT RPC return type, as it does not specialise already specified return type java.lang.CharSequence",
						ex.getMessage());
			}

			// Ensure can specialise required return type
			connection.setReturnType(String.class);

			// Ensure can not send incorrect type
			try {
				callback.onSuccess(new Integer(1));
				fail("Should not successfully send wrong type");
			} catch (ServerGwtRpcConnectionException ex) {
				assertEquals(
						"Incorrect exception",
						"Return value of type java.lang.Integer is not assignable to required return type java.lang.String for GWT RPC",
						ex.getMessage());
			}

			// Ensure can send correct type
			callback.onSuccess("CORRECT");
		}

		public void handleEscalation(@Parameter Throwable cause) {
			serverFailure = cause;
		}
	}

	/**
	 * <p>
	 * Starts the service and proxy to invoke service.
	 * <p>
	 * Necessary to run in server as using {@link SyncProxy}.
	 * 
	 * @param servicingMethodName
	 *            Name of method to service the request.
	 * @return {@link MockGwtServiceInterface} proxy to invoke service.
	 */
	private MockGwtServiceInterface startService(String servicingMethodName)
			throws Exception {

		// Configure the server (run on different port due to URL limitation)
		final int PORT = HttpTestUtil.getAvailablePort();
		HttpServerAutoWireOfficeFloorSource source = new HttpServerAutoWireOfficeFloorSource(
				PORT);
		source.addManagedObject(ServerGwtRpcConnectionManagedObjectSource.class
				.getName(), null, new AutoWire(ServerGwtRpcConnection.class),
				new AutoWire(AsyncCallback.class));
		AutoWireSection section = source.addSection("HANDLE",
				ClassSectionSource.class.getName(), Service.class.getName());

		// Link in URIs to servicing methods
		for (Method method : Service.class.getDeclaredMethods()) {
			String methodName = method.getName();
			source.linkUri("test/" + methodName, section, methodName);
		}

		// Allow handling of failures back to test
		source.linkEscalation(Throwable.class, section, "handleEscalation");

		// Start the server
		source.openOfficeFloor();

		// Obtain the service proxy
		final MockGwtServiceInterface service = (MockGwtServiceInterface) SyncProxy
				.newProxyInstance(MockGwtServiceInterface.class,
						"http://localhost:" + PORT + "/test/",
						servicingMethodName);

		// Return wrapped proxy to fail on server failure first
		return new MockGwtServiceInterface() {
			@Override
			public String service(Integer parameter)
					throws MockGwtServiceException {
				try {
					return service.service(parameter);
				} catch (MockGwtServiceException ex) {
					throw (MockGwtServiceException) ex;
				} catch (InvocationException ex) {
					throw (InvocationException) ex;
				} catch (Throwable ex) {
					// Fail on server failure first
					if (serverFailure != null) {
						fail(serverFailure);
					} else {
						// Fail on service
						fail(ex);
					}
				}
				throw new IllegalStateException(
						"Should never reach here - only for compilation");
			}
		};
	}

	/**
	 * Asserts the service parameters.
	 * 
	 * @param connection
	 *            {@link ServerGwtRpcConnection}.
	 * @param callback
	 *            {@link AsyncCallback}.
	 */
	private static void assertService(ServerGwtRpcConnection<?> connection,
			AsyncCallback<?> callback) {

		// Obtain the expected method
		final Method method;
		try {
			method = MockGwtServiceInterface.class.getMethod("service",
					Integer.class);
		} catch (Exception ex) {
			fail(ex);
			return;
		}

		// Ensure call back and connection are the same
		assertSame("Callback should be the connection", connection, callback);

		// Obtain and verify the RPC request
		RPCRequest rpcRequest = connection.getRpcRequest();
		assertEquals("Incorrect method", method, rpcRequest.getMethod());
		assertEquals("Incorrect number of parameters", 1,
				rpcRequest.getParameters().length);
		assertEquals("Incorrect parameter value", new Integer(1),
				rpcRequest.getParameters()[0]);

		// Obtain and verify the HTTP request
		HttpRequest httpRequest = connection.getHttpRequest();
		assertTrue("Incorrect request URI", httpRequest.getRequestURI()
				.startsWith("/test/"));
	}

	/**
	 * Ensure that only the first response is sent.
	 * 
	 * @param callback
	 *            {@link AsyncCallback}.
	 */
	private static void assertOnlyFirstResponse(AsyncCallback<String> callback) {

		// Ensure can not send another success
		try {
			callback.onSuccess("Should not be able send another success");
			fail("Should not be successful");
		} catch (ServerGwtRpcConnectionException ex) {
			assertEquals("Incorrect another success exception",
					"GWT RPC response already provided", ex.getMessage());
		}

		// Ensure can not send another failure
		try {
			callback.onFailure(new MockGwtServiceException(
					"Should not be able send another failure"));
			fail("Should not be successful");
		} catch (ServerGwtRpcConnectionException ex) {
			assertEquals("Incorrect another failure exception",
					"GWT RPC response already provided", ex.getMessage());
		}
	}

}