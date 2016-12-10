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

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.util.Timer;
import java.util.TimerTask;

import junit.framework.TestCase;
import net.officefloor.tutorials.performance.ApacheServicer;
import net.officefloor.tutorials.performance.GrizzlyServicer;
import net.officefloor.tutorials.performance.JettyServicer;
import net.officefloor.tutorials.performance.NginxServicer;
import net.officefloor.tutorials.performance.Servicer;
import net.officefloor.tutorials.performance.TomcatServicer;
import net.officefloor.tutorials.performance.WoofServicer;
import net.officefloor.tutorials.performance.nio.Load;
import net.officefloor.tutorials.performance.nio.Request;
import net.officefloor.tutorials.performance.nio.Runner;

/**
 * Provides individual load testing.
 * 
 * @author Daniel Sagenschneider
 */
public class IndividualLoadTestCase extends TestCase {

	/**
	 * Tests WoOF.
	 */
	public void testWoOF() throws Exception {
		this.doPerformanceTest(new WoofServicer());
	}

	/**
	 * Tests Jetty.
	 */
	public void testJetty() throws Exception {
		this.doPerformanceTest(new JettyServicer());
	}

	/**
	 * Tests Grizzly.
	 */
	public void testGrizzly() throws Exception {
		this.doPerformanceTest(new GrizzlyServicer());
	}

	/**
	 * Tests Tomcat.
	 */
	public void testTomcat() throws Exception {
		this.doPerformanceTest(new TomcatServicer());
	}

	/**
	 * Tests Apache.
	 */
	public void testApache() throws Exception {
		this.doPerformanceTest(new ApacheServicer());
	}

	/**
	 * Tests Nginx.
	 */
	public void testNginx() throws Exception {
		this.doPerformanceTest(new NginxServicer());
	}

	/**
	 * Tests the performance.
	 */
	public void doPerformanceTest(Servicer servicer) throws Exception {

		// Provide details
		int timeIntervalSeconds = 20;
		int cpuConnectionCount = 10;
		int dbConnectionCount = 10;
		
		// Disconnect after so many requests to avoid throttling
		int requestsRepeatedInSequence = 99;
		boolean isDisconnectAfterSequence = true;

		// Indicate starting
		System.out
				.println("===============================================================");
		System.out.println("Starting " + this.getName() + " with interval of "
				+ timeIntervalSeconds + " secs");
		System.out
				.println("===============================================================");

		// Obtain the host (and start servicer if local host)
		String host = System.getProperty(
				AbstractNioRunnerTestCase.PROPERTY_TARGET_HOST, "localhost");
		if ("localhost".equals(host)) {
			// Start the servicer
			System.out.print("Starting servicer ...");
			System.out.flush();
			servicer.start();
			System.out.println(" started");
		}

		// Monitor memory foot print
		final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
		final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				int bytesInMegaByte = 1024 * 1024;
				System.out
						.println("Memory: heap="
								+ (memoryBean.getHeapMemoryUsage().getUsed() / bytesInMegaByte)
								+ " non="
								+ (memoryBean.getNonHeapMemoryUsage().getUsed() / bytesInMegaByte)
								+ ", Threads: " + threadBean.getThreadCount()
								+ " peak " + threadBean.getPeakThreadCount());
			}
		}, 1000, 20 * 1000);

		// Start the runner
		Runner runner = new Runner(host, servicer.getPort(), 0.1, 0.5, 0.9,
				0.95, 0.99, 1);
		Load cpuLoad = runner.addLoad("cpu", isDisconnectAfterSequence,
				new Request("/test.php?v=N", "n", requestsRepeatedInSequence));
		Load dbLoad = runner.addLoad("db", isDisconnectAfterSequence,
				new Request("/test.php?v=D", "d", requestsRepeatedInSequence));

		try {

			// Establish connections
			cpuLoad.addConnections(cpuConnectionCount);
			dbLoad.addConnections(dbConnectionCount);

			// Warm up
			runner.runInterval("WARM UP", timeIntervalSeconds, null);

			// Undertake run
			runner.runInterval("RUN", timeIntervalSeconds, null);

		} finally {
			try {
				runner.stop();
			} finally {
				servicer.stop();
				timer.cancel();
			}
		}
	}

}