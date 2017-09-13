/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.frame.test;

import java.util.function.Function;

import org.junit.Assert;

import junit.framework.AssertionFailedError;

/**
 * {@link Thread} safe capture of a free variable for closure state of a
 * {@link Function}.
 *
 * @author Daniel Sagenschneider
 */
public class ThreadSafeClosure<T> {

	/**
	 * {@link Closure} free variable value.
	 */
	private T value;

	/**
	 * Indicates if value provided. Allows for <code>null</code> value to be
	 * provided.
	 */
	private boolean isValueProvided = false;

	/**
	 * Initialise with <code>null</code>.
	 */
	public ThreadSafeClosure() {
		this(null);
	}

	/**
	 * Initialise with initial value.
	 * 
	 * @param initialValue
	 *            Initial value.
	 */
	public ThreadSafeClosure(T initialValue) {
		this.value = initialValue;
	}

	/**
	 * Sets the value.
	 * 
	 * @param value
	 *            Value.
	 */
	public synchronized void set(T value) {

		// Set value
		this.value = value;
		this.isValueProvided = true;

		// Notify value available
		this.notifyAll();
	}

	/**
	 * Obtains current value.
	 * 
	 * @return Value or <code>null</code>.
	 */
	public synchronized T get() {
		return this.value;
	}

	/**
	 * Convenience method to wait 3 seconds and then get value.
	 * 
	 * @return Value.
	 * @throws AssertionFailedError
	 *             If times out waiting for the value.
	 */
	public synchronized T waitAndGet() throws InterruptedException {
		return this.waitAndGet(3000);
	}

	/**
	 * Obtains the value.
	 * 
	 * @param timeout
	 *            Timeout to wait for the value.
	 * @return Value.
	 * @throws AssertionFailedError
	 *             If times out waiting for the value.
	 * @throws InterruptedException
	 *             If interrupted in waiting for value.
	 */
	public synchronized T waitAndGet(long timeout) throws InterruptedException {

		// Capture time for time out
		long startTime = System.currentTimeMillis();
		while (!this.isValueProvided) {

			// Determine if timeout
			if ((System.currentTimeMillis() - startTime) > timeout) {
				Assert.fail("Timed out waiting on closure value");
			}

			// Wait some time for value
			this.wait(10);
		}

		// As here have value
		return this.value;
	}

}