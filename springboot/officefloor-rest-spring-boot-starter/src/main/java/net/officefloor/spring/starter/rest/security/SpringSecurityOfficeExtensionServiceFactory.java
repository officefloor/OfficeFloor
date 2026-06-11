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

package net.officefloor.spring.starter.rest.security;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.extension.OfficeExtensionContext;
import net.officefloor.compile.spi.office.extension.OfficeExtensionService;
import net.officefloor.compile.spi.office.extension.OfficeExtensionServiceFactory;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.spring.starter.rest.SpringBootExtra;

/** {@link OfficeExtensionServiceFactory} for Spring Security. */
public class SpringSecurityOfficeExtensionServiceFactory implements OfficeExtensionServiceFactory, OfficeExtensionService {

    /**
     * May not be on class path, so must dynamically load.
     */
    /** Spring Security filter chain class name. */
    public static final String SPRING_SECURITY_FILTER_CLASS_NAME = "org.springframework.security.web.SecurityFilterChain";
    /** Spring Security office extension class name. */
    public static final String SPRING_SECURITY_OFFICE_EXTENSION_CLASS_NAME = "net.officefloor.spring.starter.rest.security.SpringSecurityOfficeExtension";

    /*
     * ================== OfficeExtensionServiceFactory ================
     */

    @Override
    public OfficeExtensionService createService(ServiceContext context) throws Throwable {
        return SpringBootExtra.loadService(SPRING_SECURITY_FILTER_CLASS_NAME,
                SPRING_SECURITY_OFFICE_EXTENSION_CLASS_NAME, context, this);
    }

    /*
     * ======================== OfficeExtension =========================
     */

    @Override
    public void extendOffice(OfficeArchitect officeArchitect, OfficeExtensionContext context) throws Exception {
        // No Spring Security, so nothing to extend
    }

}
