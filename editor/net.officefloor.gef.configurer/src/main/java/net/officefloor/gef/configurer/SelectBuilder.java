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
 * Builder for selecting from a list.
 * 
 * @author Daniel Sagenschneider
 */
public interface SelectBuilder<M, I> extends Builder<M, I, SelectBuilder<M, I>> {

	/**
	 * <p>
	 * Configure obtaining label from item.
	 * <p>
	 * If not configured, will use {@link Object#toString()} of the item.
	 * 
	 * @param getLabel Function to obtain label from item.
	 * @return <code>this</code>.
	 */
	SelectBuilder<M, I> itemLabel(Function<I, String> getLabel);

}
