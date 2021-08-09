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
 * {@link Function} interface to load the value.
 *
 * @author Daniel Sagenschneider
 */
public interface ValueLoader<M, V> {

	/**
	 * Loads the value to the model.
	 * 
	 * @param model
	 *            Model.
	 * @param value
	 *            Value.
	 */
	void loadValue(M model, V value);
}
