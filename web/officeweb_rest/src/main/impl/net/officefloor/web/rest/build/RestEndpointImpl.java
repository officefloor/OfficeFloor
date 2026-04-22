package net.officefloor.web.rest.build;

import java.util.List;

/**
 * {@link RestEndpoint} implementation.
 */
public class RestEndpointImpl implements RestEndpoint {

    private final String path;

    private final RestConfiguration configuration;

    private final List<RestMethod> restMethods;

    /**
     * Instantiate the {@link RestEndpoint}.
     *
     * @param path          Path for the {@link RestEndpoint}.
     * @param configuration Generic {@link RestConfiguration} for this
     *                      {@link RestEndpoint} that can apply to all {@link RestMethod} instances.
     * @param restMethods {@link RestMethod} instances for this {@link RestEndpoint}.
     */
    public RestEndpointImpl(String path, RestConfiguration configuration, List<RestMethod> restMethods) {
        this.path = path;
        this.configuration = configuration;
        this.restMethods = restMethods;
    }

    /*
     * ================== RestEndpoint ===================
     */

    @Override
    public String getPath() {
        return this.path;
    }

    @Override
    public List<RestMethod> getRestMethods() {
        return this.restMethods;
    }

    @Override
    public <T> T getConfiguration(String itemName, Class<T> type) {
        return (this.configuration == null) ? null : this.configuration.getConfiguration(itemName, type);
    }
}
