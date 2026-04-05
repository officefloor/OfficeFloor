package net.officefloor.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.officefloor.activity.compose.build.ComposeArchitect;
import net.officefloor.activity.compose.build.ComposeEmployer;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.officefloor.ExternalServiceInput;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ContextAwareManagedObject;
import net.officefloor.frame.api.managedobject.InputManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObjectContext;
import net.officefloor.frame.api.managedobject.recycle.CleanupEscalation;
import net.officefloor.frame.test.Closure;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.impl.ProcessAwareServerHttpConnectionManagedObject;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.server.http.mock.MockServerHttpConnection;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.ObjectResponse;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.compile.WebCompileOfficeFloor;
import net.officefloor.web.json.JacksonHttpObjectResponderFactory;
import net.officefloor.web.rest.build.RestArchitect;
import net.officefloor.web.rest.build.RestEmployer;
import net.officefloor.web.rest.build.RestEndpoint;
import net.officefloor.web.rest.build.RestEndpointContext;
import net.officefloor.web.rest.build.RestEndpointListener;
import org.easymock.Mock;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class RestTest {

    @Test
    public void rootGet() throws Exception {
        this.doTest(HttpMethod.GET, "/", "officefloor/rest/index.GET.yaml", this.validateRootGet());
    }

    private Consumer<MockHttpServer> validateRootGet() {
        return (server) -> server.send(MockHttpServer.mockRequest("/")).assertJson(200, "GET");
    }

    public static class GetProcedure {
        public void service(ObjectResponse<String> response) {
            response.send("GET");
        }
    }

    @Test
    public void pathGet() throws Exception {
        this.doTest(HttpMethod.GET, "path", "officefloor/rest/path.GET.yaml", this.validatePathGet());
    }

    private Consumer<MockHttpServer> validatePathGet() {
        return (server) -> server.send(MockHttpServer.mockRequest("path")).assertJson(200, "GET");
    }

    @Test
    public void pathParameterGet() throws Exception {
        this.doTest(HttpMethod.GET, "{id}", "officefloor/rest/{id}.GET.yaml", this.validatePathParameterGet());
    }

    private Consumer<MockHttpServer> validatePathParameterGet() {
        return (server) -> server.send(MockHttpServer.mockRequest("1")).assertJson(200, "1");
    }

    public static class PathParameterProcedure {
        public void service(@HttpPathParameter("id") String id, ObjectResponse<String> response) {
            response.send(id);
        }
    }

    @Test
    public void queryParameterGet() throws Exception {
        this.doTest(HttpMethod.GET, "query", "officefloor/rest/query.GET.yaml", this.validateQueryParameterGet());
    }

    private Consumer<MockHttpServer> validateQueryParameterGet() {
        return (server) -> server.send(MockHttpServer.mockRequest("query?name=value")).assertJson(200, "value");
    }

    public static class QueryParameterProcedure {
        public void service(@HttpQueryParameter("name") String name, ObjectResponse<String> response) {
            response.send(name);
        }
    }

    @Test
    public void loadAll() throws Exception {
        this.doTest(((restArchitect, properties) -> {

            // Add all services
            List<RestEndpoint> endpoints = new ArrayList<>();
            restArchitect.addRestServices(false, "officefloor/rest", properties, new RestEndpointListener() {

                private RestEndpointContext currentContext;

                @Override
                public void initialise(RestEndpointContext context) {
                    this.currentContext = context;
                }

                @Override
                public void endpoint(RestEndpoint endpoint) {

                    // Ensure initialise called first
                    assertNotNull(this.currentContext, "Should have initialise called");
                    assertEquals(this.currentContext.isSecure(), endpoint.isSecure(), "Incorrect secure state");
                    assertEquals(this.currentContext.getHttpMethod(), endpoint.getHttpMethod(), "Incorrect HTTP method");
                    assertEquals(this.currentContext.getPath(), endpoint.getPath(), "Incorrect path");

                    // Add end point
                    endpoints.add(endpoint);
                }
            });

            // Ensure all end points registered
            assertEquals(4, endpoints.size(), "Incorrect number of endpoints");

        }), (server) -> {
            for (Consumer<MockHttpServer> validation : new Consumer[]{
                    this.validateRootGet(), this.validatePathGet(), this.validatePathParameterGet(), this.validateQueryParameterGet()
            }) {
                validation.accept(server);
            }
        });
    }

    @Test
    public void overrideSecure() throws Exception {
        this.doTest(((restArchitect, properties) -> {

            // Add all services
            List<RestEndpoint> endpoints = new ArrayList<>();
            restArchitect.addRestServices(false, "officefloor/rest", properties, new RestEndpointListener() {

                @Override
                public void initialise(RestEndpointContext context) {
                    context.setSecure(true);
                }

                @Override
                public void endpoint(RestEndpoint endpoint) {
                    endpoints.add(endpoint);
                }
            });

            // Ensure all end points registered
            for (RestEndpoint endpoint : endpoints) {
                assertTrue(endpoint.isSecure(), "Should make end point secure for " + endpoint.getHttpMethod().getName() + " " + endpoint.getPath());
            }

        }), (server) -> {

            // Ensure secure
            server.send(MockHttpServer.mockRequest("/").secure(true)).assertJson(200, "GET");
        });
    }

    @Test
    public void directInvocation() throws Exception {

        // Compile capturing the external service inputs
        Map<String, ExternalServiceInput<ServerHttpConnection, ProcessAwareServerHttpConnectionManagedObject<ByteBuffer>>> externalServiceInputs = new HashMap<>();

        this.doTest(((restArchitect, properties) -> {

            // Load the rest end points
            restArchitect.addRestServices(false, "officefloor/rest", properties, new RestEndpointListener() {
                @Override
                public void endpoint(RestEndpoint endpoint) {

                    // Register by qualifier
                    String qualifier = endpoint.getHttpMethod().getName() + "_" + endpoint.getPath();
                    externalServiceInputs.put(qualifier, MockHttpServer.getExternalServiceInput(endpoint.getHttpInput().getDirect()));
                }
            });

        }), (server) -> {
            this.assertDirectInvocation(HttpMethod.GET, "/", "/", "GET",  server, externalServiceInputs);
            this.assertDirectInvocation(HttpMethod.GET, "path", "path", "GET",  server, externalServiceInputs);
            this.assertDirectInvocation(HttpMethod.GET, "{id}", "1", "1",  server, externalServiceInputs);
            this.assertDirectInvocation(HttpMethod.GET, "query", "query?name=value", "value", server, externalServiceInputs);
        });
   }

    private void assertDirectInvocation(HttpMethod method, String restPath, String executePath, String expectedBody, MockHttpServer server, Map<String, ExternalServiceInput<ServerHttpConnection, ProcessAwareServerHttpConnectionManagedObject<ByteBuffer>>> externalServiceInputs) {

        // Obtain the direct invocation
        String qualifier = method.getName() + "_" + restPath;
        ExternalServiceInput<ServerHttpConnection, ProcessAwareServerHttpConnectionManagedObject<ByteBuffer>> externalServiceInput = externalServiceInputs.get(qualifier);
        assertNotNull(externalServiceInput, "External service input " + qualifier + " not found");

        // Undertake request
        MockHttpResponse response = server.direct(MockHttpServer.mockRequest(executePath).method(method), externalServiceInput);
        response.assertJson(200, expectedBody);
    }

    public void doTest(HttpMethod method, String restPath, String composeLocation, Consumer<MockHttpServer> test) throws Exception {
        this.doTest((restArchitect, properties) -> {
            RestEndpoint endpoint = restArchitect.addRestService(false, method, restPath, composeLocation, properties);
            assertFalse(endpoint.isSecure(), "Should not be secure");
            assertEquals(method, endpoint.getHttpMethod(), "Incorrect HTTP method");
            assertEquals(restPath, endpoint.getPath(), "Incorrect path");
            assertNotNull(endpoint.getHttpInput(), "Must have HTTP Input");
            assertNotNull(endpoint.getServiceInput(), "Must have service input");
        }, test);
    }

    protected static interface Setup {
        void setup(RestArchitect architect, PropertyList properties) throws Exception;
    }

    protected void doTest(Setup setup, Consumer<MockHttpServer> test) throws Exception {
        WebCompileOfficeFloor compiler = new WebCompileOfficeFloor();
        compiler.web((context) -> {

            // Employ the architects
            OfficeArchitect officeArchitect = context.getOfficeArchitect();
            OfficeSourceContext officeSourceContext = context.getOfficeSourceContext();
            ComposeArchitect composeArchitect = ComposeEmployer.employComposeArchitect(officeArchitect, officeSourceContext);
            WebArchitect webArchitect = context.getWebArchitect();
            RestArchitect restArchitect = RestEmployer.employRestArchitect(officeArchitect, webArchitect, composeArchitect, officeSourceContext);

            // Configure object response
            webArchitect.addHttpObjectResponder(new JacksonHttpObjectResponderFactory(new ObjectMapper()));

            // Add the rest servicing
            PropertyList properties = officeSourceContext.createPropertyList();
            properties.addProperty("TestClass").setValue(this.getClass().getName());
            setup.setup(restArchitect, properties);
        });
        Closure<MockHttpServer> server = new Closure<>();
        compiler.mockHttpServer((mockHttpServer) -> server.value = mockHttpServer);
        try (OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor()) {
            test.accept(server.value);
        }
    }

}
