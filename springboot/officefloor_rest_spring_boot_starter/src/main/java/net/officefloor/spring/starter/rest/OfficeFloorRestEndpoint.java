package net.officefloor.spring.starter.rest;

import net.officefloor.compile.spi.office.ExecutionManagedFunction;
import net.officefloor.compile.spi.officefloor.ExternalServiceInput;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.spring.starter.rest.cors.ComposeCorsConfiguration;
import net.officefloor.web.rest.build.RestEndpoint;
import net.officefloor.web.rest.build.RestMethod;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * REST endpoint for handling by {@link net.officefloor.frame.api.manage.OfficeFloor}.
 */
public class OfficeFloorRestEndpoint {

    /**
     * Creates the {@link CorsConfiguration} from {@link ComposeCorsConfiguration}.
     *
     * @param configuration {@link ComposeCorsConfiguration}.
     * @return {@link CorsConfiguration}.
     */
    public static CorsConfiguration createCorsConfiguration(ComposeCorsConfiguration configuration) {
        if (configuration == null) {
            return null;
        }
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(configuration.getAllowedOrigins());
        cors.setAllowedOriginPatterns(configuration.getAllowedOriginPatterns());
        cors.setAllowedMethods(configuration.getAllowedMethods());
        cors.setAllowedHeaders(configuration.getAllowedHeaders());
        cors.setExposedHeaders(configuration.getExposedHeaders());
        cors.setAllowCredentials(configuration.getAllowCredentials());
        cors.setAllowPrivateNetwork(configuration.getAllowPrivateNetwork());
        cors.setMaxAge(configuration.getMaxAge());
        return cors;
    }

    /**
     * Creates the {@link CorsConfiguration} from the {@link CrossOrigin} annotation.
     *
     * @param crossOrigin     {@link CrossOrigin} annotation.
     * @param endpointMethods REST methods implemented.
     * @return {@link CorsConfiguration}.
     */
    private static CorsConfiguration createCorsFromAnnotation(CrossOrigin crossOrigin, List<String> endpointMethods) {
        CorsConfiguration cors = new CorsConfiguration();

        String[] origins = crossOrigin.origins();
        if (origins.length > 0) {
            cors.setAllowedOrigins(Arrays.asList(origins));
        }

        String[] originPatterns = crossOrigin.originPatterns();
        if (originPatterns.length > 0) {
            cors.setAllowedOriginPatterns(Arrays.asList(originPatterns));
        }

        RequestMethod[] methods = crossOrigin.methods();
        if (methods.length > 0) {
            cors.setAllowedMethods(Arrays.stream(methods).map(RequestMethod::name).collect(Collectors.toList()));
        } else {
            cors.setAllowedMethods(endpointMethods);
        }

        String[] allowedHeaders = crossOrigin.allowedHeaders();
        if (allowedHeaders.length > 0) {
            cors.setAllowedHeaders(Arrays.asList(allowedHeaders));
        }

        String[] exposedHeaders = crossOrigin.exposedHeaders();
        if (exposedHeaders.length > 0) {
            cors.setExposedHeaders(Arrays.asList(exposedHeaders));
        }

        String allowCredentials = crossOrigin.allowCredentials();
        if (!allowCredentials.isEmpty()) {
            cors.setAllowCredentials(Boolean.parseBoolean(allowCredentials));
        }

        long maxAge = crossOrigin.maxAge();
        if (maxAge >= 0) {
            cors.setMaxAge(maxAge);
        }

        return cors;
    }

    /**
     * Convenience method to combine {@link CorsConfiguration} allowing handling of <code>null</code>.
     *
     * @param corsA {@link CorsConfiguration} A.
     * @param corsB {@link CorsConfiguration} B.
     * @return Combined {@link CorsConfiguration} or <code>null</code> if both <code>null</code>.
     */
    public static CorsConfiguration combineCors(CorsConfiguration corsA, CorsConfiguration corsB) {
        if (corsA != null) {
            if (corsB != null) {
                return corsA.combine(corsB);
            }
            return corsA;
        } else {
            return corsB;
        }
    }

