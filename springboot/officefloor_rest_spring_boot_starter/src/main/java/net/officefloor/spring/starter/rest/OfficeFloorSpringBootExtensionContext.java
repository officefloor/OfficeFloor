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

import net.officefloor.activity.compose.build.ComposeArchitect;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.web.rest.build.RestArchitect;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Context for the {@link OfficeFloorSpringBootExtension}.
 */
public interface OfficeFloorSpringBootExtensionContext {

    /**
     * Obtains the {@link OfficeArchitect}.
     *
     * @return {@link OfficeArchitect}.
     */
    OfficeArchitect getOfficeArchitect();

    /**
     * Obtains the {@link OfficeSourceContext}.
     *
     * @return {@link OfficeSourceContext}.
     */
    OfficeSourceContext getOfficeSourceContext();

    /**
     * Obtains the {@link ComposeArchitect}.
     *
     * @return {@link ComposeArchitect}.
     */
    ComposeArchitect getComposeArchitect();

    /**
     * Obtains the {@link RestArchitect}.
     *
     * @return {@link RestArchitect}.
     */
    RestArchitect getRestArchitect();

    /**
     * Obtains the {@link ConfigurableApplicationContext}.
     *
     * @return {@link ConfigurableApplicationContext}.
     */
    ConfigurableApplicationContext getApplicationContext();

}
