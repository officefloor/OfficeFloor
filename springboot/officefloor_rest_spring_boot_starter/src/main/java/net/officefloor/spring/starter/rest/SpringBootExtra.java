/*-
 * #%L
 * OfficeFloor REST Spring Boot Starter
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

package net.officefloor.spring.starter.rest;

import net.officefloor.frame.api.source.SourceContext;

/**
 * <p>
 * Provides means to load services from other Spring boot starters (other than Spring MVC).
 * <p>
 * This allows users to selectively use what they require.
 */
public class SpringBootExtra {

    /**
     * Loads the Service.
     *
     * @param extraClassName   {@link Class} that will exist if the Spring Boot extra is on the class path.
     * @param serviceClassName {@link Class} of service.
     * @param sourceContext    {@link SourceContext}.
     * @param defaultService   Default service. May be <code>null</code>.
     * @param <S>              Service type.
     * @return Service.
     * @throws Exception If fails to load service.
     */
    public static <S> S loadService(String extraClassName, String serviceClassName, SourceContext sourceContext, S defaultService) throws Exception {

        // Determine if the Spring Boot extra is on the class path
        Class<?> extraClass = sourceContext.loadOptionalClass(extraClassName);
        if (extraClass == null) {

            // Spring boot not included, so service unavailable (use default)
            return defaultService;
        }

        // Load the service
        Class<?> serviceClass = sourceContext.loadClass(serviceClassName);
        Object service = serviceClass.getConstructor().newInstance();
        return (S) service;
    }

}
