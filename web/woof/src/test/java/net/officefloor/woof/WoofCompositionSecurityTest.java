package net.officefloor.woof;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.server.http.HttpClientTestUtil;
import net.officefloor.web.ObjectResponse;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests HTTP security composition loading in WoOF.
 */
public class WoofCompositionSecurityTest {

    // TODO: Move these into WoOF Loader
    static final String YAML_SECURITY_PROPERTY = "officefloor.yaml.security";
    static final String YAML_REST_PROPERTY     = WoofCompositionTest.YAML_REST_PROPERTY;


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
            assertion.check("Inherit any-role", 403, "endpoint-override");
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
        try (OfficeFloor officeFloor = WoofLoaderSettings.contextualLoad((context) -> {
            context.setWoofPath("non-existent.woof");
            context.addOverrideProperty("TestClass", WoofCompositionSecurityTest.class.getName());
            context.addOverrideProperty(YAML_SECURITY_PROPERTY, "officefloor/security-test/security");
            context.addOverrideProperty(YAML_REST_PROPERTY, "officefloor/security-test/rest");
            return WoOF.open();
        })) {
            try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient()) {
                logic.test((message, expectedStatus, roles) -> {

                    // Undertake request and confirm appropriate secure status
                    HttpGet request = new HttpGet("http://localhost:7878" + path);
                    if (roles.length > 0) {
                        String user = roles[0]; // Consider first role the user's name
                        request.addHeader("Authorization", "Mock " + user + (AUTHENTICATED_ONLY.equals(user) ? "" : ("," + String.join(",", roles))));
                    }
                    HttpResponse response = null;
                    try {
                        response = client.execute(request);
                    } catch (Exception ex) {
                        fail(ex);
                    }
                    assertEquals(expectedStatus, response.getStatusLine().getStatusCode(), message);
                });
            }
        } catch (Exception ex) {
            fail(ex);
        }
    }

}
