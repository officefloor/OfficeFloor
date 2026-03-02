package net.officefloor.web.rest.build;

import net.officefloor.server.http.HttpMethod;
import net.officefloor.web.build.HttpInput;

/**
 * Configured REST endpoint.
 */
public interface RestEndpoint {

    /**
     * Indicates if the REST endpoint is secure (e.g. HTTPS).
     *
     * @return <code>true</code> if REST endpoint is secure.
     */
    boolean isSecure();

    /**
     * Obtains the {@link HttpMethod} for the REST endpoint.
     *
     * @return {@link HttpMethod} for the REST endpoint.
     */
    HttpMethod getHttpMethod();

    /**
     * Obtains the path for the REST endpoint.
     *
     * @return Path for the REST endpoint.
     */
    String getPath();

    /**
     * Obtains the {@link HttpInput} for the REST endpoint.
     *
     * @return {@link HttpInput} for the REST endpoint.
     */
    HttpInput getInput();

}
