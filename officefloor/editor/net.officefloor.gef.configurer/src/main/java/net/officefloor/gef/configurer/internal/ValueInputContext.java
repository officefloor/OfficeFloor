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

import javafx.beans.property.Property;
import net.officefloor.gef.configurer.Actioner;
import net.officefloor.gef.configurer.Builder;
import net.officefloor.gef.configurer.ErrorListener;
import net.officefloor.gef.configurer.ValueValidator;

/**
 * Context for the {@link ValueInput}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ValueInputContext<M, V> {

	/**
	 * Obtains the model.
	 * 
	 * @return Model.
	 */
	M getModel();

	/**
	 * Obtains the value.
	 * 
	 * @return Value.
	 */
	Property<V> getInputValue();

	/**
	 * Adds a {@link ValueValidator}.
	 * 
	 * @param validator
	 *            {@link ValueValidator}.
	 */
	void addValidator(ValueValidator<M, V> validator);

	/**
	 * Triggers reload of the value from the model for the {@link Builder}.
	 * 
	 * @param builder
	 *            {@link Builder} to identify the value from the model to reload.
	 */
	void reload(Builder<?, ?, ?> builder);

	/**
	 * Refreshes the error.
	 */
	void refreshError();

	/**
	 * Obtains the {@link Actioner}.
	 * 
	 * @return {@link Actioner}.
	 */
	Actioner getOptionalActioner();

	/**
	 * Obtains the dirty {@link Property}.
	 * 
	 * @return Dirty {@link Property}.
	 */
	Property<Boolean> dirtyProperty();

	/**
	 * Obtains the valid {@link Property}.
	 * 
	 * @return Valid {@link Property}.
	 */
	Property<Boolean> validProperty();

	/**
	 * Obtains the {@link ErrorListener}.
	 * 
	 * @return {@link ErrorListener}.
	 */
	ErrorListener getErrorListener();

}
