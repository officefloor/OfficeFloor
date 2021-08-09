/*-
 * #%L
 * Scala
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

package net.officefloor.scala;

import com.fasterxml.jackson.module.scala.DefaultScalaModule;
import net.officefloor.activity.impl.procedure.ClassProcedureSource;
import net.officefloor.activity.procedure.ProcedureLoaderUtil;
import net.officefloor.activity.procedure.build.ProcedureArchitect;
import net.officefloor.activity.procedure.build.ProcedureEmployer;
import net.officefloor.activity.procedure.spi.ProcedureSourceServiceFactory;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.test.Closure;
import net.officefloor.plugin.variable.In;
import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Var;
import net.officefloor.polyglot.test.*;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.ObjectResponse;
import net.officefloor.web.compile.WebCompileOfficeFloor;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
     * Ensure list {@link net.officefloor.activity.procedure.Procedure} instances from Scala package.
     */
    public void testListPackageProcedures() {
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
     * Ensure list {@link net.officefloor.activity.procedure.Procedure} instances from Scala object.
     */
    public void testListObjectProcedures() {
        ProcedureLoaderUtil.validateProcedures(ScalaObject.class,
				ProcedureLoaderUtil.procedure("method"),
				ProcedureLoaderUtil.procedure("procedure"));
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
            OfficeSection procedure = procedureArchitect.addProcedure("service", ScalaRequestService.class.getName(),
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
