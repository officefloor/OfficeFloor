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

import net.officefloor.tutorials.performance.Client.RequestIteration;

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
	 * {@link RequestIteration}.
	 */
	private final RequestIteration iteration;

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
	 * Indicates if {@link RequestInstance} skipped by {@link Client}.
	 */
	private volatile boolean isSkipped = false;

	/**
	 * Failure.
	 */
	private volatile Throwable failure = null;

	/**
	 * Initiate.
	 * 
	 * @param request
	 *            {@link Request}.
	 * @param iteration
	 *            {@link RequestIteration} that this {@link RequestInstance} is
	 *            part of.
	 * @param listener
	 *            {@link Listener}.
	 */
	public RequestInstance(Request request, RequestIteration iteration,
			Listener listener) {
		this.request = request;
		this.iteration = iteration;
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
	 * Obtains the {@link RequestIteration}.
	 * 
	 * @return {@link RequestIteration}.
	 */
	public RequestIteration getIteration() {
		return this.iteration;
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

		// Trigger listener
		this.triggerListener();
	}

	/**
	 * Flags failure of the {@link RequestInstance}.
	 * 
	 * @param failure
	 *            Failure.
	 */
	public void failed(Throwable failure) {

		// Store failure
		this.failure = failure;
		this.endTime = 1; // indicate complete

		// Trigger listener
		this.triggerListener();
	}

	/**
	 * Flags skipping the {@link RequestInstance}.
	 */
	public void skip() {

		// Indicate skipped
		this.isSkipped = true;
		this.endTime = 1; // indicate complete

		// Trigger listener
		this.triggerListener();
	}

	/**
	 * Triggers the possible listener.
	 */
	private void triggerListener() {
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
	 * Indicates if {@link Client} skipped {@link RequestInstance}.
	 * 
	 * @return <code>true</code> if skipped.
	 */
	public boolean isSkipped() {
		return this.isSkipped;
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

	/**
	 * Obtains the possible failure.
	 * 
	 * @return Failure. Most likely <code>null</code>.
	 */
	public Throwable getFailure() {
		return this.failure;
	}

}