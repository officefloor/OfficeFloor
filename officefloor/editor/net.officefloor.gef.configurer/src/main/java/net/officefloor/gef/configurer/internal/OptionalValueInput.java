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

import java.util.function.Consumer;

/**
 * {@link ValueInput} allowing for optionally rendering following
 * {@link ValueInput} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface OptionalValueInput<M> extends ValueInput {

	/**
	 * Specifies the optional loader.
	 * 
	 * @param loader Loads the optional content.
	 */
	void setOptionalLoader(Consumer<ValueRendererFactory<M, ? extends ValueInput>[]> loader);

}
