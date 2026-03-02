package net.officefloor.web.rest.build;

import net.officefloor.server.http.HttpMethod;

/**
 * {@link RestEndpointContext} implementation.
 */
public class RestEndpointContextImpl implements RestEndpointContext {

    private boolean isSecure;

    private final HttpMethod method;

    private final String path;

    public RestEndpointContextImpl(boolean isSecure, HttpMethod method, String path) {
        this.isSecure = isSecure;
        this.method = method;
        this.path = path;
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
    public HttpMethod getHttpMethod() {
        return this.method;
    }

    @Override
    public String getPath() {
        return this.path;
    }

}
