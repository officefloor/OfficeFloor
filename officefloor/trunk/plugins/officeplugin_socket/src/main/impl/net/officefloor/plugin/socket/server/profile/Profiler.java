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
package net.officefloor.plugin.socket.server.profile;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.socket.server.http.HttpRequest;

/**
 * Allows profiling the performance of servicing a {@link HttpRequest}.
 * 
 * @author Daniel Sagenschneider
 */
public class Profiler {

	/**
	 * Indicates whether profiling.
	 */
	private static boolean isProfile;

	static {
		isProfile = Boolean.valueOf(System.getProperty("profile.officefloor",
				String.valueOf(Boolean.FALSE)));
	}

	/**
	 * Listing of {@link Marker} instances.
	 */
	private static final List<Marker> markers = new ArrayList<Marker>();

	/**
	 * Marks the particular point in servicing a {@link HttpRequest}.
	 * 
	 * @param description
	 *            Description of the particular point in servicing the
	 *            {@link HttpRequest}.
	 */
	public static void mark(String description) {
		if (!isProfile) {
			return;
		}

		// Obtain the thread name
		String threadName = Thread.currentThread().getName();

		synchronized (markers) {
			// Mark this point
			markers.add(new Marker(System.nanoTime(), description, threadName));
		}
	}

	/**
	 * Reports on profile of the {@link HttpRequest}.
	 * 
	 * @param description
	 *            Description of this point.
	 */
	public static void report(String description) {
		if (!isProfile) {
			return;
		}

		synchronized (markers) {
			// Add marker for the report
			mark(description);

			// Indicate profile
			System.out.println("PROFILE");

			// Provide report
			long previousTimestamp = -1;
			for (Marker marker : markers) {

				// Determine time to marker
				long timeToMarker = (previousTimestamp < 0 ? 0
						: (marker.timestamp - previousTimestamp));

				// Provide reporting
				System.out.println("\t" + (timeToMarker / 1000) + "us - "
						+ marker.description + " [" + marker.threadName + "]");

				// Setup for next iteration
				previousTimestamp = marker.timestamp;
			}

			// Clear markers for next report
			markers.clear();
		}
	}

	/**
	 * All access via static methods.
	 */
	private Profiler() {
	}

	/**
	 * Marker.
	 */
	private static class Marker {

		/**
		 * Timestamp of this {@link Marker} in nanoseconds.
		 */
		public final long timestamp;

		/**
		 * Description of this {@link Marker}.
		 */
		public final String description;

		/**
		 * Name of {@link Thread} executing code for {@link Marker}.
		 */
		private final String threadName;

		/**
		 * Initiate.
		 * 
		 * @param timestamp
		 *            Timestamp of this {@link Marker} in nanoseconds.
		 * @param description
		 *            Description of this {@link Marker}.
		 * @param threadName
		 *            Name of {@link Thread} executing code for {@link Marker}.
		 */
		public Marker(long timestamp, String description, String threadName) {
			this.timestamp = timestamp;
			this.description = description;
			this.threadName = threadName;
		}
	}

}