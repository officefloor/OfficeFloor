/*-
 * #%L
 * OfficeCompiler
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
