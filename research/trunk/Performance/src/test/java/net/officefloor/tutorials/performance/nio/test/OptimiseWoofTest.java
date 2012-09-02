/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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
package net.officefloor.tutorials.performance.nio.test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import junit.framework.TestCase;
import net.officefloor.frame.api.profile.ProfiledJob;
import net.officefloor.frame.api.profile.ProfiledProcess;
import net.officefloor.frame.api.profile.ProfiledThread;
import net.officefloor.frame.api.profile.Profiler;
import net.officefloor.plugin.woof.WoofOfficeFloorSource;
import net.officefloor.tutorials.performance.JettyServicer;
import net.officefloor.tutorials.performance.Servicer;
import net.officefloor.tutorials.performance.logic.HttpServletServicer;
import net.officefloor.tutorials.performance.logic.ServiceLogic;
import net.officefloor.tutorials.performance.nio.Request;
import net.officefloor.tutorials.performance.nio.RunListener;
import net.officefloor.tutorials.performance.nio.Runner;

/**
 * Profiles WoOF to enable optimising.
 * 
 * @author Daniel Sagenschneider
 */
public class OptimiseWoofTest extends TestCase {

	/**
	 * Runs {@link Request} against WoOF profiling the handling.
	 */
	public void testWoof() throws Exception {

		OptimiseProfile profiler = new OptimiseProfile();
		ServiceLogic.runnable = profiler;

		// Start WoOF
		WoofOfficeFloorSource source = new WoofOfficeFloorSource();
		source.setProfiler(profiler);
		WoofOfficeFloorSource.run(source);

		try {
			// Run requests against WoOF
			this.doRequests(7878, profiler);

		} finally {
			WoofOfficeFloorSource.stop();
		}
	}

	/**
	 * Runs {@link Request} against Jetty profiling the handling.
	 */
	public void testJetty() throws Exception {

		OptimiseProfile profiler = new OptimiseProfile();
		HttpServletServicer.runnable = profiler;

		// Start Jetty
		Servicer servicer = new JettyServicer();
		servicer.start();

		try {
			// Run requests against Jetty
			this.doRequests(servicer.getPort(), profiler);

		} finally {
			WoofOfficeFloorSource.stop();
		}
	}

