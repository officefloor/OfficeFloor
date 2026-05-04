package net.officefloor.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.officefloor.activity.compose.build.ComposeArchitect;
import net.officefloor.activity.compose.build.ComposeEmployer;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.officefloor.ExternalServiceInput;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.Closure;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.Next;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.impl.ProcessAwareServerHttpConnectionManagedObject;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.ObjectResponse;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.compile.WebCompileOfficeFloor;
import net.officefloor.web.json.JacksonHttpObjectResponderFactory;
import net.officefloor.web.rest.build.RestArchitect;
import net.officefloor.web.rest.build.RestConfiguration;
import net.officefloor.web.rest.build.RestEmployer;
import net.officefloor.web.rest.build.RestEndpoint;
import net.officefloor.web.rest.build.RestEndpointContext;
import net.officefloor.web.rest.build.RestListener;
import net.officefloor.web.rest.build.RestMethod;
import net.officefloor.web.rest.build.RestMethodContext;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
        this.doTest(HttpMethod.GET, "/path", "officefloor/rest/path.GET.yaml", this.validatePathGet());
    }

    private Consumer<MockHttpServer> validatePathGet() {
        return (server) -> server.send(MockHttpServer.mockRequest("/path")).assertJson(200, "GET");
    }

    @Test
    public void pathParameterGet() throws Exception {
        this.doTest(HttpMethod.GET, "/{id}", "officefloor/rest/{id}.GET.yaml", this.validatePathParameterGet());
    }

    private Consumer<MockHttpServer> validatePathParameterGet() {
        return (server) -> server.send(MockHttpServer.mockRequest("/1")).assertJson(200, "1");
    }

    public static class PathParameterProcedure {
        public void service(@HttpPathParameter("id") String id, ObjectResponse<String> response) {
            response.send(id);
        }
    }

    @Test
    public void queryParameterGet() throws Exception {
        this.doTest(HttpMethod.GET, "/query", "officefloor/rest/query.GET.yaml", this.validateQueryParameterGet());
    }

    private Consumer<MockHttpServer> validateQueryParameterGet() {
        return (server) -> server.send(MockHttpServer.mockRequest("/query?name=value")).assertJson(200, "value");
    }

    public static class QueryParameterProcedure {
        public void service(@HttpQueryParameter("name") String name, ObjectResponse<String> response) {
            response.send(name);
        }
    }

    @Test
    public void additionalEndpointConfiguration() throws Exception {
        this.doTest((restArchitect, properties) -> {

            RestEndpoint endpoint = restArchitect.addRestService(false, HttpMethod.GET,
                    "/additionalConfiguration", "officefloor/rest/additionalConfiguration.GET.yaml",
                    properties, new RestConfiguration() {
                        @Override
                        public <T> T getConfiguration(String itemName, Class<T> type) {
                            assertEquals("test", itemName, "Incorrect configuration item");
                            return (T) new TestConfiguration("Matt", 48);
                        }
                    });

            this.validateAdditionalConfigurationEndPoint().accept(endpoint);

        }, this.validateAdditionalConfiguration());
    }

    protected Consumer<RestEndpoint> validateAdditionalConfigurationEndPoint() {
        return (endpoint) -> {
            TestConfiguration configuration = endpoint.getConfiguration("test", TestConfiguration.class);
            assertEquals("Matt", configuration.getName(), "Incorrect name");
            assertEquals(48, configuration.getAge(), "Incorrect age");
        };
    }

    @Test
    public void additionalMethodConfiguration() throws Exception {
        this.doTest((restArchitect, properties) -> {

            RestEndpoint endpoint = restArchitect.addRestService(false, HttpMethod.GET,
                    "/additionalConfiguration", "officefloor/rest/additionalConfiguration.GET.yaml",
                    properties, null);

            this.validateAdditionalConfigurationMethod().accept(endpoint);

        }, this.validateAdditionalConfiguration());
    }

    protected Consumer<RestEndpoint> validateAdditionalConfigurationMethod() {
        return (endpoint) -> {
            // Ensure correct method
            RestMethod method = endpoint.getRestMethods().get(0);
            assertEquals(HttpMethod.GET, method.getHttpMethod(), "Incorrect method");

            // Verify configuration for the REST method
            TestConfiguration configuration = method.getConfiguration("test", TestConfiguration.class);
            assertEquals("Daniel", configuration.getName(), "Incorrect name");
            assertEquals(47, configuration.getAge(), "Incorrect age");
        };
    }

    private Consumer<MockHttpServer> validateAdditionalConfiguration() {
        return (server) -> server.send(MockHttpServer.mockRequest("/additionalConfiguration")).assertJson(200, "configuration");
    }

    public static class AdditionalConfigurationProcedure {
        public void service(ObjectResponse<String> response) {
            response.send("configuration");
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestConfiguration {
        private String name;
        private int age;
    }

    @Test
    public void loadAll() throws Exception {
        Consumer<RestEndpoint>[] additionalConfigurationValidations = new Consumer[] {
            this.validateAdditionalConfigurationEndPoint(), this.validateAdditionalConfigurationMethod()
        };
        Consumer<MockHttpServer>[] executionValidations = new Consumer[] {
                this.validateRootGet(), this.validatePathGet(), this.validatePathParameterGet(),
                this.validateQueryParameterGet(), this.validateAdditionalConfiguration()
        };
        List<RestEndpoint> endpoints = new ArrayList<>();
        this.doTest((restArchitect, properties) -> {

            // Add all REST endpoints
            restArchitect.addRestServices(false, "officefloor/rest", properties, endpoints::add);

            // Ensure all end points registered
            assertEquals(executionValidations.length, endpoints.size(), "Incorrect number of endpoints");

            // Obtain the additional configuration end point
            List<RestEndpoint> filteredEndpoints = endpoints.stream()
                    .filter((endpoint) -> "/additionalConfiguration".equals(endpoint.getPath()))
                    .toList();
            assertEquals(1, filteredEndpoints.size(), "Should just be the one additional configuration endpoint");
            RestEndpoint additionalConfigurationEndpoint = filteredEndpoints.get(0);

            // Validate the additional configuration end point
            for (Consumer<RestEndpoint> validation : additionalConfigurationValidations) {
                validation.accept(additionalConfigurationEndpoint);
            }

        }, (server) -> {
            for (Consumer<MockHttpServer> validation : executionValidations) {
                validation.accept(server);
            }
        });
    }

    @Test
    public void overrideEndpointSecure() throws Exception {
        this.doTest(((restArchitect, properties) -> {

            // Add all services
            List<RestEndpoint> endpoints = new ArrayList<>();
            restArchitect.addRestServices(false, "officefloor/rest", properties, new RestListener() {

                @Override
                public void initialiseRestEndpoint(RestEndpointContext context) {
                    context.setSecure(true);
                }

                @Override
                public void endpoint(RestEndpoint endpoint) {
                    endpoints.add(endpoint);
                }
            });

            // Ensure all end points registered
            for (RestEndpoint endpoint : endpoints) {
                for (RestMethod method : endpoint.getRestMethods()) {
                    assertTrue(method.isSecure(), "Should make method secure for " + method.getHttpMethod().getName() + " " + endpoint.getPath());
                }
            }

        }), (server) -> {
            server.send(MockHttpServer.mockRequest("/").secure(true)).assertJson(200, "GET");
        });
    }

    @Test
    public void overrideMethodSecure() throws Exception {
        this.doTest(((restArchitect, properties) -> {

            // Add all services
            List<RestEndpoint> endpoints = new ArrayList<>();
            restArchitect.addRestServices(false, "officefloor/rest", properties, new RestListener() {

                @Override
                public void initialiseRestMethod(RestMethodContext context) {
                    if (HttpMethod.POST.isEqual(context.getHttpMethod())) {
                        context.setSecure(true);
                    }
                }

                @Override
                public void endpoint(RestEndpoint endpoint) {
                    endpoints.add(endpoint);
                }
            });

            // Ensure all end points registered
            for (RestEndpoint endpoint : endpoints) {
                for (RestMethod method : endpoint.getRestMethods()) {
                    boolean isExpectSecure = HttpMethod.POST.isEqual(method.getHttpMethod());
                    assertEquals(isExpectSecure, method.isSecure(), "Incorrect secure for " + method.getHttpMethod().getName() + " " + endpoint.getPath());
                }
            }

        }), (server) -> {
            server.send(MockHttpServer.mockRequest("/").secure(true)).assertJson(200, "GET");
        });
    }

    @Test
    public void directInvocation() throws Exception {

        // Compile capturing the external service inputs
        Map<String, ExternalServiceInput<ServerHttpConnection, ProcessAwareServerHttpConnectionManagedObject<ByteBuffer>>> externalServiceInputs = new HashMap<>();

        this.doTest(((restArchitect, properties) -> {

            // Load the rest end points
            restArchitect.addRestServices(false, "officefloor/rest", properties, new RestListener() {
                @Override
                public void endpoint(RestEndpoint endpoint) {
                    for (RestMethod method : endpoint.getRestMethods()) {

                        // Register by qualifier
                        String qualifier = method.getHttpMethod().getName() + "_" + endpoint.getPath();
                        externalServiceInputs.put(qualifier, MockHttpServer.getExternalServiceInput(method.getHttpInput().getDirect()));
                    }
                }
            });

        }), (server) -> {
            this.assertDirectInvocation(HttpMethod.GET, "/", "/", "GET",  server, externalServiceInputs);
            this.assertDirectInvocation(HttpMethod.GET, "/path", "/path", "GET",  server, externalServiceInputs);
            this.assertDirectInvocation(HttpMethod.GET, "/{id}", "/1", "1",  server, externalServiceInputs);
            this.assertDirectInvocation(HttpMethod.GET, "/query", "/query?name=value", "value", server, externalServiceInputs);
            this.assertDirectInvocation(HttpMethod.GET, "/additionalConfiguration", "/additionalConfiguration", "configuration", server, externalServiceInputs);
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

    @Test
    public void httpInputLinker() throws Exception {
        this.doTest((restArchitect, properties) -> {

            restArchitect.addRestServices(false, "officefloor/rest", properties, new RestListener() {

                @Override
                public void initialiseRestMethod(RestMethodContext context) {
                    if (HttpMethod.GET.isEqual(context.getHttpMethod()) && "/".equals(context.getPath())) {
                        context.setHttpInputLinker(linkerContext -> {

                            // Create section to intercept
                            OfficeArchitect officeArchitect = linkerContext.getOfficeArchitect();
                            OfficeSection intercept = officeArchitect.addOfficeSection("INTERCEPT",
                                    new ClassSectionSource(), InterceptProcedure.class.getName());

                            // Link section in for intercepting
                            officeArchitect.link(linkerContext.getHttpInput().getInput(),
                                    intercept.getOfficeSectionInput("intercept"));
                            officeArchitect.link(intercept.getOfficeSectionOutput("proceed"),
                                    linkerContext.getServiceInput());
                        });
                    }
                }

                @Override
                public void endpoint(RestEndpoint endpoint) {
                }
            });

        }, server -> {
            // Root GET is intercepted: header added and body still returned
            MockHttpResponse rootResponse = server.send(MockHttpServer.mockRequest("/"));
            rootResponse.assertJson(200, "GET");
            rootResponse.assertHeader("X-Intercepted", "true");

            // Path GET is not intercepted: no header
            MockHttpResponse pathResponse = server.send(MockHttpServer.mockRequest("/path"));
            pathResponse.assertJson(200, "GET");
            assertNull(pathResponse.getHeader("X-Intercepted"), "Path GET should not be intercepted");
        });
    }

    public static class InterceptProcedure {
        @Next("proceed")
        public void intercept(ServerHttpConnection connection) throws Exception {
            connection.getResponse().getHeaders().addHeader("X-Intercepted", "true");
        }
    }

    public void doTest(HttpMethod method, String restPath, String composeLocation, Consumer<MockHttpServer> test) throws Exception {
        this.doTest((restArchitect, properties) -> {

            RestEndpoint endpoint = restArchitect.addRestService(false, method, restPath, composeLocation, properties, null);

            // Validate the end point
            assertEquals(restPath, endpoint.getPath(), "Incorrect path");

            // Ensure only the one method
            List<RestMethod> restMethods = endpoint.getRestMethods();
            assertEquals(1, restMethods.size(), "Should only be one method on end point");
            RestMethod restMethod = restMethods.get(0);

            assertFalse(restMethod.isSecure(), "Should not be secure");
            assertEquals(method, restMethod.getHttpMethod(), "Incorrect HTTP method");
            assertNotNull(restMethod.getHttpInput(), "Must have HTTP Input");
            assertNotNull(restMethod.getServiceInput(), "Must have service input");
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
