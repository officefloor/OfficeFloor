package net.officefloor.spring.starter.rest;

import net.officefloor.compile.spi.officefloor.ExternalServiceInput;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.spring.starter.rest.cors.ComposeCorsConfiguration;
import org.springframework.web.cors.CorsConfiguration;

/**
 * REST endpoint for handling by {@link net.officefloor.frame.api.manage.OfficeFloor}.
 */
public class OfficeFloorRestEndpoint {

    /**
     * {@link HttpMethod} for endpoint.
     */
    private final HttpMethod method;

    /**
     * Path for endpoint.
     */
    private final String path;

    /**
     * {@link ExternalServiceInput} for direct invocation handling of REST endpoint.
     */
    private final ExternalServiceInput externalServiceInput;

    /**
     * {@link CorsConfiguration}.
     */
    private final CorsConfiguration corsConfiguration;

    /**
     * Instantiate.
     *
     * @param method               {@link HttpMethod} for endpoint.
     * @param path                 Path for endpoint.
     * @param externalServiceInput {@link ExternalServiceInput} for direct invocation handling of REST endpoint.
     * @param corsConfiguration    {@link CorsConfiguration}.
     */
    public OfficeFloorRestEndpoint(HttpMethod method, String path, ExternalServiceInput externalServiceInput,
                                   CorsConfiguration corsConfiguration) {
        this.method = method;
        this.path = path;
        this.externalServiceInput = externalServiceInput;
        this.corsConfiguration = corsConfiguration;
    }

    /**
     * Obtains the {@link HttpMethod}.
     *
     * @return {@link HttpMethod}.
     */
    public HttpMethod getHttpMethod() {
        return this.method;
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
     * Obtains the {@link ExternalServiceInput} to service the end point.
     *
     * @return {@link ExternalServiceInput}.
     */
    public ExternalServiceInput getExternalServiceInput() {
        return this.externalServiceInput;
    }

    /**
     * Obtains the {@link CorsConfiguration}.
     *
     * @return {@link CorsConfiguration} or <code>null</code> if no CORS.
     */
    public CorsConfiguration getCorsConfiguration() {
        return this.corsConfiguration;
    }
}
