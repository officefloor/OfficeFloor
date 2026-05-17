package net.officefloor.woof;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.server.http.HttpClientTestUtil;
import net.officefloor.web.ObjectResponse;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests HTTP security composition loading in WoOF.
 */
public class WoofCompositionSecurityTest {

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
        try (OfficeFloor officeFloor = WoofLoaderSettings.contextualLoad((context) -> {
            context.setWoofPath("non-existent.woof");
            context.addOverrideProperty("TestClass", WoofCompositionSecurityTest.class.getName());
            context.addOverrideProperty(WoofLoaderOfficeExtensionService.SECURITY_DIRECTORY_PROPERTY, "officefloor/security-test/security");
            context.addOverrideProperty(WoofLoaderOfficeExtensionService.REST_DIRECTORY_PROPERTY, "officefloor/security-test/rest");
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
