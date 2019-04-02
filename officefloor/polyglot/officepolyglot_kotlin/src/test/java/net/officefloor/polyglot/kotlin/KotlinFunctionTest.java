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
package net.officefloor.polyglot.kotlin;

import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.AssertionFailedError;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.test.officefloor.CompileOfficeContext;
import net.officefloor.compile.test.section.SectionLoaderUtil;
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
 * Tests adapting Kotlin {@link Object} for {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public class KotlinFunctionTest extends AbstractPolyglotFunctionTest {

	/**
	 * Ensure using top level functions.
	 */
	public void testNonFunctions() {
		boolean isSuccessful;
		try {
			SectionLoaderUtil.loadSectionType(KotlinFunctionSectionSource.class, KotlinObject.class.getName(),
					KotlinFunctionSectionSource.PROPERTY_FUNCTION_NAME, "not_available");
			isSuccessful = true;
		} catch (AssertionFailedError ex) {

			// Ensure reasonse
			assertTrue("Incorrect cause: " + ex.getMessage(), ex.getMessage().startsWith(
					"Class " + KotlinObject.class.getName() + " is not top level Kotlin functions (should end in Kt)"));

			isSuccessful = false;
		}
		assertFalse("Should not be successful", isSuccessful);
	}

	/*
	 * ========================= AbstractPolyglotFunctionTest =====================
	 */

	@Override
	protected PrimitiveTypes primitives(boolean _boolean, byte _byte, short _short, char _char, int _int, long _long,
			float _float, double _double) {
		return KotlinFunctionsKt.primitives(_boolean, _byte, _short, _char, _int, _long, _float, _double);
	}

	@Override
	protected String primitives(CompileOfficeContext context, OfficeSectionInput handleResult) {
		OfficeArchitect office = context.getOfficeArchitect();
		OfficeSection function = office.addOfficeSection("section", KotlinFunctionSectionSource.class.getName(),
				KotlinFunctionsKt.class.getName());
		function.addProperty(KotlinFunctionSectionSource.PROPERTY_FUNCTION_NAME, "primitives");
		office.link(function.getOfficeSectionOutput("use"), handleResult);
		return "section.primitives";
	}

	@Override
	protected ObjectTypes objects(String string, JavaObject object, int[] primitiveArray, JavaObject[] objectArray) {
		return KotlinFunctionsKt.objects(string, object, primitiveArray, objectArray);
	}

	@Override
	protected String objects(CompileOfficeContext context, OfficeSectionInput handleResult) {
		OfficeArchitect office = context.getOfficeArchitect();
		OfficeSection function = office.addOfficeSection("section", KotlinFunctionSectionSource.class.getName(),
				KotlinFunctionsKt.class.getName());
		function.addProperty(KotlinFunctionSectionSource.PROPERTY_FUNCTION_NAME, "objects");
		office.link(function.getOfficeSectionOutput("use"), handleResult);
		return "section.objects";
	}

	@Override
	protected CollectionTypes collections(List<Integer> list, Set<Character> set, Map<String, JavaObject> map) {
		return KotlinFunctionsKt.collections(list, set, map);
	}

	@Override
	protected String collections(CompileOfficeContext context, OfficeSectionInput handleResult) {
		OfficeArchitect office = context.getOfficeArchitect();
		OfficeSection function = office.addOfficeSection("section", KotlinFunctionSectionSource.class.getName(),
				KotlinFunctionsKt.class.getName());
		function.addProperty(KotlinFunctionSectionSource.PROPERTY_FUNCTION_NAME, "collections");
		office.link(function.getOfficeSectionOutput("use"), handleResult);
		return "section.collections";
	}

	@Override
	protected VariableTypes variables(char val, In<String> in, Out<JavaObject> out, Var<Integer> var) {
		return KotlinFunctionsKt.variables(val, in, out, var);
	}

	@Override
	protected void variables(OfficeSectionOutput pass, CompileOfficeContext context, OfficeSectionInput handleResult) {
		OfficeArchitect office = context.getOfficeArchitect();
		OfficeSection function = office.addOfficeSection("section", KotlinFunctionSectionSource.class.getName(),
				KotlinFunctionsKt.class.getName());
		function.addProperty(KotlinFunctionSectionSource.PROPERTY_FUNCTION_NAME, "variables");
		office.link(pass, function.getOfficeSectionInput("variables"));
		office.link(function.getOfficeSectionOutput("use"), handleResult);
	}

	@Override
	protected ParameterTypes parameter(String parameter) {
		return KotlinFunctionsKt.parameters(parameter);
	}

	@Override
	protected void parameter(OfficeSectionOutput pass, CompileOfficeContext context, OfficeSectionInput handleResult) {
		OfficeArchitect office = context.getOfficeArchitect();
		OfficeSection function = office.addOfficeSection("section", KotlinFunctionSectionSource.class.getName(),
				KotlinFunctionsKt.class.getName());
		function.addProperty(KotlinFunctionSectionSource.PROPERTY_FUNCTION_NAME, "parameters");
		office.link(pass, function.getOfficeSectionInput("parameters"));
		office.link(function.getOfficeSectionOutput("use"), handleResult);
	}

}