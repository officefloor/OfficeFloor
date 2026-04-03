package net.officefloor.spring.starter.rest.response;

import net.officefloor.spring.starter.rest.SpringServerHttpConnection;

/**
 * <p>
 * Handler for exceptions from {@link org.springframework.web.servlet.DispatcherServlet}.
 * <p>
 * Typically this is for {@link jakarta.servlet.FilterChain} handling.
 */
public interface SpringExceptionHandler {

    /**
     * Handles the {@link Exception} from {@link org.springframework.web.servlet.DispatcherServlet}.
     *
     * @param exception  {@link Throwable} from {@link org.springframework.web.servlet.DispatcherServlet}.
     * @param connection {@link SpringServerHttpConnection}.
     * @return <code>true</code> if handled.
     * @throws Exception If fails to handle.
     */
    boolean handle(Throwable exception, SpringServerHttpConnection connection) throws Exception;
}
