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