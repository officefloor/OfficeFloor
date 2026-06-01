package net.officefloor.web.thymeleaf;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.officefloor.activity.compose.build.ComposeArchitect;
import net.officefloor.activity.compose.build.ComposeEmployer;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.Closure;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.compile.WebCompileOfficeFloor;
import net.officefloor.web.rest.build.RestArchitect;
import net.officefloor.web.rest.build.RestEmployer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ThymeleafTest {

    /**
     * Render step only — no service function before it.
     * The template has no model expressions so the absence of a model variable is fine.
     */
    @Test
    public void staticRender() throws Exception {
        this.doTest("officefloor/rest/static.GET.yml", server ->
                server.send(MockHttpServer.mockRequest("/"))
                        .assertResponse(200, "<p>Hello Static</p>\n"));
    }

    /**
     * Service function returns a model POJO; Thymeleaf renders it via {@code ${model.message}}.
     */
    @Test
    public void modelRender() throws Exception {
        this.doTest("officefloor/rest/model.GET.yml", server ->
                server.send(MockHttpServer.mockRequest("/"))
                        .assertResponse(200, "<p>Hello Model</p>\n"));
    }

    public static class ModelService {
        public TemplateModel service() {
            return new TemplateModel("Hello Model");
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TemplateModel {
        private String message;
    }

    /**
     * HTTP query parameter flows through the service function into the template model.
     */
    @Test
    public void queryParameterRender() throws Exception {
        this.doTest("officefloor/rest/query.GET.yml", server ->
                server.send(MockHttpServer.mockRequest("/?name=World"))
                        .assertResponse(200, "<p>World</p>\n"));
    }

    public static class QueryService {
        public TemplateModel service(@HttpQueryParameter("name") String name) {
            return new TemplateModel(name);
        }
    }

    /**
     * Response Content-Type must be {@code text/html}.
     */
    @Test
    public void htmlContentType() throws Exception {
        this.doTest("officefloor/rest/static.GET.yml", server -> {
            var response = server.send(MockHttpServer.mockRequest("/"));
            assertEquals(200, response.getStatus().getStatusCode(), "Expected HTTP 200");
            var contentTypeHeader = response.getHeader("Content-Type");
            String contentType = contentTypeHeader != null ? contentTypeHeader.getValue() : null;
            assertTrue(contentType != null && contentType.startsWith("text/html"),
                    "Expected text/html Content-Type but got: " + contentType);
        });
    }

    private void doTest(String restYamlResource, ThrowingConsumer<MockHttpServer> test) throws Exception {
        WebCompileOfficeFloor compiler = new WebCompileOfficeFloor();
        compiler.web(context -> {
            OfficeArchitect officeArchitect = context.getOfficeArchitect();
            OfficeSourceContext officeSourceContext = context.getOfficeSourceContext();
            ComposeArchitect composeArchitect = ComposeEmployer.employComposeArchitect(officeArchitect, officeSourceContext);
            WebArchitect webArchitect = context.getWebArchitect();
            RestArchitect restArchitect = RestEmployer.employRestArchitect(officeArchitect, webArchitect, composeArchitect, officeSourceContext);

            PropertyList properties = officeSourceContext.createPropertyList();
            properties.addProperty("TestClass").setValue(ThymeleafTest.class.getName());

            restArchitect.addRestService(false, HttpMethod.GET, "/", restYamlResource, properties, null);
        });

        Closure<MockHttpServer> server = new Closure<>();
        compiler.mockHttpServer(mockHttpServer -> server.value = mockHttpServer);
        try (OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor()) {
            test.accept(server.value);
        }
    }

    @FunctionalInterface
    private interface ThrowingConsumer<T> {
        void accept(T t) throws Exception;
    }

}
