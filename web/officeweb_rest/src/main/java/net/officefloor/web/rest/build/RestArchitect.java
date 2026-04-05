package net.officefloor.web.rest.build;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.ExecutionExplorer;
import net.officefloor.server.http.HttpMethod;

/**
 * Builds servicing of REST requests.
 */
public interface RestArchitect {

    /**
     * Adds servicing of a REST {@link net.officefloor.server.http.HttpRequest}.
     *
     * @param isSecure            Indicates if must be over HTTPS.
     * @param method              {@link HttpMethod}.
     * @param restPath            REST path.
     * @param compositionLocation Location of composition to handle the REST {@link net.officefloor.server.http.HttpRequest}.
     * @param properties          {@link PropertyList} to configure servicing.
     * @return {@link RestEndpoint}.
     * @throws Exception If fails to load {@link RestEndpoint}.
     */
    RestEndpoint addRestService(boolean isSecure, HttpMethod method, String restPath,
                                String compositionLocation, PropertyList properties) throws Exception;

    /**
     * Adds all REST services.
     *
     * @param isSecure          Indicates if must be over HTTPS.
     * @param resourceDirectory Directory containing the REST configuration.
     * @param properties        {@link PropertyList} to configure servicing.
     * @param listener          {@link RestEndpointListener}.
     * @throws Exception If fails to load {@link RestEndpoint} instances.
     */
    void addRestServices(boolean isSecure, String resourceDirectory, PropertyList properties,
                         RestEndpointListener listener) throws Exception;

}
