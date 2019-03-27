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

import java.util.List;
import java.util.Map;
import java.util.Set;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.test.officefloor.CompileOfficeContext;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.NextFunction;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.plugin.variable.In;
import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import net.officefloor.plugin.variable.Var;

/**
 * Confirms the tests with {@link ClassSectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class JavaPolyglotFunctionTest extends AbstractPolyglotFunctionTest {

	@Override
	protected PrimitiveTypes primitives(byte _byte, short _short, char _char, int _int, long _long, float _float,
			double _double) {
		return new PrimitivesLogic().primitives(_byte, _short, _char, _int, _long, _float, _double);
	}

	@Override
	protected String primitives(CompileOfficeContext context, OfficeSectionInput handleResult) {
		OfficeArchitect office = context.getOfficeArchitect();
		OfficeSection function = office.addOfficeSection("section", ClassSectionSource.class.getName(),
				PrimitivesLogic.class.getName());
		office.link(function.getOfficeSectionOutput("use"), handleResult);
		return "section.primitives";
	}

	public static class PrimitivesLogic {
		@NextFunction("use")
		public PrimitiveTypes primitives(byte _byte, short _short, char _char, int _int, long _long, float _float,
				double _double) {
			return new PrimitiveTypes(_byte, _short, _char, _int, _long, _float, _double);
		}
	}

	@Override
	protected ObjectTypes objects(String string, JavaObject object, int[] primitiveArray, JavaObject[] objectArray) {
		return new ObjectLogic().object(string, object, primitiveArray, objectArray);
	}

	@Override
	protected String objects(CompileOfficeContext context, OfficeSectionInput handleResult) {
		OfficeArchitect office = context.getOfficeArchitect();
		OfficeSection function = office.addOfficeSection("section", ClassSectionSource.class.getName(),
				ObjectLogic.class.getName());
		office.link(function.getOfficeSectionOutput("use"), handleResult);
		return "section.object";
	}

	public static class ObjectLogic {
		@NextFunction("use")
		public ObjectTypes object(String string, JavaObject object, int[] primitiveArray, JavaObject[] objectArray) {
			return new ObjectTypes(string, object, primitiveArray, objectArray);
		}
	}

	@Override
	protected CollectionTypes collections(List<Integer> list, Set<Character> set, Map<String, JavaObject> map) {
		return new CollectionLogic().collection(list, set, map);
	}

	@Override
	protected String collections(CompileOfficeContext context, OfficeSectionInput handleResult) {
		OfficeArchitect office = context.getOfficeArchitect();
		OfficeSection function = office.addOfficeSection("section", ClassSectionSource.class.getName(),
				CollectionLogic.class.getName());
		office.link(function.getOfficeSectionOutput("use"), handleResult);
		return "section.collection";
	}

	public static class CollectionLogic {
		@NextFunction("use")
		public CollectionTypes collection(List<Integer> list, Set<Character> set, Map<String, JavaObject> map) {
			return new CollectionTypes(list, set, map);
		}
	}

	@Override
	protected VariableTypes variables(char val, In<String> in, Out<JavaObject> out, Var<Integer> var) {
		return new VariableLogic().variable(val, in, out, var);
	}

	@Override
	protected String variables(CompileOfficeContext context, OfficeSectionInput handleResult) {
		OfficeArchitect office = context.getOfficeArchitect();
		OfficeSection function = office.addOfficeSection("section", ClassSectionSource.class.getName(),
				VariableLogic.class.getName());
		office.link(function.getOfficeSectionOutput("use"), handleResult);
		return "section.variable";
	}

	public static class VariableLogic {
		@NextFunction("use")
		public VariableTypes variable(@Val char val, In<String> in, Out<JavaObject> out, Var<Integer> var) {
			out.set(new JavaObject("test"));
			int varValue = var.get();
			var.set(varValue + 1);
			return new VariableTypes(val, in.get(), varValue);
		}
	}

	@Override
	protected ParameterTypes parameter(String parameter) {
		return new ParameterLogic().parameter(parameter);
	}

	@Override
	protected void parameter(OfficeSectionOutput pass, CompileOfficeContext context, OfficeSectionInput handleResult) {
		OfficeArchitect office = context.getOfficeArchitect();
		OfficeSection function = office.addOfficeSection("section", ClassSectionSource.class.getName(),
				ParameterLogic.class.getName());
		office.link(pass, function.getOfficeSectionInput("parameter"));
		office.link(function.getOfficeSectionOutput("use"), handleResult);
	}

	public static class ParameterLogic {
		@NextFunction("use")
		public ParameterTypes parameter(@Parameter String parameter) {
			return new ParameterTypes(parameter);
		}
	}

}