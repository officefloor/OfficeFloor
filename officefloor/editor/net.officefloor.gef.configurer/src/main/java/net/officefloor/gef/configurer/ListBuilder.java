/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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