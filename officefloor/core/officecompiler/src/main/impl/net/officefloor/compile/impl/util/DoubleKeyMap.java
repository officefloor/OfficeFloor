/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
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

package net.officefloor.compile.impl.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Map that provides a double key to obtain the entry.
 * 
 * @author Daniel Sagenschneider
 */
public class DoubleKeyMap<A, B, E> {

	/**
	 * Internal map.
	 */
	private final Map<A, Map<B, E>> registry = new HashMap<A, Map<B, E>>();

	/**
	 * Registers the entry.
	 * 
	 * @param a
	 *            First key for registering.
	 * @param b
	 *            Second key for registering.
	 * @param entry
	 *            Entry to register.
	 */
	public void put(A a, B b, E entry) {

		// Obtain the inside map
		Map<B, E> inside = registry.get(a);
		if (inside == null) {
			inside = new HashMap<B, E>();
			registry.put(a, inside);
		}

		// Register the entry
		inside.put(b, entry);
	}

	/**
	 * Obtains the entry.
	 * 
	 * @param a
	 *            First key registered under.
	 * @param b
	 *            Second key registered under.
	 * @return Entry or <code>null</code> if not found.
	 */
	public E get(A a, B b) {

		// Obtain the inside map
		Map<B, E> inside = registry.get(a);
		if (inside != null) {
			return inside.get(b);
		}

		// Not found
		return null;
	}

}
