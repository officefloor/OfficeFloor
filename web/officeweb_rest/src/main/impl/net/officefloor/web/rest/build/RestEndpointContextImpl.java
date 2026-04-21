package net.officefloor.web.rest.build;

import net.officefloor.server.http.HttpMethod;

/**
 * {@link RestEndpointContext} implementation.
 */
public class RestEndpointContextImpl implements RestEndpointContext {

    private boolean isSecure;

    private final String path;

    private RestConfiguration configuration = null;

    public RestEndpointContextImpl(boolean isSecure, String path) {
        this.isSecure = isSecure;
        this.path = path;
    }

    public void addConfiguration(RestConfiguration configuration) {
        this.configuration = configuration;
    }

    /*
     * ================= RestEndpointContext ==================
     */

    @Override
    public boolean isSecure() {
        return this.isSecure;
    }

    @Override
    public void setSecure(boolean isSecure) {
        this.isSecure = isSecure;
    }

    @Override
    public String getPath() {
        return this.path;
    }

}
