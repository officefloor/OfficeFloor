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

import junit.framework.AssertionFailedError;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeFlowSourceNode;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.test.officefloor.CompileOfficeContext;
import net.officefloor.compile.test.section.SectionLoaderUtil;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.plugin.variable.In;
import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Var;
import net.officefloor.polyglot.test.AbstractPolyglotFunctionTest;
import net.officefloor.polyglot.test.CollectionTypes;
import net.officefloor.polyglot.test.JavaObject;
import net.officefloor.polyglot.test.MockHttpObject;
import net.officefloor.polyglot.test.MockHttpParameters;
import net.officefloor.polyglot.test.ObjectTypes;
import net.officefloor.polyglot.test.ParameterTypes;
import net.officefloor.polyglot.test.PrimitiveTypes;
import net.officefloor.polyglot.test.VariableTypes;
import net.officefloor.polyglot.test.WebTypes;
import net.officefloor.web.ObjectResponse;
import net.officefloor.web.compile.CompileWebContext;

/**
 * Tests adapting a Scala function via a {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public class ScalaFunctionTest extends AbstractPolyglotFunctionTest {

	/**
	 * Ensure issue if try to use non Scala object.
	 */
	public void testNonScalaObject() {
		boolean isSuccessful;
		try {
			SectionLoaderUtil.loadSectionType(ScalaFunctionSectionSource.class, NotScalaObject.class.getName(),
					ScalaFunctionSectionSource.PROPERTY_FUNCTION_NAME, "not_available");
			isSuccessful = true;
		} catch (AssertionFailedError ex) {

			// Ensure reasonse
			assertTrue("Incorrect cause: " + ex.getMessage(), ex.getMessage().startsWith("Class "
					+ NotScalaObject.class.getName() + " is not Scala Object (expecting MODULE$ static field)"));

			isSuccessful = false;
		}
		assertFalse("Should not be successful", isSuccessful);
	}

	/*
	 * ====================== AbstractPolyglotFunctionTest =========================
	 */

	@Override
	protected PrimitiveTypes primitives(boolean _boolean, byte _byte, short _short, char _char, int _int, long _long,
			float _float, double _double) {
		return package$.MODULE$.primitives(_boolean, _byte, _short, _char, _int, _long, _float, _double);
	}

	@Override
	protected String primitives(CompileOfficeContext context, OfficeSectionInput handleResult) {
		OfficeArchitect office = context.getOfficeArchitect();
		OfficeSection function = office.addOfficeSection("section", ScalaFunctionSectionSource.class.getName(),
				package$.class.getName());
		function.addProperty(ScalaFunctionSectionSource.PROPERTY_FUNCTION_NAME, "primitives");
		office.link(function.getOfficeSectionOutput("use"), handleResult);
		return "section.primitives";
	}

	@Override
	protected ObjectTypes objects(String string, JavaObject object, int[] primitiveArray, JavaObject[] objectArray) {
		return package$.MODULE$.objects(string, object, primitiveArray, objectArray);
	}

	@Override
	protected String objects(CompileOfficeContext context, OfficeSectionInput handleResult) {
		OfficeArchitect office = context.getOfficeArchitect();
		OfficeSection function = office.addOfficeSection("section", ScalaFunctionSectionSource.class.getName(),
				package$.class.getName());
		function.addProperty(ScalaFunctionSectionSource.PROPERTY_FUNCTION_NAME, "objects");
		office.link(function.getOfficeSectionOutput("use"), handleResult);
		return "section.objects";
	}

	@Override
	protected CollectionTypes collections(List<Integer> list, Set<Character> set, Map<String, JavaObject> map) {
		return package$.MODULE$.collections(list, set, map);
	}

	@Override
	protected String collections(CompileOfficeContext context, OfficeSectionInput handleResult) {
		OfficeArchitect office = context.getOfficeArchitect();
		OfficeSection function = office.addOfficeSection("section", ScalaFunctionSectionSource.class.getName(),
				package$.class.getName());
		function.addProperty(ScalaFunctionSectionSource.PROPERTY_FUNCTION_NAME, "collections");
		office.link(function.getOfficeSectionOutput("use"), handleResult);
		return "section.collections";
	}

	@Override
	protected VariableTypes variables(char val, In<String> in, Out<JavaObject> out, Var<Integer> var) {
		return package$.MODULE$.variables(val, in, out, var);
	}

	@Override
	protected void variables(OfficeSectionOutput pass, CompileOfficeContext context, OfficeSectionInput handleResult) {
		OfficeArchitect office = context.getOfficeArchitect();
		OfficeSection function = office.addOfficeSection("section", ScalaFunctionSectionSource.class.getName(),
				package$.class.getName());
		function.addProperty(ScalaFunctionSectionSource.PROPERTY_FUNCTION_NAME, "variables");
		office.link(pass, function.getOfficeSectionInput("variables"));
		office.link(function.getOfficeSectionOutput("use"), handleResult);
	}

	@Override
	protected ParameterTypes parameter(String parameter) {
		return package$.MODULE$.parameter(parameter);
	}

	@Override
	protected void parameter(OfficeSectionOutput pass, CompileOfficeContext context, OfficeSectionInput handleResult) {
		OfficeArchitect office = context.getOfficeArchitect();
		OfficeSection function = office.addOfficeSection("section", ScalaFunctionSectionSource.class.getName(),
				package$.class.getName());
		function.addProperty(ScalaFunctionSectionSource.PROPERTY_FUNCTION_NAME, "parameter");
		office.link(pass, function.getOfficeSectionInput("parameter"));
		office.link(function.getOfficeSectionOutput("use"), handleResult);
	}

	@Override
	protected void web(String pathParameter, String queryParameter, String headerParameter, String cookieParameter,
			MockHttpParameters httpParameters, MockHttpObject httpObject, ObjectResponse<WebTypes> response) {
		package$.MODULE$.web(pathParameter, queryParameter, headerParameter, cookieParameter, httpParameters,
				httpObject, response);
	}

	@Override
	protected void web(OfficeFlowSourceNode pass, CompileWebContext context) {
		OfficeArchitect office = context.getOfficeArchitect();
		OfficeSection function = office.addOfficeSection("section", ScalaFunctionSectionSource.class.getName(),
				package$.class.getName());
		function.addProperty(ScalaFunctionSectionSource.PROPERTY_FUNCTION_NAME, "web");
		office.link(pass, function.getOfficeSectionInput("web"));
	}

	@Override
	protected void httpException() throws Exception {
		package$.MODULE$.httpException();
	}

	@Override
	protected void httpException(OfficeFlowSourceNode pass, CompileWebContext context) {
		OfficeArchitect office = context.getOfficeArchitect();
		OfficeSection function = office.addOfficeSection("section", ScalaFunctionSectionSource.class.getName(),
				package$.class.getName());
		function.addProperty(ScalaFunctionSectionSource.PROPERTY_FUNCTION_NAME, "httpException");
		office.link(pass, function.getOfficeSectionInput("httpException"));
	}

	@Override
	protected String flow(CompileOfficeContext context, OfficeSectionInput next, OfficeSectionInput flow,
			OfficeSectionInput flowWithCallback, OfficeSectionInput flowWithParameterAndCallback,
			OfficeSectionInput flowWithParameter, OfficeSectionInput exception) {
		OfficeArchitect office = context.getOfficeArchitect();
		OfficeSection function = office.addOfficeSection("section", ScalaFunctionSectionSource.class.getName(),
				package$.class.getName());
		function.addProperty(ScalaFunctionSectionSource.PROPERTY_FUNCTION_NAME, "serviceFlow");
		office.link(function.getOfficeSectionOutput("nextFunction"), next);
		office.link(function.getOfficeSectionOutput("flow"), flow);
		office.link(function.getOfficeSectionOutput("flowWithCallback"), flowWithCallback);
		office.link(function.getOfficeSectionOutput("flowWithParameterAndCallback"), flowWithParameterAndCallback);
		office.link(function.getOfficeSectionOutput("flowWithParameter"), flowWithParameter);
		office.link(function.getOfficeSectionOutput("exception"), exception);
		return "section.serviceFlow";
	}

	@Override
	protected void asynchronousFlow(AsynchronousFlow flowOne, AsynchronousFlow flowTwo) {
		package$.MODULE$.asynchronousFlow(flowOne, flowTwo);
	}

	@Override
	protected String asynchronousFlow(CompileOfficeContext context) {
		OfficeArchitect office = context.getOfficeArchitect();
		OfficeSection function = office.addOfficeSection("section", ScalaFunctionSectionSource.class.getName(),
				package$.class.getName());
		function.addProperty(ScalaFunctionSectionSource.PROPERTY_FUNCTION_NAME, "asynchronousFlow");
		return "section.asynchronousFlow";
	}

}