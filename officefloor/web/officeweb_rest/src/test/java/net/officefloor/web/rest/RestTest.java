package net.officefloor.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.officefloor.activity.compose.build.ComposeArchitect;
import net.officefloor.activity.compose.build.ComposeEmployer;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.Closure;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.ObjectResponse;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.compile.WebCompileOfficeFloor;
import net.officefloor.web.json.JacksonHttpObjectResponderFactory;
import net.officefloor.web.rest.build.RestArchitect;
import net.officefloor.web.rest.build.RestEmployer;
import org.junit.jupiter.api.Test;

public class RestTest {

    @Test
    public void root() throws Exception {
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
            restArchitect.addRestService(false, "/", HttpMethod.GET, "officefloor/rest/index.GET.yaml", properties);
        });
        Closure<MockHttpServer> server = new Closure<>();
        compiler.mockHttpServer((mockHttpServer) -> server.value = mockHttpServer);
        try (OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor()) {

            // Send root request
            MockHttpResponse response = server.value.send(MockHttpServer.mockRequest("/"));
            response.assertJson(200, "ROOT");
        }
    }

    public static class RootGet {
        public void service(ObjectResponse<String> response) {
            response.send("ROOT");
        }
    }

}
