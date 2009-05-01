/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.compile.impl.util;

import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the multiple key maps.
 * 
 * @author Daniel
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