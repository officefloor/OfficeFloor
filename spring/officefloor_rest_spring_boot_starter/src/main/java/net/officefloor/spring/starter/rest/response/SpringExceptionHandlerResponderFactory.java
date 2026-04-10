package net.officefloor.spring.starter.rest.response;

import net.officefloor.server.http.HttpExternalResponse;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.spring.starter.rest.ModelAndViewBridge;
import net.officefloor.spring.starter.rest.SpringServerHttpConnection;
import net.officefloor.web.build.HttpObjectResponder;
import net.officefloor.web.build.HttpObjectResponderFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class SpringExceptionHandlerResponderFactory implements HttpObjectResponderFactory, HttpObjectResponder<Throwable> {

    /**
     * Content-type.
     */
    private final String contentType;

    /**
     * {@link SpringExceptionHandler} instances.
     */
    private final SpringExceptionHandler[] exceptionHandlers;

    /**
     * Initiate.
     *
     * @param contentType       Content-Type.
     * @param exceptionHandlers {@link SpringExceptionHandler} instances.
     */
    public SpringExceptionHandlerResponderFactory(String contentType, SpringExceptionHandler[] exceptionHandlers) {
        this.contentType = contentType;
        this.exceptionHandlers = exceptionHandlers;
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
    public void send(Throwable escalation, ServerHttpConnection connection) throws IOException {

        // Delegate to Spring to handle
        SpringServerHttpConnection springConnection = (SpringServerHttpConnection) connection;
        try {
            try {
                ModelAndViewBridge bridge = springConnection.getRenderModelAndViewBridge();
                bridge.processDispatchResult(null, escalation);
            } catch (Exception ex) {

                // Handle invocation target failure (as reflectively invoked)
                Throwable targetEx = ex;
                if (ex instanceof InvocationTargetException) {
                    targetEx = ((InvocationTargetException) ex).getTargetException();
                }

                // Attempt to handle the exception
                HANDLED: for (SpringExceptionHandler exceptionHandler : this.exceptionHandlers) {
                    try {
                        if (exceptionHandler.handle(targetEx, springConnection)) {
                            // Handled
                            targetEx = null;
                            break HANDLED;
                        }
                    } catch (Throwable handlingFailure) {
                        // Process as if were a filter chain propagating up exception
                        targetEx = handlingFailure;
                    }
                }
                if (targetEx != null) {
                    // Exception not handled, so propagate
                    throw targetEx;
                }
            }

        } catch (Throwable ex) {
            // Propagate failure
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            } else if (ex instanceof Error) {
                throw (Error) ex;
            } else {
                throw new IOException(ex);
            }
        }

        // Flag that externally handled (by Spring)
        HttpExternalResponse.of(connection.getResponse()).externalSend();
    }

}
