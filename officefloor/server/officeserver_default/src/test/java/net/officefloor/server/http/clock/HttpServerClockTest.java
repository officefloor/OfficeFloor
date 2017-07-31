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
package net.officefloor.server.http.clock;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.clock.HttpServerClock;
import net.officefloor.server.http.clock.HttpServerClockImpl;
import net.officefloor.server.http.clock.HttpServerClockSource;

/**
 * Tests the {@link HttpServerClock}.
 *
 * @author Daniel Sagenschneider
 */
public class HttpServerClockTest extends OfficeFrameTestCase {

	/**
	 * {@link HttpServerClock} being tested.
	 */
	private final HttpServerClock clock = new HttpServerClockImpl();

	/**
	 * Ensure able to source the {@link HttpServerClock}.
	 */
	public void testSouceHttpServerClock() throws Exception {

		// Obtain the server clock
		HttpServerClockSource source = (HttpServerClockSource) Class.forName(
				HttpServerClockImpl.class.getName()).newInstance();

		// Does not require meta-data
		HttpServerClock clock = source.createHttpServerClock(null);
		assertNotNull("Ensure have clock", clock);
	}

	/**
	 * Ensure able to obtain the time.
	 */
	public void testDate() throws Exception {

		// Obtain the date value
		String date = this.clock.getDateHeaderValue();

		// Validate the date is correct format
		assertDate(date);
	}

	/**
	 * Ensure able to repeat obtaining the date.
	 */
	public void testRepeatDate() throws Exception {
		for (int i = 0; i < 100; i++) {
			String date = this.clock.getDateHeaderValue();
			assertDate(date);
		}
	}

	/**
	 * Ensure can use in multi-threaded context.
	 */
	public void testMultiThreaded() throws Throwable {

		final int threadCount = 100;
		final boolean[] complete = new boolean[threadCount];
		for (int i = 0; i < threadCount; i++) {
			complete[i] = false;
		}
		final Throwable[] failures = new Throwable[threadCount];

		// Start the threads
		for (int i = 0; i < threadCount; i++) {
			final int index = i;
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {

						// Obtain date multiple times
						for (int i = 0; i < 100; i++) {
							String date = HttpServerClockTest.this.clock
									.getDateHeaderValue();
							assertDate(date);
						}

					} catch (Throwable ex) {
						// Provide failure
						synchronized (failures) {
							failures[index] = ex;
						}

					} finally {
						// Ensure complete
						synchronized (complete) {
							complete[index] = true;
							complete.notify();
						}
					}
				}
			}).start();
		}

		// Wait until test complete
		boolean isComplete = false;
		while (!isComplete) {
			synchronized (complete) {

				// Determine if complete
				isComplete = true;
				for (boolean completed : complete) {
					if (!completed) {
						isComplete = false;
					}
				}

				// Not complete so wait
				if (!isComplete) {
					complete.wait(1000);
				}
			}
		}

		// Throw any failures
		for (Throwable failure : failures) {
			if (failure != null) {
				throw failure;
			}
		}
	}

	/**
	 * Ensures the date is accurate.
	 * 
	 * @param dateHeaderValue
	 *            Header date value.
	 */
	private static void assertDate(String dateHeaderValue)
			throws ParseException {

		// Parse the date
		SimpleDateFormat formatter = new SimpleDateFormat(
				"EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
		Date date = formatter.parse(dateHeaderValue);

		// Obtain the current time
		long currentTime = System.currentTimeMillis();

		// Ensure the date is correct (give or take 2 seconds)
		assertTrue("Ensure correct date",
				Math.abs(currentTime - date.getTime()) < 2000);
	}

}