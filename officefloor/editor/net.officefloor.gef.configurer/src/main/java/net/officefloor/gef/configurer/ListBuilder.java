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

import java.util.List;
import java.util.function.Supplier;

/**
 * Builder of a list of models.
 * 
 * @author Daniel Sagenschneider
 */
public interface ListBuilder<M, I> extends ItemBuilder<I>, Builder<M, List<I>, ListBuilder<M, I>> {

	/**
	 * Provides means to add an item to the list.
	 * 
	 * @param itemFactory
	 *            {@link Supplier} for the new item.
	 * @return <code>this</code>.
	 */
	ListBuilder<M, I> addItem(Supplier<I> itemFactory);

	/**
	 * Allows for deleting an item from the list.
	 * 
	 * @return <code>this</code>.
	 */
	ListBuilder<M, I> deleteItem();

}
