package net.officefloor.webapp;

import org.apache.catalina.connector.Connector;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.war.WarAwareClassLoaderFactoryTest;
import net.officefloor.woof.compile.CompileWoof;
import net.officefloor.woof.mock.MockWoofServer;

/**
 * Ensure can provide {@link OfficeFloor} {@link Connector}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletTest extends OfficeFrameTestCase {

	/**
	 * Ensure can service simple GET.
	 */
	public void testSimpleGet() throws Exception {
		String webAppPath = WarAwareClassLoaderFactoryTest.getWarFile().getAbsolutePath();
		try (MockWoofServer server = new CompileWoof(true)
				.open(WebAppOfficeFloorCompilerConfigurationServiceFactory.PROPERTY_WEB_APP_PATH, webAppPath)) {
			MockHttpResponse response = server.send(MockHttpServer.mockRequest("/simple"));
			response.assertResponse(200, "SIMPLE");
		}
	}

}