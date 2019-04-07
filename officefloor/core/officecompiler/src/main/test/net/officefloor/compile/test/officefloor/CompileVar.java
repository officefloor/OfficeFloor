/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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