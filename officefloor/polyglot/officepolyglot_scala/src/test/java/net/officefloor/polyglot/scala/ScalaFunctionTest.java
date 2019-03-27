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

import java.util.List;
import java.util.Map;
import java.util.Set;

import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.test.officefloor.CompileOfficeContext;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.plugin.variable.In;
import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Var;
import net.officefloor.polyglot.test.AbstractPolyglotFunctionTest;
import net.officefloor.polyglot.test.CollectionTypes;
import net.officefloor.polyglot.test.JavaObject;
import net.officefloor.polyglot.test.ObjectTypes;
import net.officefloor.polyglot.test.ParameterTypes;
import net.officefloor.polyglot.test.PrimitiveTypes;
import net.officefloor.polyglot.test.VariableTypes;

/**
 * Tests adapting a Scala function via a {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public class ScalaFunctionTest extends AbstractPolyglotFunctionTest {

	@Override
	protected PrimitiveTypes primitives(byte _byte, short _short, char _char, int _int, long _long, float _float,
			double _double) {
		return package$.MODULE$.primitives(_byte, _short, _char, _int, _long, _float, _double);
	}

	@Override
	protected String primitives(CompileOfficeContext context, OfficeSectionInput handleResult) {
		// TODO implement AbstractPolyglotFunctionTest.primitives
		throw new UnsupportedOperationException("TODO implement AbstractPolyglotFunctionTest.primitives");
	}

	@Override
	protected ObjectTypes objects(String string, JavaObject object, int[] primitiveArray, JavaObject[] objectArray) {
		return package$.MODULE$.objects(string, object, primitiveArray, objectArray);
	}

	@Override
	protected String objects(CompileOfficeContext context, OfficeSectionInput handleResult) {
		// TODO implement AbstractPolyglotFunctionTest.objects
		throw new UnsupportedOperationException("TODO implement AbstractPolyglotFunctionTest.objects");
	}

	@Override
	protected CollectionTypes collections(List<Integer> list, Set<Character> set, Map<String, JavaObject> map) {
		return package$.MODULE$.collections(list, set, map);
	}

	@Override
	protected String collections(CompileOfficeContext context, OfficeSectionInput handleResult) {
		// TODO implement AbstractPolyglotFunctionTest.collections
		throw new UnsupportedOperationException("TODO implement AbstractPolyglotFunctionTest.collections");
	}

	@Override
	protected VariableTypes variables(char val, In<String> in, Out<JavaObject> out, Var<Integer> var) {
		return package$.MODULE$.variables(val, in, out, var);
	}

	@Override
	protected String variables(CompileOfficeContext context, OfficeSectionInput handleResult) {
		// TODO implement AbstractPolyglotFunctionTest.variables
		throw new UnsupportedOperationException("TODO implement AbstractPolyglotFunctionTest.variables");
	}

	@Override
	protected ParameterTypes parameter(String parameter) {
		return package$.MODULE$.parameter(parameter);
	}

	@Override
	protected void parameter(OfficeSectionOutput pass, CompileOfficeContext context, OfficeSectionInput handleResult) {
		// TODO implement AbstractPolyglotFunctionTest.parameter
		throw new UnsupportedOperationException("TODO implement AbstractPolyglotFunctionTest.parameter");
	}

}