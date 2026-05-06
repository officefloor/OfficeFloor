package net.officefloor.web.rest.build;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.server.http.HttpMethod;

/**
 * Builds servicing of REST requests.
 */
public interface RestArchitect {

    /**
     * Determines if REST endpoints are configured.
     *
     * @param resourceDirectory Directory containing the REST configuration.
     * @return <code>true</code> if REST endpoint configuration available.
     * @throws Exception If fails to check for REST configuration files.
     */
    boolean isRestAvailable(String resourceDirectory) throws Exception;

    /**
     * Adds servicing of a {@link RestMethod}.
     *
     * @param isSecure              Indicates whether request must be over HTTPS.
     * @param method                {@link HttpMethod}.
     * @param restPath              REST path.
     * @param compositionLocation   Location of composition to handle the {@link RestMethod}.
     * @param properties            {@link PropertyList} to configure servicing.
     * @param endpointConfiguration {@link RestConfiguration} for the {@link RestEndpoint}.
     * @return {@link RestEndpoint}.
     * @throws Exception If fails to load {@link RestEndpoint}.
     */
    RestEndpoint addRestService(boolean isSecure, HttpMethod method, String restPath,
                                String compositionLocation, PropertyList properties,
                                RestConfiguration endpointConfiguration) throws Exception;

    /**
     * Adds all REST services.
     *
     * @param isSecure          Indicates if must be over HTTPS.
     * @param resourceDirectory Directory containing the REST configuration.
     * @param properties        {@link PropertyList} to configure servicing.
     * @param listener          {@link RestListener}.
     * @throws Exception If fails to load {@link RestEndpoint} instances.
     */
    void addRestServices(boolean isSecure, String resourceDirectory, PropertyList properties,
                         RestListener listener) throws Exception;

}
