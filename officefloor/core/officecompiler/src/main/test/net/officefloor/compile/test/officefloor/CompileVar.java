package net.officefloor.compile.test.officefloor;

import static org.junit.Assert.fail;

import java.util.function.Consumer;

import net.officefloor.plugin.variable.Var;

/**
 * Closure to obtain {@link Var} value.
 * 
 * @author Daniel Sagenschneider
 */
public class CompileVar<T> implements Consumer<Var<T>> {

	/**
	 * Initial value.
	 */
	private final T initialValue;

	/**
	 * {@link Var}.
	 */
	private volatile Var<T> var;

	/**
	 * No initial value.
	 */
	public CompileVar() {
		this.initialValue = null;
	}

	/**
	 * Instantiate with initial value for {@link Var}.
	 * 
	 * @param initialValue Initial value.
	 */
	public CompileVar(T initialValue) {
		this.initialValue = initialValue;
	}

	/**
	 * Obtains the value of the {@link Var}.
	 * 
	 * @return Value of the {@link Var}.
	 */
	public T getValue() {
		if (this.var == null) {
			fail("Variable was not initialised");
		}
		return this.var.get();
	}

	/*
	 * ================== Consumer ======================
	 */

	@Override
	public void accept(Var<T> var) {
		this.var = var;

		// Load possible initial value
		if (this.initialValue != null) {
			var.set(this.initialValue);
		}
	}

}