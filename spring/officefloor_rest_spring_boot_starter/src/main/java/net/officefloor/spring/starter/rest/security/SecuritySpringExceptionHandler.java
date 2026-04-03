package net.officefloor.spring.starter.rest.security;

import jakarta.servlet.ServletException;
import net.officefloor.spring.starter.rest.SpringServerHttpConnection;
import net.officefloor.spring.starter.rest.response.SpringExceptionHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.ExceptionTranslationFilter;

import java.io.IOException;

/**
 * Spring Security {@link SpringExceptionHandler}.
 */
public class SecuritySpringExceptionHandler implements SpringExceptionHandler {

    /*
     * ====================== SpringExceptionHandler =====================
     */

    @Override
    public boolean handle(Throwable exception, SpringServerHttpConnection connection) throws Exception {

        // Obtain the filter
        SecurityFilterChain filterChain = connection.getApplicationContext().getBean(SecurityFilterChain.class);
        ExceptionTranslationFilter exceptionTranslationFilter = filterChain.getFilters().stream().filter(f -> f instanceof ExceptionTranslationFilter)
                .map(f -> (ExceptionTranslationFilter) f)
                .findFirst()
                .orElseThrow();

        // Undertake filter (handling security exception)
        exceptionTranslationFilter.doFilter(connection.getHttpServletRequest(), connection.getHttpServletResponse(), (request, response) -> {
            if (exception instanceof RuntimeException) {
                throw (RuntimeException) exception; // propagates the security exceptions to be handled
            } else if (exception instanceof Error) {
                throw (Error) exception;
            } else if (exception instanceof ServletException) {
                throw (ServletException) exception;
            } else if (exception instanceof IOException) {
                throw (IOException) exception;
            } else {
                // Wrap to propagate
                throw new ServletException(exception);
            }
        });

        // As here handled
        return true;
    }
}
