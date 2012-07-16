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

/**
 * Particular instance of a {@link Request}.
 * 
 * @author Daniel Sagenschneider
 */
public class RequestInstance {

	/**
	 * {@link Request}.
	 */
	private final Request request;

	/**
	 * {@link Listener}.
	 */
	private final Listener listener;

	/**
	 * Start time microseconds.
	 */
	private volatile long startTime;

	/**
	 * End time microseconds.
	 */
	private volatile long endTime = -1;

	/**
	 * Initiate.
	 * 
	 * @param request
	 *            {@link Request}.
	 * @param listener
	 *            {@link Listener}.
	 */
	public RequestInstance(Request request, Listener listener) {
		this.request = request;
		this.listener = listener;
	}

	/**
	 * Obtains the {@link Request}.
	 * 
	 * @return {@link Request}.
	 */
	public Request getRequest() {
		return this.request;
	}

	/**
	 * Flags this {@link RequestInstance} is completed.
	 * 
	 * @param startTime
	 *            Start time microseconds.
	 * @param endTime
	 *            End time microseconds.
	 */
	public void complete(long startTime, long endTime) {

		// Store request details
		this.startTime = startTime;
		this.endTime = endTime;

		// Trigger possible listener
		if (this.listener != null) {
			this.listener.trigger();
		}
	}

	/**
	 * Indicates if complete.
	 * 
	 * @return <code>true</code> if complete.
	 */
	public boolean isComplete() {
		return (this.endTime > 0);
	}

	/**
	 * Obtains the start time.
	 * 
	 * @return Start time in microseonds.
	 */
	public long getStartTime() {
		return this.startTime;
	}

	/**
	 * Obtains the end time.
	 * 
	 * @return End time in microseconds.
	 */
	public long getEndTime() {
		return this.endTime;
	}

}