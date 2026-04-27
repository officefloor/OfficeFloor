package net.officefloor.spring.starter.rest;

import net.officefloor.compile.spi.officefloor.ExternalServiceInput;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.impl.ProcessAwareServerHttpConnectionManagedObject;
import net.officefloor.spring.starter.rest.cors.ComposeCorsConfiguration;
import net.officefloor.web.rest.build.RestMethod;
import org.springframework.web.cors.CorsConfiguration;

/**
 * REST method for handling by {@link net.officefloor.frame.api.manage.OfficeFloor}.
 */
public class OfficeFloorRestMethod {

    /**
     * {@link HttpMethod} for endpoint.
     */
    private final HttpMethod method;

    /**
     * {@link ExternalServiceInput} for direct invocation handling of REST endpoint.
     */
    private final ExternalServiceInput externalServiceInput;

    /**
     * Instantiate.
     *
     * @param method {@link RestMethod}.
     */
    public OfficeFloorRestMethod(RestMethod method) {
        this.method = method.getHttpMethod();

        // Obtain the external service input
        this.externalServiceInput = method.getHttpInput().getDirect()
                .addExternalServiceInput(ServerHttpConnection.class,
                        ProcessAwareServerHttpConnectionManagedObject.class);
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
     * Obtains the {@link ExternalServiceInput} to service the end point.
     *
     * @return {@link ExternalServiceInput}.
     */
    public ExternalServiceInput getExternalServiceInput() {
        return this.externalServiceInput;
    }

}
