package net.officefloor.spring.starter.rest;

import net.officefloor.compile.spi.officefloor.ExternalServiceInput;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.spring.starter.rest.cors.ComposeCorsConfiguration;
import net.officefloor.web.rest.build.RestEndpoint;
import net.officefloor.web.rest.build.RestMethod;
import org.springframework.web.cors.CorsConfiguration;

import java.util.LinkedList;
import java.util.List;

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
    private final CorsConfiguration corsConfiguration;

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
        }

        // Default allowed methods
        if (corsConfiguration != null) {
            corsConfiguration.setAllowedMethods(allowedMethods);
        }

        // Capture the CORS configuration
        this.corsConfiguration = corsConfiguration;
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
