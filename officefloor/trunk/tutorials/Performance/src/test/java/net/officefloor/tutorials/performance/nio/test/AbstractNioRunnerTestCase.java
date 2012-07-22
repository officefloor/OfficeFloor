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

import junit.framework.TestCase;
import net.officefloor.tutorials.performance.Servicer;
import net.officefloor.tutorials.performance.nio.Load;
import net.officefloor.tutorials.performance.nio.Runner;

/**
 * Tests the performance.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractNioRunnerTestCase extends TestCase {

	/**
	 * System property to obtain the target host.
	 */
	public static final String PROPERTY_TARGET_HOST = "target.host";

	/**
	 * Creates the {@link Servicer}.
	 * 
	 * @return {@link Servicer}.
	 */
	protected abstract Servicer createServicer();

	/**
	 * Tests the performance.
	 */
	public void testPerformance() throws Exception {

		// Provide details
		int timeIntervalSeconds = 20;
		int cpuSeed = 1;
		int dbSeed = 1;

		// Indicate starting
		System.out
				.println("===============================================================");
		System.out.println("Starting " + this.getClass().getSimpleName()
				+ " with interval of " + timeIntervalSeconds + " secs");
		System.out
				.println("===============================================================");

		// Create the servicer
		Servicer servicer = this.createServicer();

		// Obtain the host (and start servicer if local host)
		String host = System.getProperty(PROPERTY_TARGET_HOST, "localhost");
		if ("localhost".equals(host)) {
			// Start the servicer
			servicer.start();
		}

		// Start the runner
		Runner runner = new Runner(host, servicer.getPort(), 0.1, 0.5, 0.9,
				0.95, 0.99);
		Load cpuLoad = runner.addLoad("/info.php?v=N", "n");
		Load dbLoad = runner.addLoad("/info.php?v=Y", "y");

		try {

			// Establish initial connections
			cpuLoad.addConnections(cpuSeed);
			dbLoad.addConnections(dbSeed);

			// Warm up
			runner.runInterval("WARM UP", 300);

			// Undertake run
			runner.runInterval("RUN", timeIntervalSeconds);

			// Provide 3 levels of magnitude
			for (int i = 0; i < 3; i++) {

				// Increment the connections
				cpuLoad.addConnections((cpuLoad.getConnectionCount() * 10)
						- cpuLoad.getConnectionCount());
				dbLoad.addConnections((dbLoad.getConnectionCount() * 10)
						- dbLoad.getConnectionCount());
				
				// Undertake run
				runner.runInterval("RUN", timeIntervalSeconds);
			}

		} finally {
			servicer.stop();
		}
	}

}