	/**
	 * Tests performance between buffering to byte array or writing directly to
	 * {@link ByteBuffer}.
	 */
	public void testBufferPerformanceDifference() {

		// Buffers
		final int SIZE = 32;
		final int REPEAT = 1000;
		byte[] array = new byte[SIZE];
		byte[] data = new byte[] { 1 };
		ByteBuffer directBuffer = ByteBuffer.allocateDirect(SIZE);
		ByteBuffer heapBuffer = ByteBuffer.allocate(SIZE);

		for (int o = 0; o < 200; o++) {

			// Separate optimised results
			System.out.println();

			// Performance writing bytes to direct buffer
			long startTime = System.nanoTime();
			for (int r = 0; r < REPEAT; r++) {
				ByteBuffer working = directBuffer.duplicate();
				for (int i = 0; i < SIZE; i++) {
					working.put((byte) 1);
				}
			}
			long endTime = System.nanoTime();
			System.out.println((endTime - startTime) + "ns by byte to direct");

			// Performance writing single byte array to direct buffer
			startTime = System.nanoTime();
			for (int r = 0; r < REPEAT; r++) {
				ByteBuffer working = directBuffer.duplicate();
				for (int i = 0; i < SIZE; i++) {
					working.put(data);
				}
			}
			endTime = System.nanoTime();
			System.out.println((endTime - startTime)
					+ "ns via single byte array to direct");

			// Performance writing via byte array to direct buffer
			startTime = System.nanoTime();
			for (int r = 0; r < REPEAT; r++) {
				ByteBuffer working = directBuffer.duplicate();
				for (int i = 0; i < SIZE; i++) {
					array[i] = 1;
				}
				working.put(array);
			}
			endTime = System.nanoTime();
			System.out
					.println((endTime - startTime) + "ns via array to direct");

			// Performance writing bytes to heap buffer
			startTime = System.nanoTime();
			for (int r = 0; r < REPEAT; r++) {
				ByteBuffer working = heapBuffer.duplicate();
				for (int i = 0; i < SIZE; i++) {
					working.put((byte) 1);
				}
			}
			endTime = System.nanoTime();
			System.out.println((endTime - startTime) + "ns by byte to heap");

			// Performance writing single byte array to heap buffer
			startTime = System.nanoTime();
			for (int r = 0; r < REPEAT; r++) {
				ByteBuffer working = heapBuffer.duplicate();
				for (int i = 0; i < SIZE; i++) {
					working.put(data);
				}
			}
			endTime = System.nanoTime();
			System.out.println((endTime - startTime)
					+ "ns via single byte array to heap");

			// Performance writing via byte array to heap buffer
			startTime = System.nanoTime();
			for (int r = 0; r < REPEAT; r++) {
				ByteBuffer working = heapBuffer.duplicate();
				for (int i = 0; i < SIZE; i++) {
					array[i] = 1;
				}
				working.put(array);
			}
			endTime = System.nanoTime();
			System.out.println((endTime - startTime) + "ns via array to heap");

			// Performance writing to byte array
			startTime = System.nanoTime();
			for (int r = 0; r < REPEAT; r++) {
				heapBuffer.duplicate(); // duplicate to keep similar
				for (int i = 0; i < SIZE; i++) {
					array[i] = 1;
				}
			}
			endTime = System.nanoTime();
			System.out.println((endTime - startTime) + "ns to array");

			// Performance writing via array copy
			byte[] sourceData = new byte[SIZE];
			startTime = System.nanoTime();
			for (int r = 0; r < REPEAT; r++) {
				heapBuffer.duplicate(); // duplicate to keep similar
				System.arraycopy(sourceData, 0, array, 0, SIZE);
			}
			endTime = System.nanoTime();
			System.out.println((endTime - startTime) + "ns via array copy");

		}
	}

	/**
	 * Do the requests.
	 * 
	 * @param port
	 *            Port.
	 * @param profiler
	 *            {@link OptimiseProfile}.
	 */
	private void doRequests(int port, OptimiseProfile profiler)
			throws Exception {

		// Run requests against WoOF
		Runner runner = new Runner("localhost", port, 0.1, 0.5, 0.9, 0.95, 0.99);
		runner.addLoad("cpu", false, new Request("/test.php?v=N", "n", 3))
				.addConnections(1);

		// Warm up
		profiler.setProfile(false);
		runner.runInterval("WARM UP", 30, null);

		// Profile
		profiler.setProfile(true);
		runner.runInterval("profiling", 10, profiler);
	}

