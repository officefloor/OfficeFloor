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
package net.officefloor.polyglot.scala;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests adapting a Scala function via a {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public class ScalaManagedFunctionTest extends OfficeFrameTestCase {

	/**
	 * Ensure can use unified types.
	 */
	public void testDirectUnifiedTypes() {
		UnifiedTypes types = package$.MODULE$.unifiedTypes((byte) 1, (short) 2, '3', 4, 5L, 6.0f, 7.0);
		assertEquals("byte", 1, types.getByte());
		assertEquals("short", 2, types.getShort());
		assertEquals("char", '3', types.getChar());
		assertEquals("integer", 4, types.getInt());
		assertEquals("long", 5, types.getLong());
		assertEquals("float", 6.0f, types.getFloat());
		assertEquals("double", 7.0, types.getDouble());
	}

	/**
	 * Ensure can use Scala object.
	 */
	public void testDirectScalaObject() {
		assertEquals("byte", 1, ScalaObject.getByte());
		assertEquals("short", 2, ScalaObject.getShort());
		assertEquals("char", '3', ScalaObject.getChar());
		assertEquals("integer", 4, ScalaObject.getInt());
		assertEquals("long", 5, ScalaObject.getLong());
		assertEquals("float", 6.0f, ScalaObject.getFloat());
		assertEquals("double", 7.0, ScalaObject.getDouble());
	}

	/**
	 * Ensure can pass in a Java object.
	 */
	public void testDirectJavaObject() {
		JavaObject object = new JavaObject();
		assertSame("Incorrect object", object, package$.MODULE$.objects(object));
	}

	/**
	 * Ensure can pass collections.
	 */
	public void testDirectCollections() {
		List<Object> list = new LinkedList<>();
		Set<Object> set = new HashSet<>();
		Map<String, Object> map = new HashMap<>();
		Collection<Object> collection = new ArrayList<>();
		CollectionTypes types = package$.MODULE$.collections(list, set, map, collection);
		assertSame("list", list, types.getList());
		assertSame("set", set, types.getSet());
		assertSame("map", map, types.getMap());
		assertSame("collection", collection, types.getCollection());
	}

}