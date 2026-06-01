/*-
 * #%L
 * Web REST
 * %%
 * Copyright (C) 2005 - 2026 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.web.rest.build;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.server.http.HttpMethod;

import java.util.List;
import java.util.Map;

/**
 * Builds servicing of REST requests.
 */
public interface RestArchitect {

    /**
     * Adds a {@link RestMethodDecorator} for the {@link RestMethod} instances.
     *
     * @param <M>       Momento type.
     * @param decorator {@link RestMethodDecorator}.
     * @return {@link MomentoKey} to retrieve possibly set Momento on the {@link RestMethod}.
     */
    <M> MomentoKey<M> addRestMethodDecorator(RestMethodDecorator<M> decorator);

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
     * @return {@link RestEndpoint} instances by their path.
     * @throws Exception If fails to load {@link RestEndpoint} instances.
     */
    Map<String, RestEndpoint> addRestServices(boolean isSecure, String resourceDirectory, PropertyList properties) throws Exception;

}
