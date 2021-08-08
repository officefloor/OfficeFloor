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
