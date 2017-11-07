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
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.spi.team.ExecutorCachedTeamSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.test.ThreadSafeClosure;
import net.officefloor.plugin.managedfunction.clazz.FlowInterface;
import net.officefloor.plugin.section.clazz.Parameter;
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

	/**
	 * {@link TestThread} instances used for testing.
	 */
	private Deque<TestThread> testThreads = new ConcurrentLinkedDeque<>();

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

			// Configure threading
			if (servicerClass == ThreadedServicer.class) {
				context.addManagedObject("MANAGED_OBJECT", ThreadedManagedObject.class, ManagedObjectScope.THREAD);
				context.getOfficeFloorDeployer().addTeam("THREADED", ExecutorCachedTeamSource.class.getName())
						.addTypeQualification(null, ThreadedManagedObject.class.getName());
			}
		});
		compile.office((context) -> {
			context.getOfficeArchitect().enableAutoWireTeams();
			context.addSection("SECTION", servicerClass);
		});
		this.officeFloor = compile.compileAndOpenOfficeFloor();
	}

	public static class Servicer {
		public void service(ServerHttpConnection connection) throws IOException {
			connection.getResponse().getEntityWriter().write("hello world");
		}
	}

	public static class ThreadedManagedObject {
	}

	public static class ThreadedServicer {

		@FlowInterface
		public static interface Flows {
			void doFlow(Thread thread);
		}

		public void service(Flows flows) {
			flows.doFlow(Thread.currentThread());
		}

		public void doFlow(@Parameter Thread thread, ServerHttpConnection connection,
				ThreadedManagedObject managedObject) throws IOException {
			assertNotSame("Should be different threads", Thread.currentThread(), thread);
			connection.getResponse().getEntityWriter().write("hello world");
		}
	}

	@Override
	protected void tearDown() throws Exception {

		// Close the OfficeFloor
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
		}

		// Ensure all threads are complete
		long startTime = System.currentTimeMillis();
		for (TestThread testThread : this.testThreads) {
			testThread.waitForCompletion(startTime);
		}

		// Complete tear down
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
		this.doPipelineRun("Single pipeline", 100000);
	}

	/**
	 * Ensure can service multiple clients.
	 */
	public void testMultipleClients() throws Exception {

		// Start server
		this.startServer(Servicer.class);

		// Warm up
		this.doPipelineRun("WARMUP", 10000);

		// Stress with multiple clients
		TestThread[] clients = new TestThread[100];
		for (int i = 0; i < clients.length; i++) {
			final int index = i;
			TestThread client = new TestThread(() -> {
				try {
					this.doPipelineRun("Multiple Client " + index, 10000);
				} catch (Exception ex) {
					throw fail(ex);
				}
			});
			clients[i] = client;
			client.start();
		}

		// Wait for completion
		long startTime = System.currentTimeMillis();
		for (TestThread client : clients) {
			client.waitForCompletion(startTime, 10);
		}
	}

	/**
	 * Ensure can service with {@link ThreadedServicer}.
	 */
	public void testServiceThreadedPipeline() throws Exception {

		// Start server
		this.startServer(ThreadedServicer.class);

		// Warm up
		this.doPipelineRun("WARMUP", 1000);

		// Stress
		this.doPipelineRun("Single pipeline with threading", 100000);
	}

	/**
	 * Ensure can service multiple clients with {@link ThreadedServicer}.
	 */
	public void testMultipleClientsWithThreadedServicer() throws Exception {

		// Start server
		this.startServer(ThreadedServicer.class);

		// Warm up
		this.doPipelineRun("WARMUP", 1000);

		// Stress with multiple clients
		TestThread[] clients = new TestThread[100];
		for (int i = 0; i < clients.length; i++) {
			final int index = i;
			TestThread client = new TestThread(() -> {
				try {
					this.doPipelineRun("Multiple Client with threaded servicing " + index, 100);
				} catch (Exception ex) {
					throw fail(ex);
				}
			});
			clients[i] = client;
			client.start();
		}

		// Wait for completion
		long startTime = System.currentTimeMillis();
		for (TestThread client : clients) {
			client.waitForCompletion(startTime, 10);
		}
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
		MockHttpRequestBuilder request = MockHttpServer.mockRequest();
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

	/**
	 * {@link Thread} for testing.
	 */
	private class TestThread extends Thread {

		private final Runnable runnable;

		private Object completion = null;

		public TestThread(Runnable runnable) {
			this.runnable = runnable;

			// Register this test thread
			MockHttpServerStressTest.this.testThreads.add(this);
		}

		private void waitForCompletion(long startTime) {
			this.waitForCompletion(startTime, 3);
		}

		private synchronized void waitForCompletion(long startTime, int secondsToRun) {
			while (this.completion == null) {

				// Determine if timed out
				MockHttpServerStressTest.this.timeout(startTime, secondsToRun);

				// Not timed out, so wait a little longer
				try {
					this.wait(10);
				} catch (InterruptedException ex) {
					throw MockHttpServerStressTest.fail(ex);
				}
			}

			// Determine if failure
			if (this.completion instanceof Throwable) {
				throw fail((Throwable) this.completion);
			}
		}

		@Override
		public void run() {
			// Run
			try {
				this.runnable.run();
			} catch (Throwable ex) {
				synchronized (this) {
					this.completion = ex;
				}
			} finally {
				// Notify complete
				synchronized (this) {
					if (this.completion == null) {
						this.completion = "COMPLETE";
					}
					this.notify();
				}
			}
		}
	}

}