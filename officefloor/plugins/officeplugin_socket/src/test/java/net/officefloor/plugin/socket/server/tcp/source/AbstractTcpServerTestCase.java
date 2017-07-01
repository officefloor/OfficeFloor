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
package net.officefloor.plugin.socket.server.tcp.source;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.text.DecimalFormat;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.plugin.socket.server.http.HttpTestUtil;
import net.officefloor.plugin.socket.server.tcp.ServerTcpConnection;

/**
 * Provides abstract functionality to test TCP connections.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractTcpServerTestCase extends AbstractOfficeConstructTestCase {

	/**
	 * Formatter of millisecond time.
	 */
	private static final DecimalFormat FORMATTER = new DecimalFormat("0.00");

	/**
	 * Port number to use for testing.
	 */
	private static int PORT;

	/**
	 * Registers the {@link ManagedObjectSource} to handle connections.
	 * 
	 * @param port
	 *            Port to bind to receive client connections.
	 * @param managedObjectName
	 *            Name to make the {@link ManagedObject} available under.
	 * @param functionName
	 *            Name of {@link ManagedFunction} to process the
	 *            {@link ServerTcpConnection}.
	 */
	protected abstract void registerManagedObjectSource(int port, String managedObjectName, String functionName);

	/**
	 * Creates the client {@link Socket} connected to the
	 * {@link ManagedObjectSource}.
	 * 
	 * @param address
	 *            {@link InetAddress}.
	 * @param port
	 *            Port.
	 * @return Client {@link Socket}.
	 * @throws Exception
	 *             If fails to create connected {@link Socket}.
	 */
	protected abstract Socket createClientSocket(InetAddress address, int port) throws Exception;

	/**
	 * Helper method to register a {@link Team} for the
	 * {@link ManagedObjectSource}.
	 * 
	 * @param managedObjectName
	 *            Name of the {@link ManagedObject}.
	 * @param managedObjectTeamName
	 *            Name of the {@link Team} for the {@link ManagedObjectSource}.
	 * @param team
	 *            {@link Team} to register.
	 */
	protected void constructManagedObjectSourceTeam(String managedObjectName, String managedObjectTeamName, Team team) {
		this.constructTeam(managedObjectTeamName, team);
		this.getOfficeBuilder().registerTeam("of-" + managedObjectName + "." + managedObjectTeamName,
				"of-" + managedObjectTeamName);
	}

	/*
	 * ==================== TestCase =========================================
	 */

	@Override
	protected final void setUp() throws Exception {
		super.setUp();

		// Always output details
		this.setVerbose(true);

		// Specify the port
		PORT = HttpTestUtil.getAvailablePort();

		// Names to handle ServerTcpConnection
		final String MO_NAME = "MO";
		final String FUNCTION_NAME = "service";

		// Register the function to process messages
		ReflectiveFunctionBuilder functionBuilder = this.constructFunction(new MessageFunction(), FUNCTION_NAME);
		functionBuilder.buildObject(MO_NAME);

		// Register the managed object source to handle TCP connection
		this.registerManagedObjectSource(PORT, MO_NAME, FUNCTION_NAME);

		// Create and open the Office Floor
		OfficeFloor officeFloor = this.constructOfficeFloor();
		officeFloor.openOfficeFloor();
	}

	/**
	 * Ensure able to answer a request.
	 */
	public void testSingleRequest() throws Exception {
		this.doRequests("", 1, true);
	}

	/**
	 * Ensures able to handle multiple requests.
	 */
	public void testMultipleRequests() throws Exception {
		this.doRequests("", 100, true);
	}

	/**
	 * Ensures can handle parallel requests.
	 */
	@StressTest
	public void testParallelRequests() throws Throwable {
		this.doParallelRequests(3, 10, true);
	}

	/**
	 * Ensures can handle heavy load of requests.
	 */
	@StressTest
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
		System.out.println(CALLERS + " callers made " + REQUESTS + " requests (total " + totalCalls + ") in " + runTime
				+ " milliseconds");
		System.out.println("Effectively 1 call every " + effectiveTimePerCall + " microseconds");
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
	private void doParallelRequests(int numberOfCallers, final int numberOfRequestsPerCaller, final boolean isLog)
			throws Throwable {

		// Create the arrays for managing threads
		final boolean[] isCompleteFlags = new boolean[numberOfCallers];
		final Throwable[] failures = new Throwable[numberOfCallers];

		// Create the callers to run
		final boolean[] startFlag = new boolean[1];
		startFlag[0] = false;
		for (int i = 0; i < numberOfCallers; i++) {

			// Indicate caller index
			final int callerIndex = i;

			// Create the caller
			Runnable caller = new Runnable() {
				@Override
				public void run() {
					try {

						// Wait on the start flag
						synchronized (startFlag) {
							if (!startFlag[0]) {
								startFlag.wait();
							}
						}

						// Execute the requested number of calls
						AbstractTcpServerTestCase.this.doRequests(String.valueOf(callerIndex),
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

		// Start the callers
		Thread.yield(); // allow request threads to start
		synchronized (startFlag) {
			startFlag[0] = true;
			startFlag.notifyAll();
		}

		// Wait for all callers to be complete
		boolean isComplete = false;
		while (!isComplete) {

			// Allow processing to complete
			Thread.sleep(100);

			// Determine if complete
			isComplete = true;
			synchronized (isCompleteFlags) {
				for (boolean isCompleteFlag : isCompleteFlags) {
					if (!isCompleteFlag) {
						isComplete = false;
					}
				}
			}
		}

		// Propagate first caller failure
		synchronized (isCompleteFlags) {

			// Provide a report on which completed
			if (isLog) {
				System.out.println("Completed:");
				for (int i = 0; i < failures.length; i++) {
					System.out.println("   " + i
							+ (failures[i] == null ? " successful"
									: " failed: " + failures[i].getMessage() + " ["
											+ failures[i].getClass().getSimpleName() + "]"));
					if (failures[i] != null) {
						failures[i].printStackTrace(System.out);
					}
				}
			}

			// Propagate the first failure
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
	 * @param requesterId
	 *            Id identifying the requester.
	 * @param numberOfRequests
	 *            Number of requests to make.
	 * @param isLog
	 *            Flag indicating whether to log details.
	 */
	private void doRequests(String requesterId, int numberOfRequests, boolean isLog) throws Exception {

		// Open socket to Office
		Socket socket = this.createClientSocket(InetAddress.getLocalHost(), PORT);
		socket.setSoTimeout(20000); // timeout reads

		// Obtain the streams
		OutputStream outputStream = socket.getOutputStream();
		InputStream inputStream = socket.getInputStream();

		// Loop sending the messages
		for (int i = 0; i < numberOfRequests; i++) {

			// Obtain the index to send
			int requestIndex = (i % Messages.getSize());

			// Send the index
			long startTime = System.nanoTime();
			outputStream.write(new byte[] { (byte) requestIndex });
			outputStream.flush();

			// Allow other processing
			Thread.yield();

			// Read a response (ends with 0 byte)
			ByteArrayOutputStream response = new ByteArrayOutputStream();
			for (int value; (value = inputStream.read()) != -1;) {
				if (value == 0) {
					break;
				}
				response.write(value);
			}
			long endTime = System.nanoTime();
			String responseText = new String(response.toByteArray());
			if (isLog) {
				System.out.println("Message [" + requesterId + ":" + i + "] processed in "
						+ FORMATTER.format((endTime - startTime) / 1000000.0) + " milliseconds (returned response '"
						+ responseText + "')");
			}

			// Ensure response correct
			assertEquals("Incorrect response", Messages.getMessage(requestIndex), responseText);
		}

		// Send finished
		long startTime = System.currentTimeMillis();
		outputStream.write(new byte[] { -1 });
		outputStream.flush();

		// Ensure no further data (and closed connection)
		assertEquals("Connection should be closed", -1, inputStream.read());

		// Indicate time to close connection
		long endTime = System.currentTimeMillis();
		if (isLog) {
			System.out.println("Message [-1] processed in " + (endTime - startTime) + " milliseconds");
		}
	}

}