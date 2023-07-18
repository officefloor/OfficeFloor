package net.officefloor.server.google.function;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.compile.impl.ApplicationOfficeFloorSource;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.server.google.function.wrap.AbstractSetupGoogleHttpFunctionJUnit;
import net.officefloor.server.http.HttpClientExtension;
import net.officefloor.server.http.HttpServer;
import net.officefloor.server.http.HttpServerImplementation;
import net.officefloor.server.http.OfficeFloorHttpServerImplementation;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.test.OfficeFloorExtension;

/**
 * Test functionality for extending with {@link HttpServer}.
 */
public abstract class AbstractExtendWithHttpServerTestCase {

	/**
	 * Extends {@link AbstractSetupGoogleHttpFunctionJUnit} with {@link HttpServer}.
	 * 
	 * @param <J>      Test type.
	 * @param testCase {@link AbstractSetupGoogleHttpFunctionJUnit}.
	 * @return {@link AbstractSetupGoogleHttpFunctionJUnit}.
	 */
	protected static <J extends AbstractSetupGoogleHttpFunctionJUnit<J>> J extendWithHttpServer(J testCase) {
		return testCase.officeFloor((deployer, context) -> {

			// Configure the HTTP server
			DeployedOfficeInput input = deployer.getDeployedOffice(ApplicationOfficeFloorSource.OFFICE_NAME)
					.getDeployedOfficeInput("SERVICE", "service");
			HttpServer server = new HttpServer(input, deployer, context);

			// Ensure using default HTTP server implementation
			assertEquals(OfficeFloorHttpServerImplementation.class, server.getHttpServerImplementation().getClass(),
					"Should use default " + HttpServerImplementation.class.getSimpleName());
		}).office((architect, context) -> {

			// Configure service handling
			architect.enableAutoWireObjects();
			architect.addOfficeSection("SERVICE", ClassSectionSource.class.getName(), Servicer.class.getName());
		});
	}

	public static class Servicer {
		public void service(ServerHttpConnection connection) throws Exception {
			connection.getResponse().getEntityWriter().write("SUCCESSFUL");
		}
	}

	private static final @RegisterExtension @Order(1) OfficeFloorExtension officeFloor = new OfficeFloorExtension();

	public static final @RegisterExtension HttpClientExtension client = new HttpClientExtension();

	/**
	 * Ensure can make request on configured {@link HttpServer}.
	 */
	@Test
	public void requestOnAdditionalHttpServer() throws Exception {
		HttpResponse response = client.execute(new HttpGet("http://localhost:7878"));
		String entity = EntityUtils.toString(response.getEntity());
		assertEquals(200, response.getStatusLine().getStatusCode(), "Should be successful: " + entity);
		assertEquals("SUCCESSFUL", entity, "Incorrect response entity");
	}

}
