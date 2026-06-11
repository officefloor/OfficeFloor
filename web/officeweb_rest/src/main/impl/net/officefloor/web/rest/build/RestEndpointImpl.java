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
