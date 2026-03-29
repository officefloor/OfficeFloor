package net.officefloor.web.rest.build;

import net.officefloor.server.http.HttpMethod;

/**
 * Context for the REST endpoint.
 */
public interface RestEndpointContext {

    /**
     * Indicates if secure (e.g. HTTPS).
     *
     * @return <code>true</code> if secure.
     */
    boolean isSecure();

    /**
     * Allows overriding if secure.
     *
     * @param isSecure Specify if secure.
     */
    void setSecure(boolean isSecure);

    /**
     * Obtains the {@link HttpMethod}.
     *
     * @return {@link HttpMethod}.
     */
    HttpMethod getHttpMethod();

    /**
     * Obtains the path.
     *
     * @return Path.
     */
    String getPath();

}
