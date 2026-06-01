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

import net.officefloor.frame.api.source.ServiceContext;

/**
 * Convenience {@link OfficeFloorSpringBootExtensionServiceFactory} that handles extra Spring Boots that may not be on the class path.
 */
public abstract class AbstractExtraSpringBootExtensionServiceFactory implements OfficeFloorSpringBootExtensionServiceFactory, OfficeFloorSpringBootExtension {

    /**
     * Specifies the {@link Class} that would be on path if extra Spring Boot included.
     *
     * @return {@link Class} name.
     */
    public abstract String getExtraKeyClassName();

    /**
     * Name of the {@link OfficeFloorSpringBootExtension} {@link Class}.
     *
     * @return {@link OfficeFloorSpringBootExtension} {@link Class}.
     */
    public abstract String getOfficeFloorSpringBootExtensionClassName();

    /*
     * ==================== OfficeFloorSpringBootExtensionServiceFactory ================
     */

    @Override
    public OfficeFloorSpringBootExtension createService(ServiceContext context) throws Throwable {
        return SpringBootExtra.loadService(this.getExtraKeyClassName(), this.getOfficeFloorSpringBootExtensionClassName(), context, this);
    }

    /*
     * =========================== OfficeFloorSpringBootExtension ========================
     */

    @Override
    public void extendSpringBootSupport(OfficeFloorSpringBootExtensionContext context) throws Exception {
        // Not on class path, so no extra support
    }
}
