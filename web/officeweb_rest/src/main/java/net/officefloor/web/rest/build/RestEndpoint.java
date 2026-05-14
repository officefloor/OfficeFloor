package net.officefloor.web.rest.build;

import java.util.List;

/**
 * Configured REST endpoint.
 */
public interface RestEndpoint {

    /**
     * Obtains the path for the {@link RestEndpoint}.
     *
     * @return Path for the {@link RestEndpoint}.
     */
    String getPath();

    /**
     * Obtains the {@link RestMethod} instances supported by this {@link RestEndpoint}.
     *
     * @return {@link RestMethod} instances supported by this {@link RestEndpoint}.
     */
    List<RestMethod> getRestMethods();

}
