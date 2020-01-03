package net.officefloor.frame.test;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Captures a free variable for closure state of a {@link Function}.
 *
 * @author Daniel Sagenschneider
 */
public class Closure<T> implements Consumer<T> {

	/**
	 * {@link Closure} free variable value.
	 */
	public T value;

	/**
	 * Initialise with <code>null</code>.
	 */
	public Closure() {
		this(null);
	}

	/**
	 * Initialise with initial value.
	 * 
	 * @param initialValue Initial value.
	 */
	public Closure(T initialValue) {
		this.value = initialValue;
	}

	/*
	 * ===================== Consumer ========================
	 */

	@Override
	public void accept(T value) {
		this.value = value;
	}

}