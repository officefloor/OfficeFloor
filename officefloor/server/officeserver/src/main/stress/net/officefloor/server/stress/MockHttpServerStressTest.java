/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.server.stress;

import java.io.IOException;

import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.test.ThreadSafeClosure;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.impl.ProcessAwareServerHttpConnectionManagedObject;
import net.officefloor.server.http.mock.MockHttpRequestBuilder;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;

/**
 * Stress tests the {@link ProcessAwareServerHttpConnectionManagedObject}
 * servicing within the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockHttpServerStressTest extends OfficeFrameTestCase {

	/**
	 * {@link MockHttpServer}.
	 */
	private MockHttpServer server;

	/**
	 * {@link OfficeFloor}
	 */
	private OfficeFloor officeFloor;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Log GC
		this.setLogGC();
	}

	/**
	 * Starts the {@link OfficeFloor}.
	 * 
	 * @param servicerClass
	 *            {@link Class} of the servicer.
	 */
	private void startServer(Class<?> servicerClass) throws Exception {

		// Compile the OfficeFloor
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.officeFloor((context) -> {

			// Obtain the input
			DeployedOfficeInput serviceInput = context.getDeployedOffice().getDeployedOfficeInput("SECTION", "service");

			// Configure the mock server
			this.server = MockHttpServer.configureMockHttpServer(serviceInput);
		});
		compile.office((context) -> {
			context.addSection("SECTION", servicerClass);
		});
		this.officeFloor = compile.compileAndOpenOfficeFloor();
	}

	public static class Servicer {
		public void service(ServerHttpConnection connection) throws IOException {
			connection.getHttpResponse().getEntityWriter().write("hello world");
		}
	}

	@Override
	protected void tearDown() throws Exception {
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
		}
		super.tearDown();
	}

	/**
	 * Ensure can service on same {@link Thread}.
	 */
	public void testServicePipeline() throws Exception {

		// Start server
		this.startServer(Servicer.class);

		// Warm up
		this.doPipelineRun("WARMUP", 10000);

		// Stress
		this.doPipelineRun("Single pipeline", 1000000);
	}

	/**
	 * Undertakes the pipeline run.
	 * 
	 * @param prefix
	 *            Prefix for results.
	 * @param requestCount
	 *            Number of requests.
	 */
	private void doPipelineRun(String prefix, int requestCount) throws InterruptedException {

		// Create the request
		MockHttpRequestBuilder request = this.server.createMockHttpRequest();
		request.setEfficientForStressTests(true);

		// Undertake the run
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < requestCount; i++) {
			ThreadSafeClosure<MockHttpResponse> listener = new ThreadSafeClosure<>();
			this.server.send(request, (mockResponse) -> {
				listener.set(mockResponse);
			});
			MockHttpResponse response = listener.waitAndGet();
			assertNotNull("Should have response on single thread servicing", response);
			assertSame("Incorrect status", HttpStatus.OK, response.getHttpStatus());
			assertEquals("Incorrect response entity", "hello world",
					response.getHttpEntity(ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET));
		}
		long runTime = (System.currentTimeMillis() - startTime);
		System.out.println(prefix + " serviced " + requestCount + " requests in " + runTime
				+ " milliseconds (effectively " + ((((float) requestCount) / runTime) * 1000) + " per second)");
	}

}