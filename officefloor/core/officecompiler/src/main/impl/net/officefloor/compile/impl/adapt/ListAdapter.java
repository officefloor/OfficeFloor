/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
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

package net.officefloor.compile.impl.adapt;

import java.util.List;

/**
 * Extracts the values of a {@link List}.
 * 
 * @author Daniel Sagenschneider
 */
public class ListAdapter {

	/**
	 * Translates {@link List} to array of its values.
	 * 
	 * @param list {@link List}.
	 * @return Array of {@link List} values.
	 */
	public static Object[] toArray(Object list) {
		return ((List<?>) list).toArray();
	}

}
