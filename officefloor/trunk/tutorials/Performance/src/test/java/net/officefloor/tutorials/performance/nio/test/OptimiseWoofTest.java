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
import net.officefloor.frame.api.profile.ProfiledJob;
import net.officefloor.frame.api.profile.ProfiledProcess;
import net.officefloor.frame.api.profile.ProfiledThread;
import net.officefloor.frame.api.profile.Profiler;
import net.officefloor.plugin.woof.WoofOfficeFloorSource;
import net.officefloor.tutorials.performance.nio.Request;
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
	public void testOptimise() throws Exception {

		// Start WoOF
		WoofOfficeFloorSource source = new WoofOfficeFloorSource();
		source.setProfiler(new Profiler() {
			@Override
			public void profileProcess(ProfiledProcess process) {

				// Create message
				StringBuilder log = new StringBuilder();
				log.append("PROCESS\n");
				for (ProfiledThread thread : process.getProfiledThreads()) {
					log.append("  THREAD\n");
					for (ProfiledJob job : thread.getProfiledJobs()) {
						String jobName = job.getJobName();

						// Ignore socket jobs
						if ("net.officefloor.plugin.socket.server.http.ServerHttpConnection.accepter.accepter"
								.equals(jobName)) {
							return;
						}

						// Include the job
						log.append("    ");
						log.append(jobName);
						log.append(" - ");
						log.append(job.getStartTimestamp());
						log.append("  [");
						log.append(job.getExecutingThreadName());
						log.append("]\n");
					}
				}

				// Log message
				System.out.println(log.toString());
			}
		});
		WoofOfficeFloorSource.run(source);

		try {
			// Run requests against WoOF
			Runner runner = new Runner("localhost", 7878, 0.1, 0.5, 0.9, 0.95,
					0.99);
			runner.addLoad("cpu", true, new Request("/test.php?v=N", "n", 3))
					.addConnections(1);
			runner.runInterval("profiling", 10);

		} finally {
			WoofOfficeFloorSource.stop();
		}
	}

}