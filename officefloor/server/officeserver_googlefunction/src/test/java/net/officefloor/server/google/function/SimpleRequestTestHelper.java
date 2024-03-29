package net.officefloor.server.google.function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.officefloor.compile.impl.ApplicationOfficeFloorSource;
import net.officefloor.compile.spi.office.extension.OfficeExtensionService;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionService;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.server.google.function.wrap.AbstractSetupGoogleHttpFunctionJUnit;
import net.officefloor.server.http.HttpClientTestUtil;
import net.officefloor.server.http.HttpServer;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;

/**
 * Test helper to send simple requests.
 */
public class SimpleRequestTestHelper {

	private static final ObjectMapper mapper = new ObjectMapper();

	/**
	 * Loads the application into the {@link AbstractSetupGoogleHttpFunctionJUnit}.
	 * 
	 * @param <J>   Type of {@link AbstractSetupGoogleHttpFunctionJUnit}.
	 * @param setup {@link AbstractSetupGoogleHttpFunctionJUnit}.
	 * @return Input {@link AbstractSetupGoogleHttpFunctionJUnit}.
	 */
	public static <J extends AbstractSetupGoogleHttpFunctionJUnit<J>> J loadApplication(J setup) {
		return setup.officeFloor(getOfficeFloorExtension()).office(getOfficeExtension(Servicer.class));
	}

	public static class Servicer {
		public void service(ServerHttpConnection connection) throws IOException {
			mapper.writeValue(connection.getResponse().getEntityWriter(), new MockDataTransferObject("MOCK RESPONSE"));
		}
	}

	/**
	 * Loads the async application into the
	 * {@link AbstractSetupGoogleHttpFunctionJUnit}.
	 * 
	 * @param <J>   Type of {@link AbstractSetupGoogleHttpFunctionJUnit}.
	 * @param setup {@link AbstractSetupGoogleHttpFunctionJUnit}.
	 * @return Input {@link AbstractSetupGoogleHttpFunctionJUnit}.
	 */
	public static <J extends AbstractSetupGoogleHttpFunctionJUnit<J>> J loadAsyncApplication(J setup) {
		return setup.officeFloor(getOfficeFloorExtension()).office(getOfficeExtension(AsyncServicer.class));
	}

	public static class AsyncServicer {
		public void service(AsynchronousFlow async, ServerHttpConnection connection) {
			new Thread(() -> {

				// Ensure asynchronously completes sometime later
				try {
					Thread.sleep(10);
				} catch (InterruptedException ex) {
					// Ignore
				}

				// Complete the flow
				async.complete(() -> {
					connection.getResponse().getEntityWriter().write("MOCK ASYNC RESPONSE");
				});

			}).start();
		}
	}

	/**
	 * Obtains the {@link OfficeFloorExtensionService}.
	 * 
	 * @return {@link OfficeFloorExtensionService}.
	 */
	public static OfficeFloorExtensionService getOfficeFloorExtension() {
		return (deployer, context) -> {

			// Configure the HTTP server
			DeployedOfficeInput input = deployer.getDeployedOffice(ApplicationOfficeFloorSource.OFFICE_NAME)
					.getDeployedOfficeInput("SERVICE", "service");
			HttpServer server = new HttpServer(input, deployer, context);

			// Ensure using appropriate implementation
			assertEquals(GoogleFunctionHttpServerImplementation.class, server.getHttpServerImplementation().getClass(),
					"Should load " + GoogleFunctionHttpServerImplementation.class.getSimpleName());

		};
	}

	/**
	 * Obtains the {@link OfficeExtensionService}.
	 * 
	 * @param sectionClass {@link Class} for {@link ClassSectionSource} to service
	 *                     request.
	 * @return {@link OfficeExtensionService}.
	 */
	public static OfficeExtensionService getOfficeExtension(Class<?> sectionClass) {
		return (architect, context) -> {

			// Configure the servicing
			architect.addOfficeSection("SERVICE", ClassSectionSource.class.getName(), sectionClass.getName());
		};
	}

	/**
	 * Ensure can send request via socket.
	 */
	public static void assertRequest() {
		try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient()) {

			// Undertake request
			HttpPost request = new HttpPost("http://localhost:7878");
			String requestEntity = mapper.writeValueAsString(new MockDataTransferObject("MOCK REQUEST"));
			request.setEntity(new StringEntity(requestEntity));
			HttpResponse response = client.execute(request);

			// Ensure appropriate response
			String responseEntity = EntityUtils.toString(response.getEntity());
			assertEquals(200, response.getStatusLine().getStatusCode(), "Should be successful: " + responseEntity);
			MockDataTransferObject responseEntityObject = mapper.readValue(responseEntity,
					MockDataTransferObject.class);
			assertEquals("MOCK RESPONSE", responseEntityObject.getText(), "Incorrect response");

		} catch (Exception ex) {
			fail(ex);
			throw new IllegalStateException("fail should propagate failure");
		}
	}

	/**
	 * Ensure can send request via socket.
	 */
	public static void assertSecureRequest() {
		try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient(true)) {

			// Undertake request
			HttpPost request = new HttpPost("https://localhost:7979");
			String requestEntity = mapper.writeValueAsString(new MockDataTransferObject("MOCK REQUEST"));
			request.setEntity(new StringEntity(requestEntity));
			HttpResponse response = client.execute(request);

			// Ensure appropriate response
			String responseEntity = EntityUtils.toString(response.getEntity());
			assertEquals(200, response.getStatusLine().getStatusCode(), "Should be successful: " + responseEntity);
			MockDataTransferObject responseEntityObject = mapper.readValue(responseEntity,
					MockDataTransferObject.class);
			assertEquals("MOCK RESPONSE", responseEntityObject.getText(), "Incorrect response");

		} catch (Exception ex) {
			fail(ex);
			throw new IllegalStateException("fail should propagate failure");
		}
	}

	/**
	 * Asserts simple request to {@link MockHttpServer}.
	 * 
	 * @param mockHttpServer {@link MockHttpServer}.
	 */
	public static void assertMockRequest(MockHttpServer mockHttpServer) {
		MockHttpResponse response = mockHttpServer
				.send(MockHttpServer.mockJsonRequest(new MockDataTransferObject("MOCK REQUEST")));
		response.assertJson(200, new MockDataTransferObject("MOCK RESPONSE"));
	}

	/**
	 * All access vis static methods.
	 */
	private SimpleRequestTestHelper() {
	}

}
