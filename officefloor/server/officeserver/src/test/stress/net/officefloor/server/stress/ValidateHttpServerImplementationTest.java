/*-
 * #%L
 * HTTP Server
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

package net.officefloor.server.stress;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.Charset;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import net.officefloor.server.http.AbstractHttpServerImplementationTestCase;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpServerImplementation;
import net.officefloor.server.http.HttpServerLocation;

/**
 * Validates the {@link AbstractHttpServerImplementationTestCase}.
 * 
 * @author Daniel Sagenschneider
 */
public class ValidateHttpServerImplementationTest extends AbstractHttpServerImplementationTestCase {

	/*
	 * =================== AbstractHttpServerImplementationTest =================
	 */

	@Override
	protected Class<? extends HttpServerImplementation> getHttpServerImplementationClass() {
		return ValidateHttpServerImplementation.class;
	}

	@Override
	protected AutoCloseable startRawHttpServer(HttpServerLocation serverLocation) throws Exception {
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
		return () -> server.stop();
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
