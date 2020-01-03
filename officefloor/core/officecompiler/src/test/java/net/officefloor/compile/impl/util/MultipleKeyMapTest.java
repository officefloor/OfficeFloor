package net.officefloor.compile.impl.util;

import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the multiple key maps.
 * 
 * @author Daniel Sagenschneider
 */
public class MultipleKeyMapTest extends OfficeFrameTestCase {

	/**
	 * Tests the {@link DoubleKeyMap}.
	 */
	public void testDoubleKeyMap() {

		// Create the map
		DoubleKeyMap<String, String, Object> map = new DoubleKeyMap<String, String, Object>();

		// Ensure handle not available
		assertNull("Should not find", map.get("A", "B"));

		// Ensure can add and retrieve
		Object entry = new Object();
		map.put("A", "B", entry);
		assertEquals("Incorrect entry", entry, map.get("A", "B"));
	}

	/**
	 * Tests the {@link TripleKeyMap}.
	 */
	public void testTripleKeyMap() {

		// Create the map
		TripleKeyMap<String, String, String, Object> map = new TripleKeyMap<String, String, String, Object>();

		// Ensure handle not available
		assertNull("Should not find", map.get("A", "B", "C"));

		// Ensure can add and retrieve
		Object entry = new Object();
		map.put("A", "B", "C", entry);
		assertEquals("Incorrect entry", entry, map.get("A", "B", "C"));
	}

}