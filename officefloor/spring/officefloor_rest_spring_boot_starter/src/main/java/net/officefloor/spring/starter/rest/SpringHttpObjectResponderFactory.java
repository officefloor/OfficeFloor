package net.officefloor.spring.starter.rest;

import net.officefloor.server.http.HttpExternalResponse;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.build.HttpObjectResponder;
import net.officefloor.web.build.HttpObjectResponderFactory;
import org.springframework.web.servlet.DispatcherServlet;

import java.io.IOException;

public class SpringHttpObjectResponderFactory implements HttpObjectResponderFactory, HttpObjectResponder<Throwable> {

    /**
     * Content-type.
     */
    private final String contentType;

    /**
     * Initaite.
     *
     * @param contentType Content-Type.
     */
    public SpringHttpObjectResponderFactory(String contentType) {
        this.contentType = contentType;
    }

    /*
     * ===================== HttpObjectResponderFactory ================
     */

    @Override
    public String getContentType() {
        return this.contentType;
    }

    @Override
    public <T> HttpObjectResponder<T> createHttpObjectResponder(Class<T> objectType) {
        return null; // only delegating to Spring for exception handling
    }

    @Override
    public <E extends Throwable> HttpObjectResponder<E> createHttpEscalationResponder(Class<E> escalationType) {
        return (HttpObjectResponder<E>) this;
    }

    /*
     * ====================== HttpObjectResponder ========================
     */

    @Override
    public Class<Throwable> getObjectType() {
        return Throwable.class;
    }

    @Override
    public void send(Throwable escalation, ServerHttpConnection connection) throws IOException {

        // Delegate to Spring to handle
        SpringServerHttpConnection springConnection = (SpringServerHttpConnection) connection;
        try {
            springConnection.processDispatchResult(null, escalation);
        } catch (Exception ex) {
            throw new IOException(ex);
        }

        // Flag that externally handled (by Spring)
        HttpExternalResponse.of(connection.getResponse()).externalSend();
    }

}
