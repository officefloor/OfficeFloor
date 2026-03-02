package net.officefloor.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.officefloor.activity.compose.build.ComposeArchitect;
import net.officefloor.activity.compose.build.ComposeEmployer;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.officefloor.ExternalServiceInput;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.Closure;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.impl.ProcessAwareServerHttpConnectionManagedObject;
import net.officefloor.server.http.mock.MockHttpServer;
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
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        Map<String, ExternalServiceInput<ServerHttpConnection, ProcessAwareServerHttpConnectionManagedObject>> externalServiceInputs = new HashMap<>();
        WebCompileOfficeFloor compiler = new WebCompileOfficeFloor();
        Closure<DeployedOffice> deployedOffice = new Closure<>();
        compiler.officeFloor((context) -> {
            deployedOffice.value = context.getDeployedOffice();
        });
        compiler.web((context) -> {

            // Employ the architects
            OfficeArchitect officeArchitect = context.getOfficeArchitect();
            OfficeSourceContext officeSourceContext = context.getOfficeSourceContext();
            ComposeArchitect<OfficeSection> composeArchitect = ComposeEmployer.employComposeArchitect(officeArchitect, officeSourceContext);
            WebArchitect webArchitect = context.getWebArchitect();
            RestArchitect restArchitect = RestEmployer.employRestArchitect(officeArchitect, webArchitect, composeArchitect, officeSourceContext);

            // Configure object response
            webArchitect.addHttpObjectResponder(new JacksonHttpObjectResponderFactory(new ObjectMapper()));

            // Add the rest servicing
            PropertyList properties = officeSourceContext.createPropertyList();
            properties.addProperty("TestClass").setValue(this.getClass().getName());

            // Load the rest end points
            restArchitect.addRestServices(false, "officefloor/rest", properties, new RestEndpointListener() {
                @Override
                public void endpoint(RestEndpoint endpoint) {

                    // Obtain the deployed office input
                    String sectionName = endpoint.getServiceInput().getOfficeSection().getOfficeSectionName();
                    String sectionInputName = endpoint.getServiceInput().getOfficeSectionInputName();
                    DeployedOfficeInput input = deployedOffice.value.getDeployedOfficeInput(sectionName, sectionInputName);

                    // Register by qualifier
                    String qualifier = endpoint.getHttpMethod().getName() + "_" + endpoint.getPath();
                    ExternalServiceInput<ServerHttpConnection, ProcessAwareServerHttpConnectionManagedObject> externalServiceInput = input.addExternalServiceInput(ServerHttpConnection.class, ProcessAwareServerHttpConnectionManagedObject.class, ProcessAwareServerHttpConnectionManagedObject.getCleanupEscalationHandler());
                    externalServiceInputs.put(qualifier, externalServiceInput);
                }
            });
        });
        Closure<MockHttpServer> server = new Closure<>();
        compiler.mockHttpServer((mockHttpServer) -> server.value = mockHttpServer);
        try (OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor()) {


        }
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

    public void doTest(BiConsumer<RestArchitect, PropertyList> setup, Consumer<MockHttpServer> test) throws Exception {
        WebCompileOfficeFloor compiler = new WebCompileOfficeFloor();
        compiler.web((context) -> {

            // Employ the architects
            OfficeArchitect officeArchitect = context.getOfficeArchitect();
            OfficeSourceContext officeSourceContext = context.getOfficeSourceContext();
            ComposeArchitect<OfficeSection> composeArchitect = ComposeEmployer.employComposeArchitect(officeArchitect, officeSourceContext);
            WebArchitect webArchitect = context.getWebArchitect();
            RestArchitect restArchitect = RestEmployer.employRestArchitect(officeArchitect, webArchitect, composeArchitect, officeSourceContext);

            // Configure object response
            webArchitect.addHttpObjectResponder(new JacksonHttpObjectResponderFactory(new ObjectMapper()));

            // Add the rest servicing
            PropertyList properties = officeSourceContext.createPropertyList();
            properties.addProperty("TestClass").setValue(this.getClass().getName());
            setup.accept(restArchitect, properties);
        });
        Closure<MockHttpServer> server = new Closure<>();
        compiler.mockHttpServer((mockHttpServer) -> server.value = mockHttpServer);
        try (OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor()) {
            test.accept(server.value);
        }
    }

}
