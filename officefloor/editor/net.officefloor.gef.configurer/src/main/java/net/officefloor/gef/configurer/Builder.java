/*-
 * #%L
 * [bundle] OfficeFloor Configurer
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
