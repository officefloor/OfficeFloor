package net.officefloor.model.test.variable;

import net.officefloor.plugin.variable.Var;

/**
 * Mock {@link Var}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockVar<T> implements Var<T> {

	/**
	 * Value.
	 */
	private volatile T value = null;

	/**
	 * Default constructor.
	 */
	public MockVar() {
	}

	/**
	 * Instantiate.
	 * 
	 * @param value Initial value.
	 */
	public MockVar(T value) {
		this.value = value;
	}

	/*
	 * ================== Var ========================
	 */

	@Override
	public void set(T value) {
		this.value = value;
	}

	@Override
	public T get() {
		return this.value;
	}

}