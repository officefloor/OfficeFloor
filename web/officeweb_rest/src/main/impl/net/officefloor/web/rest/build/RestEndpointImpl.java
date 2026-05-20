package net.officefloor.web.rest.build;

import java.util.List;

/**
 * {@link RestEndpoint} implementation.
 */
public class RestEndpointImpl implements RestEndpoint {

    private final String path;

    private final List<RestMethod> restMethods;

    /**
     * Instantiate the {@link RestEndpoint}.
     *
     * @param path          Path for the {@link RestEndpoint}.
     * @param restMethods {@link RestMethod} instances for this {@link RestEndpoint}.
     */
    public RestEndpointImpl(String path, List<RestMethod> restMethods) {
        this.path = path;
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

}
