package net.officefloor.web.rest.build;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.web.build.WebArchitect;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * {@link RestEndpointContext} implementation.
 */
public class RestEndpointContextImpl implements RestEndpointContext {

    private boolean isSecure;

    private final String path;

    private RestConfiguration configuration = null;

    private final List<RestMethodContextImpl> methods = new LinkedList<>();

    public RestEndpointContextImpl(boolean isSecure, String path) {
        this.isSecure = isSecure;
        this.path = path;
    }

    public void addConfiguration(RestConfiguration configuration) {
        this.configuration = configuration;
    }

    public void addRestMethod(RestMethodContextImpl method) {
        this.methods.add(method);
    }

    public RestEndpoint buildRestEndpoint(WebArchitect webArchitect, OfficeArchitect officeArchitect) {

        // Build the methods
        List<RestMethod> restMethods = new ArrayList<>(this.methods.size());
        for (RestMethodContextImpl method : this.methods) {
            restMethods.add(method.buildRestMethod(webArchitect, officeArchitect));
        }

        // Create the Rest endpoint
        RestEndpointImpl restEndpoint = new RestEndpointImpl(this.path, this.configuration, restMethods);

        // Return the Rest endpoint
        return restEndpoint;
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
