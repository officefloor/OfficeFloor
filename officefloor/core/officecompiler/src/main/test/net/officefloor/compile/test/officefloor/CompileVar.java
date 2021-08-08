/*-
 * #%L
 * OfficeCompiler
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
