/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.frame.test;

import java.util.function.Function;

import org.junit.Assert;

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
	 * Possible failure.
	 */
	private Throwable failure;

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
	 * @param initialValue Initial value.
	 */
	public ThreadSafeClosure(T initialValue) {
		this.value = initialValue;
	}

	/**
	 * Sets the value.
	 * 
	 * @param value Value.
	 */
	public synchronized void set(T value) {

		// Set value
		this.value = value;
		this.isValueProvided = true;

		// Notify value available
		this.notifyAll();
	}

	/**
	 * Flags a failure.
	 * 
	 * @param cause Cause of the failure.
	 */
	public synchronized void failure(Throwable cause) {

		// Set failure
		this.failure = cause;
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
	 */
	public synchronized T waitAndGet() {
		return this.waitAndGet(3000);
	}

	/**
	 * Obtains the value.
	 * 
	 * @param timeout Timeout to wait for the value.
	 * @return Value.
	 */
	public synchronized T waitAndGet(long timeout) {

		// Capture time for time out
		long startTime = System.currentTimeMillis();
		while (!this.isValueProvided) {

			// Determine if timeout
			if ((System.currentTimeMillis() - startTime) > timeout) {
				Assert.fail("Timed out waiting on closure value");
			}

			// Wait some time for value
			try {
				this.wait(10);
			} catch (InterruptedException ex) {
				OfficeFrameTestCase.fail(ex);
			}
		}

		// Determine if failure
		if (this.failure != null) {
			OfficeFrameTestCase.fail(this.failure);
		}

		// As here have value
		return this.value;
	}

}
