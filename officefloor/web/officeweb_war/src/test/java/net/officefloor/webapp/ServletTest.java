package net.officefloor.webapp;

import java.util.Arrays;

import org.apache.catalina.connector.Connector;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.woof.compile.CompileWoof;
import net.officefloor.woof.mock.MockWoofServer;

/**
 * Ensure can provide {@link OfficeFloor} {@link Connector}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletTest extends OfficeFrameTestCase {

	private static final ObjectMapper mapper = new ObjectMapper();

	/**
	 * Ensure can service simple GET.
	 */
	public void testSimpleGet() throws Exception {
		try (MockWoofServer server = new CompileWoof(true).open()) {
			MockHttpResponse response = server.send(MockHttpServer.mockRequest("/posts"));
			response.assertResponse(200, mapper.writeValueAsString(Arrays.asList()), "Content-Type",
					"application/json");
		}
	}

}