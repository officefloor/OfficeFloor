/*-
 * #%L
 * Kotlin
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.kotlin;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
 * Tests adapting Kotlin {@link Object} for
 * {@link net.officefloor.activity.procedure.Procedure}.
 *
 * @author Daniel Sagenschneider
 */
public class KotlinProcedureTest extends AbstractPolyglotProcedureTest {

	/**
	 * Ensure no procedures when using non-functions.
	 */
	public void testNonFunctions() {
		ProcedureLoaderUtil.validateProcedures(KotlinObject.class, ProcedureLoaderUtil.procedure("getDependency"),
				ProcedureLoaderUtil.procedure("getValue"));
	}

	/**
	 * Ensure list {@link net.officefloor.activity.procedure.Procedure} instances.
	 */
	public void testListProcedures() {
		ProcedureLoaderUtil.validateProcedures(KotlinFunctionsKt.class,
				ProcedureLoaderUtil.procedure("asynchronousFlow", KotlinProcedureSourceServiceFactory.class),
				ProcedureLoaderUtil.procedure("collections", KotlinProcedureSourceServiceFactory.class),
				ProcedureLoaderUtil.procedure("httpException", KotlinProcedureSourceServiceFactory.class),
				ProcedureLoaderUtil.procedure("objects", KotlinProcedureSourceServiceFactory.class),
				ProcedureLoaderUtil.procedure("parameter", KotlinProcedureSourceServiceFactory.class),
				ProcedureLoaderUtil.procedure("primitives", KotlinProcedureSourceServiceFactory.class),
				ProcedureLoaderUtil.procedure("serviceFlow", KotlinProcedureSourceServiceFactory.class),
				ProcedureLoaderUtil.procedure("variables", KotlinProcedureSourceServiceFactory.class),
				ProcedureLoaderUtil.procedure("web", KotlinProcedureSourceServiceFactory.class));
	}

	/**
	 * Ensure able to send Kotlin data object.
	 */
	public void testWebSendKotlinDataObject() throws Throwable {
		WebCompileOfficeFloor compiler = new WebCompileOfficeFloor();
		Closure<MockHttpServer> server = new Closure<>();
		compiler.mockHttpServer((mockServer) -> server.value = mockServer);
		compiler.web((context) -> {
			OfficeArchitect officeArchitect = context.getOfficeArchitect();
			ProcedureArchitect<OfficeSection> procedureArchitect = ProcedureEmployer
					.employProcedureArchitect(officeArchitect, context.getOfficeSourceContext());
			OfficeSection procedure = procedureArchitect.addProcedure("service", KotlinRequestService.class.getName(),
					ClassProcedureSource.SOURCE_NAME, "service", false, new PropertyListImpl());
			officeArchitect.link(context.getWebArchitect().getHttpInput(false, "/").getInput(),
					procedure.getOfficeSectionInput(ProcedureArchitect.INPUT_NAME));
		});
		this.officeFloor = compiler.compileAndOpenOfficeFloor();
		MockHttpResponse response = server.value
				.send(MockHttpServer.mockRequest().header("Content-Type", "application/json")
						.entity(mapper.writeValueAsString(new KotlinRequest(1, "test"))));
		response.assertResponse(200, mapper.writeValueAsString(new KotlinRequest(2, "Serviced test")));
	}

	public static class KotlinRequestService {
		public void service(KotlinRequest request, ObjectResponse<KotlinRequest> response) {
			response.send(new KotlinRequest(request.getId() + 1, "Serviced " + request.getMessage()));
		}
	}

	/*
	 * ========================= AbstractPolyglotFunctionTest =====================
	 */

	@Override
	protected Class<? extends ProcedureSourceServiceFactory> getProcedureSourceServiceFactoryClass() {
		return KotlinProcedureSourceServiceFactory.class;
	}

	@Override
	protected boolean isSupportExceptions() {
		return false;
	}

	@Override
	protected PrimitiveTypes primitives(boolean _boolean, byte _byte, short _short, char _char, int _int, long _long,
			float _float, double _double) {
		return KotlinFunctionsKt.primitives(_boolean, _byte, _short, _char, _int, _long, _float, _double);
	}

	@Override
	protected void primitives(ProcedureBuilder builder) {
		builder.setProcedure(KotlinFunctionsKt.class, "primitives");
	}

	@Override
	protected ObjectTypes objects(String string, JavaObject object, int[] primitiveArray, JavaObject[] objectArray) {
		return KotlinFunctionsKt.objects(string, object, primitiveArray, objectArray);
	}

	@Override
	protected void objects(ProcedureBuilder builder) {
		builder.setProcedure(KotlinFunctionsKt.class, "objects");
	}

	@Override
	protected CollectionTypes collections(List<Integer> list, Set<Character> set, Map<String, JavaObject> map) {
		return KotlinFunctionsKt.collections(list, set, map);
	}

	@Override
	protected void collections(ProcedureBuilder builder) {
		builder.setProcedure(KotlinFunctionsKt.class, "collections");
	}

	@Override
	protected VariableTypes variables(char val, In<String> in, Out<JavaObject> out, Var<Integer> var) {
		return KotlinFunctionsKt.variables(val, in, out, var);
	}

	@Override
	protected void variables(ProcedureBuilder builder) {
		builder.setProcedure(KotlinFunctionsKt.class, "variables");
	}

	@Override
	protected ParameterTypes parameter(String parameter) {
		return KotlinFunctionsKt.parameter(parameter);
	}

	@Override
	protected void parameter(ProcedureBuilder builder) {
		builder.setProcedure(KotlinFunctionsKt.class, "parameter");
	}

	@Override
	protected void web(String pathParameter, String queryParameter, String headerParameter, String cookieParameter,
			MockHttpParameters httpParameters, MockHttpObject httpObject, ObjectResponse<WebTypes> response) {
		KotlinFunctionsKt.web(pathParameter, queryParameter, headerParameter, cookieParameter, httpParameters,
				httpObject, response);
	}

	@Override
	protected void web(ProcedureBuilder builder) {
		builder.setProcedure(KotlinFunctionsKt.class, "web");
	}

	@Override
	protected void httpException() throws Exception {
		KotlinFunctionsKt.httpException();
	}

	@Override
	protected void httpException(ProcedureBuilder builder) {
		builder.setProcedure(KotlinFunctionsKt.class, "httpException");
	}

	@Override
	protected void flow(ProcedureBuilder builder) {
		builder.setProcedure(KotlinFunctionsKt.class, "serviceFlow");
	}

	@Override
	protected void asynchronousFlow(AsynchronousFlow flowOne, AsynchronousFlow flowTwo) {
		KotlinFunctionsKt.asynchronousFlow(flowOne, flowTwo);
	}

	@Override
	protected void asynchronousFlow(ProcedureBuilder builder) {
		builder.setProcedure(KotlinFunctionsKt.class, "asynchronousFlow");
	}

}
