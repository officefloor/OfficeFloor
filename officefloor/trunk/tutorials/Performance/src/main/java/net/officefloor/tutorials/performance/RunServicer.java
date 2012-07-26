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
package net.officefloor.tutorials.performance;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Runs a {@link Servicer}.
 * 
 * @author Daniel Sagenschneider
 */
public class RunServicer {

	/**
	 * Runs a particular {@link Servicer}.
	 * 
	 * @param args
	 *            Prefix of the {@link Servicer} to run.
	 * @throws Exception
	 *             If fails to run.
	 */
	public static void main(String[] args) throws Exception {

		// Obtain the prefix of the servicer
		if (args.length != 1) {
			System.err.println("USAGE: run.sh <servicer prefix>");
			System.exit(1);
		}
		String servicerPrefix = args[0].trim();

		// Load the servicer
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
		String servicerClassName = Servicer.class.getPackage().getName() + "."
				+ servicerPrefix + Servicer.class.getSimpleName();
		Object instance = classLoader.loadClass(servicerClassName)
				.newInstance();
		final Servicer servicer = (Servicer) instance;

		// Start the servicer
		System.out.print("Starting servicer ...");
		System.out.flush();
		servicer.start();
		System.out.println(" started");

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

		// Add shutdown hook (to attempt to stop servicer gracefully)
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					System.out.print("Shutting down servicer ...");
					System.out.flush();
					servicer.stop();
					System.out.println(" shutdown");
				} catch (Exception ex) {
					System.err.println("Failure shutting down servicer");
					ex.printStackTrace();
				}
			}
		}));

		// Sleep until told to stop
		for (;;) {
			Thread.sleep(1000000);
		}
	}

}