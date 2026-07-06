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

import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.web.build.HttpInput;

/**
 * REST supported {@link HttpMethod}.
 */
public interface RestMethod {

    /**
     * Indicates if the {@link RestMethod} is secure (e.g. HTTPS).
     *
     * @return <code>true</code> if {@link RestMethod} is secure.
     */
    boolean isSecure();

    /**
     * Obtains the {@link HttpMethod}.
     *
     * @return {@link HttpMethod}.
     */
    HttpMethod getHttpMethod();

    /**
     * Obtains the {@link HttpInput}.
     *
     * @return {@link HttpInput}.
     */
    HttpInput getHttpInput();

    /**
     * Obtains the {@link OfficeSectionInput} to service the {@link RestMethod}.
     *
     * @return {@link OfficeSectionInput} to service the {@link RestMethod}.
     */
    OfficeSectionInput getServiceInput();

    /**
     * Obtains the Momento for the {@link MomentoKey}.
     *
     * @param key {@link MomentoKey}.
     * @param <M> Type of Momento.
     * @return Momento or <code>null</code> if no Momento specified by the respective {@link RestMethodDecorator}.
     */
    <M> M getMomento(MomentoKey<M> key);

}
