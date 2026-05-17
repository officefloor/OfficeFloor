package net.officefloor.web.security.rest;

import net.officefloor.activity.compose.build.ComposeArchitect;
import net.officefloor.activity.compose.build.ComposeEmployer;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.Closure;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.compile.WebCompileOfficeFloor;
import net.officefloor.web.rest.build.RestArchitect;
import net.officefloor.web.rest.build.RestEmployer;
import net.officefloor.web.security.HttpSecurityArchitectTest;
import net.officefloor.web.security.build.HttpSecurityArchitect;
import net.officefloor.web.security.build.HttpSecurityArchitectEmployer;
import net.officefloor.web.security.build.HttpSecurityBuilder;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RestSecurityTest {

    @Test
    public void rest() throws Exception {
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

            // Create the properties
            PropertyList properties = sourceContext.createPropertyList();
            properties.addProperty("TestClass").setValue(HttpSecurityArchitectTest.class.getName());
            properties.addProperty("RestTestClass").setValue(this.getClass().getName());

            // Load the security
            Map<String, HttpSecurityBuilder> securities = security.addHttpSecurities("officefloor/security", properties);
            assertNotNull(securities.get("one"), "Should have HTTP Security");

            // Add the security decoration
            rest.addRestMethodDecorator(new HttpSecurityRestMethodDecorator(securities));

            // Load the REST
            rest.addRestServices(false, "officefloor/rest", properties);

            // Inform
            security.informWebArchitect();
        });
        compile.mockHttpServer((mockServer) -> server.value = mockServer);
        try (OfficeFloor officeFloor = compile.compileAndOpenOfficeFloor()) {

            // Ensure secured
            MockHttpResponse response = server.value.send(MockHttpServer.mockRequest());
            response.assertResponse(401, "");

            // Ensure access
            response = server.value.send(MockHttpServer.mockRequest().header("Authorization", "Bearer TOKEN"));
            response.assertResponse(200, "Access");
        }
    }

    public static class RestService {
        public void service(ServerHttpConnection connection) throws Exception {
            connection.getResponse().getEntityWriter().write("Access");
        }
    }

}
