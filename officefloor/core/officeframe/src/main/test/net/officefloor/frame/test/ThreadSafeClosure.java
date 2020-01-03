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