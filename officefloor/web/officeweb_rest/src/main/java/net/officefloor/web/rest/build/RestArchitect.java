package net.officefloor.web.rest.build;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.server.http.HttpMethod;

/**
 * Builds servicing of REST requests.
 */
public interface RestArchitect {

    /**
     * Adds servicing of a REST {@link net.officefloor.server.http.HttpRequest}.
     *
     * @param isSecure            Indicates if must be over HTTPS.
     * @param restPath            REST path.
     * @param method              {@link HttpMethod}.
     * @param compositionLocation Location of composition to handle the REST {@link net.officefloor.server.http.HttpRequest}.
     * @param properties          {@link PropertyList} to configure servicing.
     */
    void addRestService(boolean isSecure, String restPath, HttpMethod method, String compositionLocation, PropertyList properties);

}
