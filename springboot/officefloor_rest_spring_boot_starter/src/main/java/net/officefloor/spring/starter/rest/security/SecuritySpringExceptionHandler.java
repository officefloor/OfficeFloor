package net.officefloor.spring.starter.rest.security;

import jakarta.servlet.ServletException;
import net.officefloor.spring.starter.rest.SpringServerHttpConnection;
import net.officefloor.spring.starter.rest.response.SpringExceptionHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.ExceptionTranslationFilter;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

/**
 * Spring Security {@link SpringExceptionHandler}.
 */
public class SecuritySpringExceptionHandler implements SpringExceptionHandler {

    /*
     * ====================== SpringExceptionHandler =====================
     */

    @Override
    public boolean handle(Throwable exception, SpringServerHttpConnection connection) throws Exception {

        // From the security filters find the exception translation
        Map<String, SecurityFilterChain> securityFilters = connection.getApplicationContext().getBeansOfType(SecurityFilterChain.class);
        Optional<ExceptionTranslationFilter> exceptionTranslationFilter = securityFilters.values().stream()
                .filter((securityFilter) -> securityFilter.matches(connection.getHttpServletRequest()))
                .flatMap((securityFilter) -> securityFilter.getFilters().stream())
                .filter(f -> f instanceof ExceptionTranslationFilter)
                .map(f -> (ExceptionTranslationFilter) f)
                .findFirst();

        // Determine if found exception translation filter
        if (exceptionTranslationFilter.isEmpty()) {
            return false; // not handled
        }

        // Undertake filter (handling security exception)
        exceptionTranslationFilter.get().doFilter(connection.getHttpServletRequest(), connection.getHttpServletResponse(), (request, response) -> {
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
