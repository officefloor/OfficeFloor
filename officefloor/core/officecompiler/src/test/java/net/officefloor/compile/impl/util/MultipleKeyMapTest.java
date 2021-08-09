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
