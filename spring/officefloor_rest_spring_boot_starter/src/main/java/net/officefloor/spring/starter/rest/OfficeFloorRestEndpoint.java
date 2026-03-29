package net.officefloor.spring.starter.rest;

import net.officefloor.compile.spi.officefloor.ExternalServiceInput;
import net.officefloor.server.http.HttpMethod;

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
     * Instantiate.
     *
     * @param method               {@link HttpMethod} for endpoint.
     * @param path                 Path for endpoint.
     * @param externalServiceInput {@link ExternalServiceInput} for direct invocation handling of REST endpoint.
     */
    public OfficeFloorRestEndpoint(HttpMethod method, String path, ExternalServiceInput externalServiceInput) {
        this.method = method;
        this.path = path;
        this.externalServiceInput = externalServiceInput;
    }

    public HttpMethod getHttpMethod() {
        return this.method;
    }

    public String getPath() {
        return this.path;
    }

    public ExternalServiceInput getExternalServiceInput() {
        return this.externalServiceInput;
    }

}
