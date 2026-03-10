package net.officefloor.spring.starter.rest;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.lang.reflect.Method;

/**
 * {@link net.officefloor.frame.api.managedobject.source.ManagedObjectSource} for a Spring argument.
 */
public class SpringArgumentManagedObjectSource<S> extends AbstractSpringManagedObjectSource<S> {

    /**
     * Instantiate.
     *
     * @param method         {@link Method}.
     * @param parameterIndex Index of parameter on the {@link Method}.
     */
    public SpringArgumentManagedObjectSource(Class<S> objectType, Method method, int parameterIndex) {
        super(objectType, (connection) -> {

            // Create the method parameter
            MethodParameter methodParameter = new MethodParameter(method, parameterIndex);

            // Determine the resolver to handler the method argument
            RequestMappingHandlerAdapter adapter = connection.getRequestMappingHandlerAdapter();
            for (HandlerMethodArgumentResolver resolver : adapter.getArgumentResolvers()) {
                if (resolver.supportsParameter(methodParameter)) {

                    // Have resolver, so create details to resolve
                    ModelAndViewContainer mavContainer = new ModelAndViewContainer();
                    NativeWebRequest webRequest = new ServletWebRequest(connection.getHttpServletRequest(), connection.getHttpServletResponse());
                    WebDataBinderFactory binderFactory = null;

                    // Have resolver for parameter
                    return (S) resolver.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory);
                }
            }

            // As here no resolution
            return (S) "TODO handle unknown resolution for Spring";
        });
    }

}
