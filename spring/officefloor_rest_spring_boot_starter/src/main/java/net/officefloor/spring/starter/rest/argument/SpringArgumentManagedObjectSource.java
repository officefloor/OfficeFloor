package net.officefloor.spring.starter.rest.argument;

import jakarta.servlet.http.HttpServletRequest;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.spring.starter.rest.SpringServerHttpConnection;
import net.officefloor.spring.starter.rest.ModelAndViewBridge;
import net.officefloor.web.build.HttpValueLocation;
import net.officefloor.web.state.HttpRequestState;
import org.springframework.core.MethodParameter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link net.officefloor.frame.api.managedobject.source.ManagedObjectSource} for a Spring argument.
 */
public class SpringArgumentManagedObjectSource extends AbstractManagedObjectSource<SpringArgumentManagedObjectSource.DependencyKeys, None> {

    /**
     * Dependency keys.
     */
    public static enum DependencyKeys {
        SERVER_HTTP_CONNECTION,
        HTTP_REQUEST_STATE
    }

    private final Class<?> objectType;

    private final Method method;

    private final int parameterIndex;

    /**
     * Instantiate.
     *
     * @param objectType     Object type.
     * @param method         {@link Method}.
     * @param parameterIndex Index of parameter on the {@link Method}.
     */
    public SpringArgumentManagedObjectSource(Class<?> objectType, Method method, int parameterIndex) {
        this.objectType = objectType;
        this.method = method;
        this.parameterIndex = parameterIndex;
    }


    /*
     * ==================== ManagedObjectSource =================
     */

    @Override
    protected void loadSpecification(SpecificationContext context) {
        // No specification
    }

    @Override
    protected void loadMetaData(MetaDataContext<DependencyKeys, None> context) throws Exception {
        context.setObjectClass(this.objectType);
        context.setManagedObjectClass(SpringArgumentManagedObject.class);
        context.addDependency(DependencyKeys.SERVER_HTTP_CONNECTION, ServerHttpConnection.class);
        context.addDependency(DependencyKeys.HTTP_REQUEST_STATE, HttpRequestState.class);
    }

    @Override
    protected ManagedObject getManagedObject() throws Throwable {
        return new SpringArgumentManagedObject();
    }

    /**
     * {@link ManagedObject} for the Spring argument.
     */
    public class SpringArgumentManagedObject implements CoordinatingManagedObject<DependencyKeys> {

        private SpringServerHttpConnection connection;

        private HttpRequestState requestState;

        @Override
        public void loadObjects(ObjectRegistry<DependencyKeys> registry) throws Throwable {
            this.connection = (SpringServerHttpConnection) registry.getObject(DependencyKeys.SERVER_HTTP_CONNECTION);
            this.requestState = (HttpRequestState) registry.getObject(DependencyKeys.HTTP_REQUEST_STATE);
        }

        @Override
        public Object getObject() throws Throwable {

            // Obtain the request (to load argument resolution state)
            HttpServletRequest request = this.connection.getHttpServletRequest();

            // Load the Spring path variables
            Map<String, String> pathVariables = new HashMap<>();
            this.requestState.loadValues((name, value, location) -> {
                if (location == HttpValueLocation.PATH) {
                    pathVariables.put(name, value);
                }
            });
            request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, pathVariables);

            // Create the method parameter
            MethodParameter methodParameter = new MethodParameter(
                    SpringArgumentManagedObjectSource.this.method, SpringArgumentManagedObjectSource.this.parameterIndex);

            // Obtain the model and view bridge
            ModelAndViewBridge bridge = this.connection.getModelAndViewBridge(SpringArgumentManagedObjectSource.this.method);

            // Determine the resolver to handler the method argument
            RequestMappingHandlerAdapter adapter = this.connection.getRequestMappingHandlerAdapter();
            for (HandlerMethodArgumentResolver resolver : adapter.getArgumentResolvers()) {
                if (resolver.supportsParameter(methodParameter)) {

                    // Have resolver for parameter
                    return resolver.resolveArgument(methodParameter, bridge.getModelAndViewContainer(),
                            bridge.getNativeWebRequest(), bridge.getWebDataBinderFactory());
                }
            }

            // As here no resolution
            throw new IllegalStateException("Spring unable to resolve parameter " + parameterIndex + " on method " + method.getName());
        }
    }

}
