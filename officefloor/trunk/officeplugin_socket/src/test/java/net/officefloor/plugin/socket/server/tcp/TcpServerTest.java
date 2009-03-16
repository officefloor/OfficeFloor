/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.plugin.socket.server.tcp;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.ManagedObjectHandlerBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeEnhancer;
import net.officefloor.frame.api.build.OfficeEnhancerContext;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.spi.team.OnePersonTeam;
import net.officefloor.frame.impl.spi.team.PassiveTeam;
import net.officefloor.frame.impl.spi.team.WorkerPerTaskTeam;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveWorkBuilder;
import net.officefloor.frame.test.ReflectiveWorkBuilder.ReflectiveTaskBuilder;
import net.officefloor.plugin.impl.socket.server.ServerSocketHandlerEnum;

/**
 * Tests the {@link TcpServerSocketManagedObjectSource}.
 * 
 * @author Daniel
 */
public class TcpServerTest extends AbstractOfficeConstructTestCase {

	/**
	 * Starting port number.
	 */
	public static int portStart = 12346;

	/**
	 * Port number to use for testing.
	 */
	public static int PORT;

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor;

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.test.AbstractOfficeConstructTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Specify the port
		PORT = portStart;
		portStart++; // increment for next test

		// Obtain the office name
		String officeName = this.getOfficeName();

		// Register the Server Socket Managed Object
		ManagedObjectBuilder<?> serverSocketBuilder = this
				.constructManagedObject("MO",
						TcpServerSocketManagedObjectSource.class, officeName);
		serverSocketBuilder.addProperty(
				TcpServerSocketManagedObjectSource.PROPERTY_PORT, String
						.valueOf(PORT));
		serverSocketBuilder
				.addProperty(
						TcpServerSocketManagedObjectSource.PROPERTY_BUFFER_SIZE,
						"1024");
		serverSocketBuilder.addProperty(
				TcpServerSocketManagedObjectSource.PROPERTY_MESSAGE_SIZE, "3");
		serverSocketBuilder.setDefaultTimeout(3000);

		// Register the necessary teams for socket listening
		this.constructTeam("ACCEPTER_TEAM", new OnePersonTeam(100));
		this.constructTeam("LISTENER_TEAM", new WorkerPerTaskTeam("Listener"));
		this.constructTeam("CLEANUP_TEAM", new PassiveTeam());
		OfficeBuilder officeBuilder = this.getOfficeBuilder();
		officeBuilder.registerTeam("of-MO.serversocket." + PORT
				+ ".Accepter.TEAM", "of-ACCEPTER_TEAM");
		officeBuilder.registerTeam("of-MO.serversocket." + PORT
				+ ".Listener.TEAM", "of-LISTENER_TEAM");
		officeBuilder.registerTeam("of-MO.tcp.connection.cleanup",
				"of-CLEANUP_TEAM");

		// Provide the process managed object to the office
		officeBuilder.addProcessManagedObject("MO", "MO");

		// Register team to do the work
		this.constructTeam("WORKER", new OnePersonTeam(100));

		// Register the work to process messages
		ReflectiveWorkBuilder workBuilder = this.constructWork(
				new MessageWork(), "servicer", "service");
		ReflectiveTaskBuilder taskBuilder = workBuilder.buildTask("service",
				"WORKER");
		taskBuilder.buildObject("P-MO", "MO");
		taskBuilder
				.buildFlow("service", FlowInstigationStrategyEnum.SEQUENTIAL);

		// Link handler to task
		officeBuilder.addOfficeEnhancer(new OfficeEnhancer() {
			@Override
			public void enhanceOffice(OfficeEnhancerContext context) {
				// Obtain the managed object handler builder
				ManagedObjectHandlerBuilder<ServerSocketHandlerEnum> handlerBuilder = context
						.getManagedObjectHandlerBuilder("of-MO",
								ServerSocketHandlerEnum.class);

				// Link in the handler task
				handlerBuilder.registerHandler(
						ServerSocketHandlerEnum.SERVER_SOCKET_HANDLER)
						.linkProcess(0, "servicer", "service");
			}
		});

		// Create and open the Office Floor
		this.officeFloor = this.constructOfficeFloor();
		this.officeFloor.openOfficeFloor();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.test.AbstractOfficeConstructTestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {

		// Close the office
		this.officeFloor.closeOfficeFloor();

		// Clean up
		super.tearDown();
	}

	/**
	 * Ensure able to answer a request.
	 */
	public void testSingleRequest() throws Exception {
		this.doRequests(1, true);
	}

	/**
	 * Ensures able to handle multiple requests.
	 */
	public void testMultipleRequests() throws Exception {
		this.doRequests(10, true);
	}

	/**
	 * Ensures can handle parallel requests.
	 */
	public void testParallelRequests() throws Throwable {
		this.doParallelRequests(3, 10, true);
	}

