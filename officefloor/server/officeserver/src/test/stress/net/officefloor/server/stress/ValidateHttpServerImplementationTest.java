package net.officefloor.server.stress;

import java.io.IOException;
import java.nio.charset.Charset;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import net.officefloor.server.http.AbstractHttpServerImplementationTest;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpServerImplementation;
import net.officefloor.server.http.HttpServerLocation;

/**
 * Validates the {@link AbstractHttpServerImplementationTest}.
 * 
 * @author Daniel Sagenschneider
 */
public class ValidateHttpServerImplementationTest extends AbstractHttpServerImplementationTest<Server> {

	/*
	 * =================== AbstractHttpServerImplementationTest =================
	 */

	@Override
	protected Class<? extends HttpServerImplementation> getHttpServerImplementationClass() {
		return ValidateHttpServerImplementation.class;
	}

	@Override
	protected Server startRawHttpServer(HttpServerLocation serverLocation) throws Exception {
		Server server = ValidateHttpServerImplementation.createServer(serverLocation, null);
		byte[] helloWorld = "hello world".getBytes(Charset.forName("UTF-8"));
		server.setHandler(new AbstractHandler() {
			@Override
			public void handle(String target, Request baseRequest, HttpServletRequest request,
					HttpServletResponse response) throws IOException, ServletException {
				response.setStatus(HttpServletResponse.SC_OK);
				response.setHeader("Date", "NOW");
				response.setHeader("Server", "OfficeFloorServer Jetty");
				response.setHeader("Expires", "Thu, 01 Jan 1970 00:00:00 GMT");
				response.setContentType("text/plain");
				response.getOutputStream().write(helloWorld);
				baseRequest.setHandled(true);
			}
		});
		server.start();
		return server;
	}

	@Override
	protected void stopRawHttpServer(Server momento) throws Exception {
		momento.stop();
	}

	@Override
	protected HttpHeader[] getServerResponseHeaderValues() {
		return new HttpHeader[] { newHttpHeader("Date", "NOW"), newHttpHeader("Server", "OfficeFloorServer Jetty"),
				newHttpHeader("Expires", "Thu, 01 Jan 1970 00:00:00 GMT"), newHttpHeader("Content-Type", "text/plain"),
				newHttpHeader("Content-Length", "?") };
	}

	@Override
	protected String getServerNameSuffix() {
		return "Jetty";
	}

	@Override
	protected boolean isHandleCancel() {
		return false;
	}

	/**
	 * Ensures the multi-client pipeline test runs appropriately.
	 */
	public void testMultiClientPipeline() throws Exception {
		this.doMultiClientLoadTest(BufferServicer.class, 2, 100, "Validate");
	}

	/**
	 * Ensure validate results.
	 */
	public void testValidateResults() throws Exception {
		PipelineResult result = new PipelineResult(0, 1000, 1);
		assertFalse(CompareResult.setResult("MOCK", null, result));
		assertFalse(CompareResult.setResult("MOCK", BytesServicer.class, result));
		assertFalse(CompareResult.setResult("MOCK", BufferServicer.class, result));
		assertTrue(CompareResult.setResult("MOCK", FileServicer.class, result));
	}

}