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

import net.officefloor.activity.impl.procedure.ClassProcedureService;
import net.officefloor.activity.procedure.spi.ProcedureServiceFactory;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.plugin.clazz.FlowInterface;
import net.officefloor.plugin.clazz.Qualified;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.plugin.variable.In;
import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import net.officefloor.plugin.variable.Var;
import net.officefloor.server.http.HttpException;
import net.officefloor.web.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Confirms the tests with {@link ClassSectionSource}.
 *
 * @author Daniel Sagenschneider
 */
public class JavaDefaultProcedureTest extends AbstractPolyglotProcedureTest {

	@Override
	protected Class<? extends ProcedureServiceFactory> getProcedureServiceFactoryClass() {
		return null;
	}

	@Override
	protected String getServiceName(Class<? extends ProcedureServiceFactory> serviceFactory) {
		return ClassProcedureService.SERVICE_NAME; // default service name
	}

	@Override
	protected PrimitiveTypes primitives(boolean _boolean, byte _byte, short _short, char _char, int _int, long _long,
			float _float, double _double) {
		return new PrimitivesLogic().primitives(_boolean, _byte, _short, _char, _int, _long, _float, _double);
	}

	@Override
	protected void primitives(ProcedureBuilder builder) {
		builder.setProcedure(PrimitivesLogic.class, "primitives");
	}

	public static class PrimitivesLogic {
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
	protected void objects(ProcedureBuilder builder) {
		builder.setProcedure(ObjectLogic.class, "object");
	}

	public static class ObjectLogic {
		public ObjectTypes object(String string, JavaObject object, int[] primitiveArray, JavaObject[] objectArray) {
			return new ObjectTypes(string, object, primitiveArray, objectArray);
		}
	}

	@Override
	protected CollectionTypes collections(List<Integer> list, Set<Character> set, Map<String, JavaObject> map) {
		return new CollectionLogic().collection(list, set, map);
	}

	@Override
	protected void collections(ProcedureBuilder builder) {
		builder.setProcedure(CollectionLogic.class, "collection");
	}

	public static class CollectionLogic {
		public CollectionTypes collection(List<Integer> list, Set<Character> set, Map<String, JavaObject> map) {
			return new CollectionTypes(list, set, map);
		}
	}

	@Override
	protected VariableTypes variables(char val, In<String> in, Out<JavaObject> out, Var<Integer> var) {
		return new VariableLogic().variable(val, in, out, var);
	}

	@Override
	protected void variables(ProcedureBuilder builder) {
		builder.setProcedure(VariableLogic.class, "variable");
	}

	public static class VariableLogic {
		public VariableTypes variable(@Val char val, In<String> in, Out<JavaObject> out,
				@Qualified("qualified") Var<Integer> var) {
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
	protected void parameter(ProcedureBuilder builder) {
		builder.setProcedure(ParameterLogic.class, "parameter");
	}

	public static class ParameterLogic {
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
	protected void web(ProcedureBuilder builder) {
		builder.setProcedure(WebLogic.class, "web");
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
	protected void httpException(ProcedureBuilder builder) {
		builder.setProcedure(HttpExceptionLogic.class, "httpException");
	}

	public static class HttpExceptionLogic {
		public void httpException() throws HttpException {
			throw new HttpException(422, "test");
		}
	}

	@Override
	protected void flow(ProcedureBuilder builder) {
		builder.setProcedure(FlowLogic.class, "service");
	}

	@FlowInterface
	public static interface Flows {
		void flow();

		void flowWithCallback(FlowCallback callback);

		void flowWithParameterAndCallback(String parameter, FlowCallback callback);

		void flowWithParameter(String parameter);
	}

	public static class FlowLogic {
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
				break;
			}
		}
	}

	@Override
	protected void asynchronousFlow(AsynchronousFlow flowOne, AsynchronousFlow flowTwo) {
		new AsynchronousFlowLogic().service(flowOne, flowTwo);
	}

	@Override
	protected void asynchronousFlow(ProcedureBuilder builder) {
		builder.setProcedure(AsynchronousFlowLogic.class, "service");
	}

	public static class AsynchronousFlowLogic {
		public void service(AsynchronousFlow flowOne, AsynchronousFlow flowTwo) {
			flowOne.complete(() -> flowTwo.complete(null));
		}
	}

}