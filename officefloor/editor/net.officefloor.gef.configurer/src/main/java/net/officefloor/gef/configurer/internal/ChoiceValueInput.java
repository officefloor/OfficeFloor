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

package net.officefloor.gef.configurer.internal;

import java.util.function.Supplier;

import javafx.beans.property.ReadOnlyProperty;

/**
 * {@link ValueInput} allowing for rendering choice of following
 * {@link ValueInput} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface ChoiceValueInput<M> extends ValueInput {

	/**
	 * Obtains the array of {@link ValueRendererFactory} instances.
	 * 
	 * @return Array of {@link ValueRendererFactory} instances.
	 */
	Supplier<ValueRendererFactory<M, ? extends ValueInput>[]>[] getChoiceValueRendererFactories();

	/**
	 * Obtains the index into choice {@link ValueRenderer} listing to display.
	 * 
	 * @return Index into choice {@link ValueRenderer} listing to display.
	 */
	ReadOnlyProperty<Integer> getChoiceIndex();

}
