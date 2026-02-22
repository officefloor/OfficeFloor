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
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.ObjectResponse;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.compile.WebCompileOfficeFloor;
import net.officefloor.web.json.JacksonHttpObjectResponderFactory;
import net.officefloor.web.rest.build.RestArchitect;
import net.officefloor.web.rest.build.RestEmployer;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

public class RestTest {

    @Test
    public void rootGet() throws Exception {
        this.doTest(HttpMethod.GET, "/", "officefloor/rest/index.GET.yaml", (server) -> {
            MockHttpResponse response = server.send(MockHttpServer.mockRequest("/"));
            response.assertJson(200, "GET");
        });
    }

    public static class GetProcedure {
        public void service(ObjectResponse<String> response) {
            response.send("GET");
        }
    }

    @Test
    public void pathGet() throws Exception {
        this.doTest(HttpMethod.GET, "path", "officefloor/rest/path.GET.yaml", (server) -> {
            MockHttpResponse response = server.send(MockHttpServer.mockRequest("path"));
            response.assertJson(200, "GET");
        });
    }

    @Test
    public void pathParameterGet() throws Exception {
        this.doTest(HttpMethod.GET, "{id}", "officefloor/rest/{id}.GET.yaml", (server) -> {
            MockHttpResponse response = server.send(MockHttpServer.mockRequest("1"));
            response.assertJson(200, "1");
        });
    }

    public static class PathParameterProcedure {
        public void service(@HttpPathParameter("id") String id, ObjectResponse<String> response) {
            response.send(id);
        }
    }

    @Test
    public void queryParameterGet() throws Exception {
        this.doTest(HttpMethod.GET, "query", "officefloor/rest/query.GET.yaml", (server) -> {
            MockHttpResponse response = server.send(MockHttpServer.mockRequest("query?name=value"));
            response.assertJson(200, "value");
        });
    }

    public static class QueryParameterProcedure {
        public void service(@HttpQueryParameter("name") String name, ObjectResponse<String> response) {
            response.send(name);
        }
    }

    public void doTest(HttpMethod method, String restPath, String composeLocation, Consumer<MockHttpServer> test) throws Exception {
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
            restArchitect.addRestService(false, method, restPath, composeLocation, properties);
        });
        Closure<MockHttpServer> server = new Closure<>();
        compiler.mockHttpServer((mockHttpServer) -> server.value = mockHttpServer);
        try (OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor()) {
            test.accept(server.value);
        }
    }

}
