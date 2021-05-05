/*-
 * #%L
 * [bundle] OfficeFloor Configurer
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

package net.officefloor.gef.configurer;

import java.util.function.Function;

/**
 * Generic builder.
 * 
 * @author Daniel Sagenschneider
 */
public interface Builder<M, V, B extends Builder<M, V, B>> {

	/**
	 * Configures obtaining the initial value.
	 * 
	 * @param getInitialValue
	 *            Obtains the initial value.
	 * @return <code>this</code>.
	 */
	B init(Function<M, V> getInitialValue);

	/**
	 * Validates the text value.
	 * 
	 * @param validator
	 *            {@link ValueValidator}.
	 * @return <code>this</code>.
	 */
	B validate(ValueValidator<M, V> validator);

	/**
	 * Specifies the {@link ValueLoader} to load the value to the model.
	 * 
	 * @param valueLoader
	 *            {@link ValueLoader} to load the value to the model.
	 * @return <code>this</code>.
	 */
	B setValue(ValueLoader<M, V> valueLoader);

}
