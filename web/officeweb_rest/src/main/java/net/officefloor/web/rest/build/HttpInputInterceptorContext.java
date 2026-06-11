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

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeFlowSinkNode;
import net.officefloor.compile.spi.office.OfficeFlowSourceNode;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.web.build.HttpInput;

/**
 * Context for the {@link HttpInputInterceptor}.
 */
public interface HttpInputInterceptorContext {

    /**
     * Adds intercepting of the {@link HttpInput} before handling.
     *
     * @param input  {@link OfficeFlowSinkNode} to invoke for interception.
     * @param output {@link OfficeFlowSourceNode} for output of interception to next.
     */
    void link(OfficeFlowSinkNode input, OfficeFlowSourceNode output);

    /**
     * Obtains the {@link OfficeArchitect} to establish links.
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

}
