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

import com.fasterxml.jackson.module.scala.DefaultScalaModule;

import junit.framework.AssertionFailedError;
import net.officefloor.activity.impl.procedure.ClassProcedureSource;
import net.officefloor.activity.procedure.ProcedureLoaderUtil;
import net.officefloor.activity.procedure.build.ProcedureArchitect;
import net.officefloor.activity.procedure.build.ProcedureEmployer;
import net.officefloor.activity.procedure.spi.ProcedureSourceServiceFactory;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeFlowSourceNode;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.test.officefloor.CompileOfficeContext;
import net.officefloor.compile.test.section.SectionLoaderUtil;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.test.Closure;
import net.officefloor.plugin.variable.In;
import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Var;
import net.officefloor.polyglot.test.AbstractPolyglotProcedureTest;
import net.officefloor.polyglot.test.CollectionTypes;
import net.officefloor.polyglot.test.JavaObject;
import net.officefloor.polyglot.test.MockHttpObject;
import net.officefloor.polyglot.test.MockHttpParameters;
import net.officefloor.polyglot.test.ObjectTypes;
import net.officefloor.polyglot.test.ParameterTypes;
import net.officefloor.polyglot.test.PrimitiveTypes;
import net.officefloor.polyglot.test.VariableTypes;
import net.officefloor.polyglot.test.WebTypes;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.ObjectResponse;
import net.officefloor.web.compile.CompileWebContext;
import net.officefloor.web.compile.WebCompileOfficeFloor;

/**
 * Tests adapting a Scala function to a
 * {@link net.officefloor.activity.procedure.Procedure}.
 *
 * @author Daniel Sagenschneider
 */
public class ScalaProcedureTest extends AbstractPolyglotProcedureTest {

	static {
		mapper.registerModule(new DefaultScalaModule());
	}

	/**
	 * Ensure no procedures when using non Scala object.
	 */
	public void testNonScalaObject() {
		ProcedureLoaderUtil.validateProcedures(NotScalaObject.class);
	}

	/**
	 * Ensure list {@link net.officefloor.activity.procedure.Procedure} instances.
	 */
	public void testListProcedures() {
		ProcedureLoaderUtil.validateProcedures(package$.class,
				ProcedureLoaderUtil.procedure("asynchronousFlow", ScalaProcedureSourceServiceFactory.class),
				ProcedureLoaderUtil.procedure("collections", ScalaProcedureSourceServiceFactory.class),
				ProcedureLoaderUtil.procedure("httpException", ScalaProcedureSourceServiceFactory.class),
				ProcedureLoaderUtil.procedure("objects", ScalaProcedureSourceServiceFactory.class),
				ProcedureLoaderUtil.procedure("parameter", ScalaProcedureSourceServiceFactory.class),
				ProcedureLoaderUtil.procedure("primitives", ScalaProcedureSourceServiceFactory.class),
				ProcedureLoaderUtil.procedure("serviceFlow", ScalaProcedureSourceServiceFactory.class),
				ProcedureLoaderUtil.procedure("variables", ScalaProcedureSourceServiceFactory.class),
				ProcedureLoaderUtil.procedure("web", ScalaProcedureSourceServiceFactory.class));
	}

	/**
	 * Ensure able to send Scala class.
	 */
	public void testWebSendScalaClass() throws Throwable {
		WebCompileOfficeFloor compiler = new WebCompileOfficeFloor();
		Closure<MockHttpServer> server = new Closure<>();
		compiler.mockHttpServer((mockServer) -> server.value = mockServer);
		compiler.web((context) -> {
			OfficeArchitect officeArchitect = context.getOfficeArchitect();
			ProcedureArchitect<OfficeSection> procedureArchitect = ProcedureEmployer
					.employProcedureArchitect(officeArchitect, context.getOfficeSourceContext());
			OfficeSection procedure = procedureArchitect.addProcedure(ScalaRequestService.class.getName(),
					ClassProcedureSource.SOURCE_NAME, "service", false, new PropertyListImpl());
			officeArchitect.link(context.getWebArchitect().getHttpInput(false, "/").getInput(),
					procedure.getOfficeSectionInput(ProcedureArchitect.INPUT_NAME));
		});
		this.officeFloor = compiler.compileAndOpenOfficeFloor();
		MockHttpResponse response = server.value
				.send(MockHttpServer.mockRequest().header("Content-Type", "application/json")
						.entity(mapper.writeValueAsString(new ScalaRequest(1, "test"))));
		response.assertResponse(200, "{\"identifier\":2,\"message\":\"Serviced test\"}");
	}

