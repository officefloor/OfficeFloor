package net.officefloor.web;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.officefloor.compile.spi.office.OfficeAdministration;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.Closure;
import net.officefloor.plugin.administration.clazz.ClassAdministrationSource;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.build.HttpEscalationResponder;
import net.officefloor.web.build.HttpObjectResponder;
import net.officefloor.web.build.HttpObjectResponderContext;
import net.officefloor.web.build.HttpObjectResponderFactory;
import net.officefloor.web.compile.WebCompileOfficeFloor;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AdministerServerHttpConnectionTest {

    /**
     * Ensure can administer the {@link net.officefloor.server.http.ServerHttpConnection}.
     */
    @Test
    public void administerServerHttpConnection() throws Exception {

        // Ensure clear state
        MockAdministration.connections = null;

        // Compile
        WebCompileOfficeFloor compile = new WebCompileOfficeFloor();
        compile.web((context) -> {

            // Allow responding
            context.getWebArchitect().addHttpObjectResponder(new AdminHttpObjectResponder());

            // Service
            context.link(false, "/", MockServicer.class);

            // Add the administration (allowing to auto wire the connection)
            OfficeArchitect architect = context.getOfficeArchitect();
            OfficeAdministration admin = architect.addOfficeAdministration("ADMIN", ClassAdministrationSource.class.getName());
            admin.addProperty(ClassAdministrationSource.CLASS_NAME_PROPERTY_NAME, MockAdministration.class.getName());
            admin.enableAutoWireExtensions();
            architect.addManagedFunctionAugmentor((augmentContext) -> augmentContext.addPreAdministration(admin));
        });
        Closure<MockHttpServer> server = new Closure<>();
        compile.mockHttpServer((mockHttpServer) -> server.value = mockHttpServer);
        try (OfficeFloor officeFloor = compile.compileAndOpenOfficeFloor()) {
            MockHttpResponse response = server.value.send(MockHttpServer.mockRequest("/").header("accept", "application/json"));
            response.assertResponse(200, "Serviced");
        }

        // Ensure connection is administered
        assertNotNull(MockAdministration.connections, "Should have administration invoked");
        assertEquals(1, MockAdministration.connections.length, "Should have the connection");
        assertNotNull(MockAdministration.connections[0], "Must provide the connection");
    }

    public static class MockServicer {
        public void service(ObjectResponse<String> response) {
            response.send("Serviced");
        }
    }

    public static class MockAdministration {

        private static ServerHttpConnection[] connections = null;

        public void administer(ServerHttpConnection[] connections) {
            MockAdministration.connections = connections;
        }
    }

    public static class AdminHttpObjectResponder implements HttpObjectResponderFactory, HttpObjectResponder<String> {

        @Override
        public String getContentType() {
            return "application/json";
        }

        @Override
        public <T> HttpObjectResponder<T> createHttpObjectResponder(Class<T> objectType) {
            return (HttpObjectResponder<T>) this;
        }

        @Override
        public <E extends Throwable> HttpEscalationResponder<E> createHttpEscalationResponder(Class<E> escalationType, boolean isOfficeFloorEscalation) {
            return null;
        }

        @Override
        public void send(HttpObjectResponderContext<String> context) throws IOException {
            context.getServerHttpConnection().getResponse().getEntityWriter().write(context.getResponseObject());
        }
    }

}
