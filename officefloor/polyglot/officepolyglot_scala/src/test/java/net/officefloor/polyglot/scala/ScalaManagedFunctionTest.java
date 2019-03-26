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

import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.compile.spi.section.SectionFunctionNamespace;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.compile.test.section.SectionLoaderUtil;
import net.officefloor.extension.CompileOffice;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.test.variable.MockVar;
import net.officefloor.plugin.section.clazz.Parameter;

/**
 * Tests adapting a Scala function via a {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public class ScalaManagedFunctionTest extends OfficeFrameTestCase {

	/**
	 * Ensure can use primitive types.
	 */
	public void testDirectPrimitiveTypes() {
		PrimitiveTypes types = package$.MODULE$.primitiveTypes((byte) 1, (short) 2, '3', 4, 5L, 6.0f, 7.0);
		assertEquals("byte", 1, types.getByte());
		assertEquals("short", 2, types.getShort());
		assertEquals("char", '3', types.getChar());
		assertEquals("integer", 4, types.getInt());
		assertEquals("long", 5, types.getLong());
		assertEquals("float", 6.0f, types.getFloat());
		assertEquals("double", 7.0, types.getDouble());
	}

	/**
	 * Ensure correct {@link ManagedFunctionType}.
	 */
	public void testTypePrimitive() {

		// TODO fix up
		if (true) {
			System.err.println("TODO: testTypePrimitive");
			return;
		}

		// Create expected type
		SectionDesigner expected = SectionLoaderUtil.createSectionDesigner();
		SectionFunctionNamespace namespace = expected.addSectionFunctionNamespace("SCALA",
				ScalaManagedFunctionSource.class.getName());
		SectionFunction function = namespace.addSectionFunction("primitiveTypes", "primitiveTypes");
		function.getFunctionObject("Byte");
		expected.addSectionInput("primitiveTypes", null);

		// Ensure correct type
		SectionLoaderUtil.validateOfficeSection(expected, ScalaSectionSource.class, package$.class.getName(),
				"function", "primitiveTypes");
	}
	
	/**
	 * Ensure can invoke primitive types.
	 */
	public void testInvokePrimitiveTypes() {
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		compiler.office((context) -> {
			
		});
		
	}

	/**
	 * Ensure can pass in a Java object.
	 */
	public void testDirectJavaObject() {
		JavaObject object = new JavaObject("test");
		assertSame("Incorrect object", object, package$.MODULE$.objects(object));
	}

	/**
	 * Ensure can pass collections.
	 */
	public void testDirectCollections() {
		List<Integer> list = new LinkedList<>();
		Set<Character> set = new HashSet<>();
		Map<String, JavaObject> map = new HashMap<>();
		Collection<Float> collection = new ArrayList<>();
		CollectionTypes types = package$.MODULE$.collections(list, set, map, collection);
		assertSame("list", list, types.getList());
		assertSame("set", set, types.getSet());
		assertSame("map", map, types.getMap());
		assertSame("collection", collection, types.getCollection());
	}

	/**
	 * Ensure can handle variables.
	 */
	public void testDirectVariables() {
		MockVar<String> in = new MockVar<>("2");
		MockVar<JavaObject> out = new MockVar<>();
		MockVar<Integer> var = new MockVar<>(3);
		VariableTypes types = package$.MODULE$.variables('1', in, out, var);
		assertEquals("val", '1', types.getVal());
		assertEquals("in", "2", types.getIn());
		assertEquals("var", 3, types.getVar());
		assertEquals("update out", "test", out.get().getIdentifier());
		assertEquals("update var", Integer.valueOf(4), var.get());
	}

	/**
	 * Ensure can provide {@link Parameter}.
	 */
	public void testDirectParameter() {
		String result = package$.MODULE$.parameters("test");
		assertEquals("parameter", "test", result);
	}

}