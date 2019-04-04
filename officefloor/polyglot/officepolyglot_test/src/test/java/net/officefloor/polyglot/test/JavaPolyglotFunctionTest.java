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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeFlowSourceNode;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.test.officefloor.CompileOfficeContext;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.plugin.managedfunction.clazz.FlowInterface;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.NextFunction;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.plugin.variable.In;
import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import net.officefloor.plugin.variable.Var;
import net.officefloor.server.http.HttpException;
import net.officefloor.web.HttpCookieParameter;
import net.officefloor.web.HttpHeaderParameter;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.ObjectResponse;
import net.officefloor.web.compile.CompileWebContext;

/**
 * Confirms the tests with {@link ClassSectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class JavaPolyglotFunctionTest extends AbstractPolyglotFunctionTest {

	@Override
	protected PrimitiveTypes primitives(boolean _boolean, byte _byte, short _short, char _char, int _int, long _long,
			float _float, double _double) {
		return new PrimitivesLogic().primitives(_boolean, _byte, _short, _char, _int, _long, _float, _double);
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
		public PrimitiveTypes primitives(boolean _boolean, byte _byte, short _short, char _char, int _int, long _long,
				float _float, double _double) {
			return new PrimitiveTypes(_boolean, _byte, _short, _char, _int, _long, _float, _double);
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
	protected void variables(OfficeSectionOutput pass, CompileOfficeContext context, OfficeSectionInput handleResult) {
		OfficeArchitect office = context.getOfficeArchitect();
		OfficeSection function = office.addOfficeSection("section", ClassSectionSource.class.getName(),
				VariableLogic.class.getName());
		office.link(pass, function.getOfficeSectionInput("variable"));
		office.link(function.getOfficeSectionOutput("use"), handleResult);
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

	@Override
	protected void web(String pathParameter, String queryParameter, String headerParameter, String cookieParameter,
			MockHttpParameters httpParameters, MockHttpObject httpObject, ObjectResponse<WebTypes> response) {
		new WebLogic().web(pathParameter, queryParameter, headerParameter, cookieParameter, httpParameters, httpObject,
				response);
	}

	@Override
	protected void web(OfficeFlowSourceNode pass, CompileWebContext context) {
		OfficeArchitect office = context.getOfficeArchitect();
		OfficeSection function = office.addOfficeSection("section", ClassSectionSource.class.getName(),
				WebLogic.class.getName());
		office.link(pass, function.getOfficeSectionInput("web"));
	}

	public static class WebLogic {
		public void web(@HttpPathParameter("param") String pathParameter,
				@HttpQueryParameter("param") String queryParameter,
				@HttpHeaderParameter("param") String headerParameter,
				@HttpCookieParameter("param") String cookieParameter, MockHttpParameters httpParameters,
				MockHttpObject httpObject, ObjectResponse<WebTypes> response) {
			response.send(new WebTypes(pathParameter, queryParameter, headerParameter, cookieParameter, httpParameters,
					httpObject, new JavaObject(pathParameter)));
		}
	}

	@Override
	protected void httpException() throws Exception {
		new HttpExceptionLogic().httpException();
	}

	@Override
	protected void httpException(OfficeFlowSourceNode pass, CompileWebContext context) {
		OfficeArchitect office = context.getOfficeArchitect();
		OfficeSection function = office.addOfficeSection("section", ClassSectionSource.class.getName(),
				HttpExceptionLogic.class.getName());
		office.link(pass, function.getOfficeSectionInput("httpException"));
	}

	public static class HttpExceptionLogic {
		public void httpException() throws HttpException {
			throw new HttpException(422, "test");
		}
	}

	@Override
	protected String flow(CompileOfficeContext context, OfficeSectionInput next, OfficeSectionInput flow,
			OfficeSectionInput flowWithCallback, OfficeSectionInput flowWithParameterAndCallback,
			OfficeSectionInput flowWithParameter, OfficeSectionInput exception) {
		OfficeArchitect office = context.getOfficeArchitect();
		OfficeSection function = office.addOfficeSection("section", ClassSectionSource.class.getName(),
				FlowLogic.class.getName());
		office.link(function.getOfficeSectionOutput("nextFunction"), next);
		office.link(function.getOfficeSectionOutput("flow"), flow);
		office.link(function.getOfficeSectionOutput("flowWithCallback"), flowWithCallback);
		office.link(function.getOfficeSectionOutput("flowWithParameterAndCallback"), flowWithParameterAndCallback);
		office.link(function.getOfficeSectionOutput("flowWithParameter"), flowWithParameter);
		office.link(function.getOfficeSectionOutput(IOException.class.getName()), exception);
		return "section.service";
	}

	@FlowInterface
	public static interface Flows {
		void flow();

		void flowWithCallback(FlowCallback callback);

		void flowWithParameterAndCallback(String parameter, FlowCallback callback);

		void flowWithParameter(String parameter);
	}

	public static class FlowLogic {
		@NextFunction("nextFunction")
		public void service(@Parameter String flowType, Flows flows) throws IOException {
			switch (flowType) {
			case "nextFunction":
				return; // do nothing so next function fires
			case "flow":
				flows.flow();
				return;
			case "callbacks":
				flows.flowWithCallback((error1) -> {
					flows.flowWithParameterAndCallback("1", (error2) -> {
						flows.flowWithParameter("2");
					});
				});
				return;
			case "exception":
				throw new IOException();
			default:
				fail("Invalid flow type: " + flowType);
			}
		}
	}

	@Override
	protected void asynchronousFlow(AsynchronousFlow flowOne, AsynchronousFlow flowTwo) {
		new AsynchronousFlowLogic().service(flowOne, flowTwo);
	}

	@Override
	protected String asynchronousFlow(CompileOfficeContext context) {
		context.addSection("section", AsynchronousFlowLogic.class);
		return "section.service";
	}

	public static class AsynchronousFlowLogic {
		public void service(AsynchronousFlow flowOne, AsynchronousFlow flowTwo) {
			flowOne.complete(() -> flowTwo.complete(null));
		}
	}

}