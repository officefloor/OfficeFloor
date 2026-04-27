package net.officefloor.web.rest.build;

import net.officefloor.server.http.HttpMethod;

/**
 * Context for the {@link RestMethod}.
 */
public interface RestMethodContext extends RestEndpointContext {

    /**
     * Obtains the {@link HttpMethod}.
     *
     * @return {@link HttpMethod}.
     */
    HttpMethod getHttpMethod();

}