	/**
	 * Ensures can handle heavy load of requests.
	 */
	public void testHeavyLoad() throws Throwable {
		final int CALLERS = 100;
		final int REQUESTS = 100;
		long startTime = System.currentTimeMillis();
		this.doParallelRequests(CALLERS, REQUESTS, true);
		long endTime = System.currentTimeMillis();
		long runTime = endTime - startTime;

		// Output details of performance
		int totalCalls = CALLERS * REQUESTS;
		long effectiveTimePerCall = (long) ((runTime / (double) totalCalls) * 1000);
		System.out.println("=====================");
		System.out.println(CALLERS + " callers made " + REQUESTS
				+ " requests (total " + totalCalls + ") in " + runTime
				+ " milliseconds");
		System.out.println("Effectively 1 call every " + effectiveTimePerCall
				+ " microseconds");
		System.out.println("=====================");
	}

	/**
	 * Does parallel requesting.
	 * 
	 * @param numberOfCallers
	 *            Number of callers making parallel requests.
	 * @param numberOfRequestsPerCaller
	 *            Number requests each caller makes.
	 * @param isLog
	 *            Flag indicating whether to log details.
	 */
	private void doParallelRequests(int numberOfCallers,
			final int numberOfRequestsPerCaller, final boolean isLog)
			throws Throwable {

		// Create the arrays for managing threads
		final boolean[] isCompleteFlags = new boolean[numberOfCallers];
		final Throwable[] failures = new Throwable[numberOfCallers];

		// Create the callers to run
		for (int i = 0; i < numberOfCallers; i++) {

			// Indicate caller index
			final int callerIndex = i;

			// Create the caller
			Runnable caller = new Runnable() {
				@Override
				public void run() {
					try {

						// Execute the requested number of calls
						TcpServerTest.this.doRequests(
								numberOfRequestsPerCaller, isLog);

					} catch (Throwable ex) {
						// Record failure of caller
						synchronized (isCompleteFlags) {
							failures[callerIndex] = ex;
						}
					} finally {
						// Flag caller complete
						synchronized (isCompleteFlags) {
							isCompleteFlags[callerIndex] = true;
						}
					}
				}
			};

			// Start the caller
			new Thread(caller).start();
		}

		// Wait for all callers to be complete
		boolean isComplete = false;
		while (!isComplete) {
			Thread.sleep(100);
			isComplete = true;
			synchronized (isCompleteFlags) {
				// Determine if complete
				for (boolean isCompleteFlag : isCompleteFlags) {
					if (!isCompleteFlag) {
						isComplete = false;
					}
				}
			}
		}

		// Propagate first caller failure
		synchronized (isCompleteFlags) {
			for (Throwable failure : failures) {
				if (failure != null) {
					throw failure;
				}
			}
		}
	}

	/**
	 * Do the requests.
	 * 
	 * @param numberOfRequests
	 *            Number of requests to make.
	 * @param isLog
	 *            Flag indicating whether to log details.
	 */
	private void doRequests(int numberOfRequests, boolean isLog)
			throws Exception {

		// Open socket to Office
		Socket socket = new Socket();
		socket.connect(new InetSocketAddress(InetAddress.getLocalHost(), PORT),
				5000);

		// Obtain the streams
		OutputStream outputStream = socket.getOutputStream();
		InputStream inputStream = socket.getInputStream();

		// Loop sending the messages
		for (int i = 0; i < numberOfRequests; i++) {

			// Obtain the index to send
			int requestIndex = (i % Messages.getSize());

			// Send the index
			long startTime = System.currentTimeMillis();
			outputStream.write(new byte[] { (byte) requestIndex });
			outputStream.flush();

			// Read a response (ends with 0 byte)
			ByteArrayOutputStream response = new ByteArrayOutputStream();
			for (int value; (value = inputStream.read()) != -1;) {
				if (value == 0) {
					break;
				}
				response.write(value);
			}
			long endTime = System.currentTimeMillis();
			String responseText = new String(response.toByteArray());
			if (isLog) {
				System.out.println("Message [" + requestIndex
						+ "] processed in " + (endTime - startTime)
						+ " milli-seconds (returned response '" + responseText
						+ "')");
			}

			// Ensure response correct
			assertEquals("Incorrect response", Messages
					.getMessage(requestIndex), responseText);
		}

		// Send finished
		long startTime = System.currentTimeMillis();
		outputStream.write(new byte[] { -1 });
		outputStream.flush();

		// Ensure no further data (and closed connection)
		assertEquals("Connection should be closed", -1, inputStream.read());

		// Indicate time to close connection
		long endTime = System.currentTimeMillis();
		System.out.println("Message [-1] processed in " + (endTime - startTime)
				+ " milli-seconds");
	}

}