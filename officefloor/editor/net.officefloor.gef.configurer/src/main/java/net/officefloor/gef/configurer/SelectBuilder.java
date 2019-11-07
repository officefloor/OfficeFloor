/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
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