	public static class ScalaRequestService {
		public void service(ScalaRequest request, ObjectResponse<ScalaRequest> response) {
			response.send(new ScalaRequest(request.identifier() + 1, "Serviced " + request.message()));
		}
	}

	/*
	 * ====================== AbstractPolyglotFunctionTest =========================
	 */

	@Override
	protected Class<? extends ProcedureSourceServiceFactory> getProcedureSourceServiceFactoryClass() {
		return ScalaProcedureSourceServiceFactory.class;
	}

	@Override
	protected boolean isSupportExceptions() {
		return false;
	}

	@Override
	protected PrimitiveTypes primitives(boolean _boolean, byte _byte, short _short, char _char, int _int, long _long,
			float _float, double _double) {
		return package$.MODULE$.primitives(_boolean, _byte, _short, _char, _int, _long, _float, _double);
	}

	@Override
	protected void primitives(ProcedureBuilder builder) {
		builder.setProcedure(package$.class, "primitives");
	}

	@Override
	protected ObjectTypes objects(String string, JavaObject object, int[] primitiveArray, JavaObject[] objectArray) {
		return package$.MODULE$.objects(string, object, primitiveArray, objectArray);
	}

	@Override
	protected void objects(ProcedureBuilder builder) {
		builder.setProcedure(package$.class, "objects");
	}

	@Override
	protected CollectionTypes collections(List<Integer> list, Set<Character> set, Map<String, JavaObject> map) {
		return package$.MODULE$.collections(list, set, map);
	}

	@Override
	protected void collections(ProcedureBuilder builder) {
		builder.setProcedure(package$.class, "collections");
	}

	@Override
	protected VariableTypes variables(char val, In<String> in, Out<JavaObject> out, Var<Integer> var) {
		return package$.MODULE$.variables(val, in, out, var);
	}

	@Override
	protected void variables(ProcedureBuilder builder) {
		builder.setProcedure(package$.class, "variables");
	}

	@Override
	protected ParameterTypes parameter(String parameter) {
		return package$.MODULE$.parameter(parameter);
	}

	@Override
	protected void parameter(ProcedureBuilder builder) {
		builder.setProcedure(package$.class, "parameter");
	}

	@Override
	protected void web(String pathParameter, String queryParameter, String headerParameter, String cookieParameter,
			MockHttpParameters httpParameters, MockHttpObject httpObject, ObjectResponse<WebTypes> response) {
		package$.MODULE$.web(pathParameter, queryParameter, headerParameter, cookieParameter, httpParameters,
				httpObject, response);
	}

	@Override
	protected void web(ProcedureBuilder builder) {
		builder.setProcedure(package$.class, "web");
	}

	@Override
	protected void httpException() throws Exception {
		package$.MODULE$.httpException();
	}

	@Override
	protected void httpException(ProcedureBuilder builder) {
		builder.setProcedure(package$.class, "httpException");
	}

	@Override
	protected void flow(ProcedureBuilder builder) {
		builder.setProcedure(package$.class, "serviceFlow");
	}

	@Override
	protected void asynchronousFlow(AsynchronousFlow flowOne, AsynchronousFlow flowTwo) {
		package$.MODULE$.asynchronousFlow(flowOne, flowTwo);
	}

	@Override
	protected void asynchronousFlow(ProcedureBuilder builder) {
		builder.setProcedure(package$.class, "asynchronousFlow");
	}
}