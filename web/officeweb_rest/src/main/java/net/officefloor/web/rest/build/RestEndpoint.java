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

    /**
     * <p>
     * Obtains additional configuration for the {@link RestEndpoint}.
     * <p>
     * This for example is CORS configuration for all {@link RestMethod} instances of this {@link RestEndpoint}.
     *
     * @param itemName Name of configuration item.
     * @param type     Type of configuration.
     * @param <T>      Type of configuration.
     * @return Configuration item.
     */
    <T> T getConfiguration(String itemName, Class<T> type);

}
