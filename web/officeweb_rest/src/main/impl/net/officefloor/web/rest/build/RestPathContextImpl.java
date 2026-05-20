package net.officefloor.web.rest.build;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.web.build.WebArchitect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * {@link RestPathContext} implementation.
 */
public class RestPathContextImpl implements RestPathContext {

    private final String path;

    private final RestPathContext parentPath;

    private RestConfiguration configuration = null;

    private final Map<String, RestPathContextImpl> childPathSegments = new HashMap<>();

    private final List<RestMethodContextImpl<?>> methods = new LinkedList<>();

    public RestPathContextImpl(String path, RestPathContext parentPath) {
        this.path = path;
        this.parentPath = parentPath;
    }

    public RestPathContextImpl getRestPathContext(String path) {

        // Split into path segments
        String[] pathSegments = path.split("/");

        // Create the path from this path segment
        RestPathContextImpl endpoint = this;
        String currentPath = this.path;
        NEXT_PATH_SEGMENT: for (String pathSegment : pathSegments) {

            // Ignore empty path segments
            if (pathSegment.isEmpty()) {
                continue NEXT_PATH_SEGMENT;
            }

            // Update current path
            if (!currentPath.endsWith("/")) {
                currentPath += "/";
            }
            currentPath += pathSegment;

            // Obtain child path segment
            final String finalCurrentPath = currentPath;
            final RestPathContextImpl finalEndpoint = endpoint;
            endpoint = endpoint.childPathSegments.computeIfAbsent(
                    pathSegment, (key) -> new RestPathContextImpl(finalCurrentPath, finalEndpoint));
        }

        // Return the endpoint
        return endpoint;
    }

    public void addConfiguration(RestConfiguration configuration) {
        this.configuration = configuration;
    }

    public void addRestMethod(RestMethodContextImpl<?> method) {
        this.methods.add(method);
    }

    public boolean hasRestMethods() {
        return !this.methods.isEmpty();
    }

    public void decorateRestMethods(List<RestMethodDecorator<?>> decorators) {
        this.visitEndpoints(this, (path) -> {
            path.methods.forEach((restMethod) -> restMethod.decorateRestMethod(decorators));
        });
    }

    public void loadRestEndpoints(Map<String, RestEndpoint> endpoints, WebArchitect webArchitect, OfficeArchitect officeArchitect, OfficeSourceContext sourceContext) {
        this.visitEndpoints(this, (path) -> {

            // Determine if have REST methods
            if (!path.hasRestMethods()) {
                return; // no REST methods
            }

            // Obtain the path
            String restPath = path.getPath();

            // Build the REST endpoint
            RestEndpoint endpoint = path.buildRestEndpoint(webArchitect, officeArchitect, sourceContext);

            // Include the REST endpoint
            endpoints.put(restPath, endpoint);
        });
    }

    protected void visitEndpoints(RestPathContextImpl path, Consumer<RestPathContextImpl> visitor) {

        // Visit the current path
        visitor.accept(path);

        // Visit the children
        path.childPathSegments.values().forEach((childPath) -> this.visitEndpoints(childPath, visitor));
    }

    public RestEndpoint buildRestEndpoint(WebArchitect webArchitect, OfficeArchitect officeArchitect, OfficeSourceContext sourceContext) {

        // Build the methods
        List<RestMethod> restMethods = new ArrayList<>(this.methods.size());
        for (RestMethodContextImpl<?> method : this.methods) {
            restMethods.add(method.buildRestMethod(webArchitect, officeArchitect, sourceContext));
        }

        // Create the Rest endpoint
        RestEndpointImpl restEndpoint = new RestEndpointImpl(this.path, restMethods);

        // Return the Rest endpoint
        return restEndpoint;
    }

    /*
     * ================= RestPathContext ==================
     */

    @Override
    public String getPath() {
        return this.path;
    }

    @Override
    public RestPathContext getParentPath() {
        return this.parentPath;
    }

    @Override
    public <T> T getConfiguration(String itemName, Class<T> type) {
        return (this.configuration != null) ? this.configuration.getConfiguration(itemName, type) : null;
    }

}