	/**
	 * {@link Profiler} to provide execution times to aid optimisation.
	 */
	private static class OptimiseProfile implements Profiler, RunListener,
			Runnable {

		/**
		 * {@link ProfiledEntry} instances.
		 */
		private List<ProfiledEntry> entries = new ArrayList<ProfiledEntry>();

		/**
		 * Indicates whether to profile.
		 */
		private boolean isProfile = true;

		/**
		 * Previous time stamp.
		 */
		private long previousTimestamp = -1;

		/**
		 * Total service time.
		 */
		private long totalServiceTime = 0;

		/**
		 * Number of responses.
		 */
		private int numberOfResponses = 0;

		/**
		 * Flags whether to profile.
		 * 
		 * @param isProfile
		 *            <code>true</code> to profile.
		 */
		public void setProfile(boolean isProfile) {
			synchronized (this.entries) {
				this.isProfile = isProfile;
			}
		}

		/**
		 * Logs the time stamp.
		 * 
		 * @param timestamp
		 *            Time stamp.
		 * @param description
		 *            Description.
		 */
		private void log(long timestamp, String description) {
			this.entries.add(new ProfiledEntry(timestamp, description));
		}

		/*
		 * =================== Profiler ==============================
		 */

		@Override
		public void profileProcess(ProfiledProcess process) {

			// Determine completion time
			long processCompletionTimestamp = System.nanoTime();

			synchronized (this.entries) {

				// Determine if profiling
				if (!this.isProfile) {
					return; // not profiling
				}

				// Create message
				this.log(process.getStartTimestamp(), "PROCESS START");
				for (ProfiledThread thread : process.getProfiledThreads()) {
					this.log(thread.getStartTimestamp(), "THREAD");
					for (ProfiledJob job : thread.getProfiledJobs()) {
						String jobName = job.getJobName();

						// Ignore socket jobs
						if ((jobName
								.startsWith("net.officefloor.plugin.socket.server.http.ServerHttpConnection.accepter.accepter"))
								|| (jobName
										.startsWith("net.officefloor.plugin.socket.server.http.ServerHttpConnection.listener"))) {
							return;
						}

						// Include the job
						this.log(job.getStartTimestamp(),
								jobName + " [" + job.getExecutingThreadName()
										+ "]");
					}
				}
				this.log(processCompletionTimestamp, "PROCESS END");
			}
		}

		/*
		 * ============================= Runnable ==========================
		 */

		@Override
		public void run() {
			synchronized (this.entries) {

				// Determine if profiling
				if (!this.isProfile) {
					return; // not profiling
				}

				// Log servicing request
				this.log(System.nanoTime(), "servicing request");
			}
		}

		/*
		 * ============================= RunListener ==========================
		 */

		@Override
		public void requestSent() {
			synchronized (this.entries) {

				// Determine if profiling
				if (!this.isProfile) {
					return; // not profiling
				}

				// Log the request being sent
				this.log(System.nanoTime(), "REQUEST SENT");

				// Indicate time for recording
				this.log(System.nanoTime(), "[nano time capture]");
			}
		}

		@Override
		public void responseReceived() {

			synchronized (this.entries) {

				// Determine if profiling
				if (!this.isProfile) {
					return; // not profiling
				}

				// Provide response received entry
				this.log(System.nanoTime(), "RECEIVED RESPONSE");

				// Sort the entries
				Collections.sort(this.entries, new Comparator<ProfiledEntry>() {
					@Override
					public int compare(ProfiledEntry a, ProfiledEntry b) {
						return (int) (a.timestamp - b.timestamp);
					}
				});

				// Log the process
				System.out.println();
				boolean isFirst = true;
				for (ProfiledEntry entry : this.entries) {

					// Determine time difference
					long difference = entry.timestamp - this.previousTimestamp;
					this.previousTimestamp = entry.timestamp; // setup for next

					// Log entry
					if (isFirst) {
						System.out.println("0 - " + entry.description);
						isFirst = false;
					} else {
						System.out.println(difference + " - "
								+ entry.description);
					}
				}

				// Obtain the service time
				long startTime = this.entries.get(0).timestamp;
				long endTime = this.entries.get(this.entries.size() - 1).timestamp;
				long serviceTime = (endTime - startTime);

				// Keep track of average
				this.totalServiceTime += serviceTime;
				this.numberOfResponses++;

				// Indicate total
				System.out.println("TOTAL: " + serviceTime + "   (AVG "
						+ (this.totalServiceTime / this.numberOfResponses)
						+ ")");

				// Clear the entries
				this.entries.clear();
			}
		}
	}

	/**
	 * Profiled entry.
	 */
	private static class ProfiledEntry {

		/**
		 * Time stamp.
		 */
		public long timestamp;

		/**
		 * Description.
		 */
		public String description;

		/**
		 * Initiate.
		 * 
		 * @param timestamp
		 *            Time stamp.
		 * @param description
		 *            Description.
		 */
		public ProfiledEntry(long timestamp, String description) {
			this.timestamp = timestamp;
			this.description = description;
		}
	}

}