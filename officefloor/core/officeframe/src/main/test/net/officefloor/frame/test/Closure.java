/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