    /**
     * Path for endpoint.
     */
    private final String path;

    /**
     * {@link CorsConfiguration}.
     */
    private CorsConfiguration corsConfiguration;

    /**
     * {@link OfficeFloorRestMethod} instances.
     */
    private final List<OfficeFloorRestMethod> methods = new LinkedList<>();

    /**
     * Instantiate.
     *
     * @param endpoint {@link RestEndpoint}.
     */
    public OfficeFloorRestEndpoint(RestEndpoint endpoint) {

        // Determine the path
        String path = endpoint.getPath();
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        this.path = path;

        // Obtain the possible CORS configuration
        CorsConfiguration corsConfiguration = createCorsConfiguration(endpoint.getConfiguration("cors", ComposeCorsConfiguration.class));

        // Add the REST methods
        List<String> allowedMethods = new LinkedList<>();
        for (RestMethod method : endpoint.getRestMethods()) {

            // Add the REST method
            this.methods.add(new OfficeFloorRestMethod(method));

            // Default to allowed if not configured
            allowedMethods.add(method.getHttpMethod().getName());

            // Obtain the possible CORS configuration
            CorsConfiguration methodCors = OfficeFloorRestEndpoint.createCorsConfiguration(method.getConfiguration("cors", ComposeCorsConfiguration.class));
            corsConfiguration = combineCors(corsConfiguration, methodCors);

            // Explore functions for further information
            method.getServiceInput().addExecutionExplorer((executionContext) ->
                    exploreCrossOrigin(executionContext.getInitialManagedFunction(), new HashSet<>(), allowedMethods));
        }

        // Default allowed methods
        if (corsConfiguration != null) {
            corsConfiguration.setAllowedMethods(allowedMethods);
        }

        // Capture the CORS configuration
        this.corsConfiguration = corsConfiguration;
    }

    /**
     * Recursively explores the {@link ExecutionManagedFunction} tree for {@link CrossOrigin} annotations.
     *
     * @param function {@link ExecutionManagedFunction}.
     * @param visited Already visited {@link ExecutionManagedFunction}.
     * @param endpointMethods REST methods supported.
     */
    private void exploreCrossOrigin(ExecutionManagedFunction function, Set<String> visited, List<String> endpointMethods) {

        // Determine if visited or no function
        if ((function == null) || (!visited.add(function.getManagedFunctionName()))) {
            return;
        }

        // Combine in possible CORS information
        CrossOrigin crossOrigin = function.getManagedFunctionType().getAnnotation(CrossOrigin.class);
        if (crossOrigin != null) {
            this.corsConfiguration = combineCors(this.corsConfiguration, createCorsFromAnnotation(crossOrigin, endpointMethods));
            return;
        }

        // Explore further the tree
        exploreCrossOrigin(function.getNextManagedFunction(), visited, endpointMethods);
        for (var flowType : function.getManagedFunctionType().getFlowTypes()) {
            exploreCrossOrigin(function.getManagedFunction(flowType), visited, endpointMethods);
        }
    }

    /**
     * Obtains the path.
     *
     * @return Path.
     */
    public String getPath() {
        return this.path;
    }

    /**
     * Obtains the {@link CorsConfiguration}.
     *
     * @return {@link CorsConfiguration} or <code>null</code> if no CORS.
     */
    public CorsConfiguration getCorsConfiguration() {
        return this.corsConfiguration;
    }

    /**
     * Obtains the {@link OfficeFloorRestMethod} for this {@link OfficeFloorRestEndpoint}.
     *
     * @return {@link OfficeFloorRestMethod} for this {@link OfficeFloorRestEndpoint}.
     */
    public List<OfficeFloorRestMethod> getRestMethods() {
        return this.methods;
    }

}
