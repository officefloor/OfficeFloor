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
package net.officefloor.compile.impl.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Map that provides a triple key to obtain the entry.
 * 
 * @author Daniel Sagenschneider
 */
public class TripleKeyMap<A, B, C, E> {

	/**
	 * Internal map.
	 */
	private final Map<A, Map<B, Map<C, E>>> registry = new HashMap<A, Map<B, Map<C, E>>>();

	/**
	 * Registers the entry.
	 * 
	 * @param a
	 *            First key for registering.
	 * @param b
	 *            Second key for registering.
	 * @param c
	 *            Third key for registering.
	 * @param entry
	 *            Entry to register.
	 */
	public void put(A a, B b, C c, E entry) {

		// Obtain the b map
		Map<B, Map<C, E>> bMap = registry.get(a);
		if (bMap == null) {
			bMap = new HashMap<B, Map<C, E>>();
			registry.put(a, bMap);
		}

		// Obtain the c map
		Map<C, E> cMap = bMap.get(b);
		if (cMap == null) {
			cMap = new HashMap<C, E>();
			bMap.put(b, cMap);
		}

		// Register the entry
		cMap.put(c, entry);
	}

	/**
	 * Obtains the entry.
	 * 
	 * @param a
	 *            First key registered under.
	 * @param b
	 *            Second key registered under.
	 * @param c
	 *            Third key registered under.
	 * @return Entry or <code>null</code> if not found.
	 */
	public E get(A a, B b, C c) {

		// Obtain the maps and then entry
		Map<B, Map<C, E>> bMap = registry.get(a);
		if (bMap != null) {
			Map<C, E> cMap = bMap.get(b);
			if (cMap != null) {
				return cMap.get(c);
			}
		}

		// Not found
		return null;
	}

}