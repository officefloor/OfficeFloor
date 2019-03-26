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
package net.officefloor.polyglot.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.test.officefloor.CompileOfficeContext;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.compile.test.officefloor.CompileVar;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.test.variable.MockVar;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.plugin.variable.In;
import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Var;

/**
 * Abstract tests for a polyglot function via a {@link SectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractPolyglotFunctionTest extends OfficeFrameTestCase {

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor;

	@Override
	protected void tearDown() throws Exception {
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Ensure can use primitive types.
	 */
	public void testDirectPrimitives() {
		PrimitiveTypes types = this.primitives((byte) 1, (short) 2, '3', 4, 5L, 6.0f, 7.0);
		assertPrimitives(types);
	}

	protected abstract PrimitiveTypes primitives(byte _byte, short _short, char _char, int _int, long _long,
			float _float, double _double);

	/**
	 * Ensure can invoke primitive types.
	 */
	public void testInvokePrimitives() throws Throwable {
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		Closure<String> functionName = new Closure<>();
		CompileVar<PrimitiveTypes> primitives = new CompileVar<>();
		compiler.office((context) -> {
			value(context, Byte.valueOf((byte) 1));
			value(context, Short.valueOf((short) 2));
			value(context, Character.valueOf('3'));
			value(context, Integer.valueOf(4));
			value(context, Long.valueOf(5));
			value(context, Float.valueOf(6.0f));
			value(context, Double.valueOf(7.0));
			context.variable(null, PrimitiveTypes.class, primitives);
			functionName.value = AbstractPolyglotFunctionTest.this.primitives(context);
		});
		this.officeFloor = compiler.compileAndOpenOfficeFloor();
		CompileOfficeFloor.invokeProcess(this.officeFloor, functionName.value, null);
		assertPrimitives(primitives.getValue());
	}

	protected abstract String primitives(CompileOfficeContext context);

	private static void assertPrimitives(PrimitiveTypes types) {
		assertEquals("byte", 1, types.getByte());
		assertEquals("short", 2, types.getShort());
		assertEquals("char", '3', types.getChar());
		assertEquals("integer", 4, types.getInt());
		assertEquals("long", 5, types.getLong());
		assertEquals("float", 6.0f, types.getFloat());
		assertEquals("double", 7.0, types.getDouble());
	}

	/**
	 * Ensure can pass in a Java object.
	 */
	public void testDirectJavaObject() {
		JavaObject object = new JavaObject("test");
		assertSame("Incorrect object", object, this.objects(object));
	}

	protected abstract JavaObject objects(JavaObject object);

	/**
	 * Ensure can pass collections.
	 */
	public void testDirectCollections() {
		List<Integer> list = new LinkedList<>();
		Set<Character> set = new HashSet<>();
		Map<String, JavaObject> map = new HashMap<>();
		Collection<Float> collection = new ArrayList<>();
		CollectionTypes types = this.collections(list, set, map, collection);
		assertSame("list", list, types.getList());
		assertSame("set", set, types.getSet());
		assertSame("map", map, types.getMap());
		assertSame("collection", collection, types.getCollection());
	}

	protected abstract CollectionTypes collections(List<Integer> list, Set<Character> set, Map<String, JavaObject> map,
			Collection<Float> collection);

	/**
	 * Ensure can handle variables.
	 */
	public void testDirectVariables() {
		MockVar<String> in = new MockVar<>("2");
		MockVar<JavaObject> out = new MockVar<>();
		MockVar<Integer> var = new MockVar<>(3);
		VariableTypes types = this.variables('1', in, out, var);
		assertEquals("val", '1', types.getVal());
		assertEquals("in", "2", types.getIn());
		assertEquals("var", 3, types.getVar());
		assertEquals("update out", "test", out.get().getIdentifier());
		assertEquals("update var", Integer.valueOf(4), var.get());
	}

	protected abstract VariableTypes variables(char val, In<String> in, Out<JavaObject> out, Var<Integer> var);

	/**
	 * Ensure can provide {@link Parameter}.
	 */
	public void testDirectParameter() {
		String result = this.parameter("test");
		assertEquals("parameter", "test", result);
	}

	protected abstract String parameter(String parameter);

	/**
	 * Loads a value.
	 * 
	 * @param context {@link CompileOfficeContext}.
	 * @param value   Value.
	 */
	private static void value(CompileOfficeContext context, Object value) {
		context.getOfficeArchitect()
				.addOfficeManagedObjectSource(value.getClass().getName(), new ValueManagedObjectSource(value))
				.addOfficeManagedObject(value.getClass().getName(), ManagedObjectScope.THREAD);
	}

	/**
	 * Value {@link ManagedObjectSource}.
	 */
	private static class ValueManagedObjectSource extends AbstractManagedObjectSource<None, None>
			implements ManagedObject {

		/**
		 * Value.
		 */
		private final Object value;

		/***
		 * Instantiate.
		 * 
		 * @param value Value.
		 */
		private ValueManagedObjectSource(Object value) {
			this.value = value;
		}

		/*
		 * ======================= ManagedObjectSource ===================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No specification
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
			context.setObjectClass(this.value.getClass());
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			return this;
		}

		/*
		 * =================== ManagedObject ==============================
		 */

		@Override
		public Object getObject() throws Throwable {
			return this.value;
		}
	}

}