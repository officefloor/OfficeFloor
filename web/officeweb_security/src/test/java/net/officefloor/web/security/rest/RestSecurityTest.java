package net.officefloor.web.security.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.officefloor.activity.compose.build.ComposeArchitect;
import net.officefloor.activity.compose.build.ComposeEmployer;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.Closure;
import net.officefloor.server.http.HttpClientTestUtil;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpRequestBuilder;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.ObjectResponse;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.compile.WebCompileOfficeFloor;
import net.officefloor.web.json.JacksonHttpObjectResponderFactory;
import net.officefloor.web.rest.build.RestArchitect;
import net.officefloor.web.rest.build.RestEmployer;
import net.officefloor.web.security.HttpSecurityArchitectTest;
import net.officefloor.web.security.build.HttpSecurityArchitect;
import net.officefloor.web.security.build.HttpSecurityArchitectEmployer;
import net.officefloor.web.security.build.HttpSecurityBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public class RestSecurityTest {

    private static final String AUTHENTICATED_ONLY = "_authenticated";

    @Test
    public void notSecured() {
        this.doTest("/open", (assertion) -> {
            assertion.check("Should not be secured", 200);
            assertion.check("Should ignore authentication", 200, "ignored");
        });
    }

    @Test
    public void endpointAnyRole() {
        this.doTest("/endpoint-any-role", (assertion) -> {
            assertion.check("Must be authenticated", 401);
            assertion.check("Incorrect authentication", 403, "incorrect");
            assertion.check("Access", 200, "endpoint");
        });
    }

    @Test
    public void methodAnyRole() {
        this.doTest("/method-any-role", (assertion) -> {
            assertion.check("Must be authenticated", 401);
            assertion.check("Incorrect authentication", 403, "incorrect");
            assertion.check("Access", 200, "method");
        });
    }

    @Test
    public void methodAnyRoleOverrideEndpointAnyRole() {
        this.doTest("/method-override-endpoint-any-role", (assertion) -> {
            assertion.check("Must be authenticated", 401);
            assertion.check("Endpoint any-role no longer applies", 403, "endpoint");
            assertion.check("Only method any-role applies", 200, "method");
        });
    }

    @Test
    public void inheritEndpointAnyRole() {
        this.doTest("/endpoint-any-role/access", (assertion) -> {
            assertion.check("Must be authenticated", 401);
            assertion.check("Incorrect authentication", 403, "incorrect");
            assertion.check("Access", 200, "endpoint");
        });
    }

    @Test
    public void overrideAnyRoleForOnlyAuthentication() {
        this.doTest("/endpoint-any-role/authenticated", (assertion) -> {
            assertion.check("Must be authenticated", 401);
            assertion.check("No any roles, so just authenticated", 200, AUTHENTICATED_ONLY);
        });
    }

    @Test
    public void overrideEndpointAnyRole() {
        this.doTest("/endpoint-any-role/endpoint-override", (assertion) -> {
            assertion.check("Must be authenticated", 401);
            assertion.check("Parent any-role no longer applies", 403, "endpoint");
            assertion.check("Only child any-role applies", 200, "endpoint-override");
        });
    }

    @Test
    public void overrideMethodAnyRole() {
        this.doTest("/endpoint-any-role/method-override", (assertion) -> {
            assertion.check("Must be authenticated", 401);
            assertion.check("Parent any-role no longer applies", 403, "endpoint");
            assertion.check("Only method any-role applies", 200, "method");
        });
    }

    @Test
    public void inheritOverrideEndpointAnyRole() {
        this.doTest("/endpoint-any-role/endpoint-override/access", (assertion) -> {
            assertion.check("Must be authenticated", 401);
            assertion.check("Inherit any-role", 200, "endpoint-override");
        });
    }

    @Test
    public void methodAnyRoleNotInherited() {
        this.doTest("/endpoint-any-role/method-override/access", (assertion) -> {
            assertion.check("Must be authenticated", 401);
            assertion.check("Method specific security not inherited", 403, "method");
            assertion.check("Parent endpoint security is inherited", 200, "endpoint");
        });
    }

    @Test
    public void endpointAllRoles() {
        this.doTest("/endpoint-all-roles", (assertion) -> {
            assertion.check("Must be authenticated", 401);
            assertion.check("Only one of the roles", 403, "endpoint-one");
            assertion.check("All roles", 200, "endpoint-one", "endpoint-two");
        });
    }

    @Test
    public void methodAllRoles() {
        this.doTest("/method-all-roles", (assertion) -> {
            assertion.check("Must be authenticated", 401);
            assertion.check("Only one of the roles", 403, "method-one");
            assertion.check("All roles", 200, "method-one", "method-two");
        });
    }

    @Test
    public void inheritEndpointAllRoles() {
        this.doTest("/endpoint-all-roles/access", (assertion) -> {
            assertion.check("Must be authenticated", 401);
            assertion.check("Incorrect authentication", 403, "endpoint-one");
            assertion.check("Access", 200, "endpoint-one", "endpoint-two");
        });
    }

    @Test
    public void overrideAllRolesForOnlyAuthentication() {
        this.doTest("/endpoint-all-roles/authenticated", (assertion) -> {
            assertion.check("Must be authenticated", 401);
            assertion.check("Not inherit all-roles, so just authenticated", 200, AUTHENTICATED_ONLY);
        });
    }

    @Test
    public void accumulateEndpointAllRoles() {
        this.doTest("/endpoint-all-roles/endpoint-accumulate", (assertion) -> {
            assertion.check("Must be authenticated", 401);
            assertion.check("Parent all-roles requires further roles", 403, "endpoint-one", "endpoint-two");
            assertion.check("Additional role", 200, "endpoint-one", "endpoint-two", "endpoint-accumulate");
        });
    }

    @Test
    public void accumulateMethodAllRoles() {
        this.doTest("/endpoint-all-roles/method-accumulate", (assertion) -> {
            assertion.check("Must be authenticated", 401);
            assertion.check("Parent all-roles reuqires further roles", 403, "endpoint-one", "endpoint-two");
            assertion.check("Additional role", 200, "endpoint-one", "endpoint-two", "method-accumulate");
        });
    }

    @Test
    public void inheritAccumulateEndpointAllRoles() {
        this.doTest("/endpoint-all-roles/endpoint-accumulate/access", (assertion) -> {
            assertion.check("Must be authenticated", 401);
            assertion.check("Inherit all-roles", 200, "endpoint-one", "endpoint-two", "endpoint-accumulate");
        });
    }

    @Test
    public void methodAllRolesAccumulates() {
        this.doTest("/endpoint-any-role/method-accumulate/access", (assertion) -> {
            assertion.check("Must be authenticated", 401);
            assertion.check("Method specific security not inherited", 200, "endpoint-one", "endpoint-two");
        });
    }

    public static class SecuredService {
        public void service(ObjectResponse<String> response) {
            response.send("SECURED");
        }
    }

    @FunctionalInterface
    protected interface TestLogic {
        void test(TestAssertion assertion);
    }

    protected interface TestAssertion {
        void check(String message, int expectedStatus, String... roles);
    }

    protected void doTest(String path, TestLogic logic) {
        Closure<MockHttpServer> server = new Closure<>();
        WebCompileOfficeFloor compile = new WebCompileOfficeFloor();
        compile.web((context) -> {

            // Employ architects
            WebArchitect web = context.getWebArchitect();
            OfficeArchitect office = context.getOfficeArchitect();
            OfficeSourceContext sourceContext = context.getOfficeSourceContext();
            ComposeArchitect compose = ComposeEmployer.employComposeArchitect(office, sourceContext);
            RestArchitect rest = RestEmployer.employRestArchitect(office, web, compose, sourceContext);
            HttpSecurityArchitect security = HttpSecurityArchitectEmployer.employHttpSecurityArchitect(web, compose, office, sourceContext);

            // Configure responder
            web.addHttpObjectResponder(new JacksonHttpObjectResponderFactory(new ObjectMapper()));

            // Create the properties
            PropertyList properties = sourceContext.createPropertyList();
            properties.addProperty("TestClass").setValue(this.getClass().getName());

            // Load the security
            Map<String, HttpSecurityBuilder> securities = security.addHttpSecurities("officefloor/rest-security", properties);
            assertNotNull(securities.get("challenge"), "Should have HTTP Security");

            // Inform web architect first so default HttpAccessControl is available for explicit linking
            security.informWebArchitect();

            // Add the security decoration
            rest.addRestMethodDecorator(new HttpSecurityRestMethodDecorator(securities));

            // Load the REST
            rest.addRestServices(false, "officefloor/rest", properties);
        });
        compile.mockHttpServer((mockServer) -> server.value = mockServer);
        try (OfficeFloor officeFloor = compile.compileAndOpenOfficeFloor()) {
            try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient()) {
                logic.test((message, expectedStatus, roles) -> {

                    // Undertake request and confirm appropriate secure status
                    MockHttpRequestBuilder request = MockHttpServer.mockRequest(path);
                    if (roles.length > 0) {
                        String user = roles[0]; // Consider first role the user's name
                        request.header("Authorization", "Mock " + user + (AUTHENTICATED_ONLY.equals(user) ? "" : ("," + String.join(",", roles))));
                    }
                    MockHttpResponse response = server.value.send(request);
                    assertEquals(expectedStatus, response.getStatus().getStatusCode(), message);
                });
            }
        } catch (Exception ex) {
            fail(ex);
        }
    }

}
