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

import java.util.List;

/**
 * {@link RestMethod} implementation.
 */
public class RestMethodImpl implements RestMethod {

    private final boolean isSecure;

    private final HttpMethod httpMethod;

    private final HttpInput httpInput;

    private final OfficeSectionInput sectionInput;

    private Object[] momentos;

    /**
     * Instantiate.
     *
     * @param isSecure     Indicates if {@link HttpMethod} requires secure connection.
     * @param httpMethod   {@link HttpMethod}.
     * @param httpInput    {@link HttpInput} to service this {@link RestMethod}.
     * @param sectionInput {@link OfficeSectionInput} to service this {@link RestMethod}.
     * @param momentos     Momentos.
     */
    public RestMethodImpl(boolean isSecure, HttpMethod httpMethod, HttpInput httpInput,
                          OfficeSectionInput sectionInput, Object[] momentos) {
        this.isSecure = isSecure;
        this.httpMethod = httpMethod;
        this.httpInput = httpInput;
        this.sectionInput = sectionInput;
        this.momentos = momentos;
    }

    /*
     * =================== RestMethod ===================
     */

    @Override
    public boolean isSecure() {
        return this.isSecure;
    }

    @Override
    public HttpMethod getHttpMethod() {
        return this.httpMethod;
    }

    @Override
    public HttpInput getHttpInput() {
        return this.httpInput;
    }

    @Override
    public OfficeSectionInput getServiceInput() {
        return this.sectionInput;
    }

    @Override
    public <M> M getMomento(MomentoKey<M> key) {
        int momentoIndex = MomentoKeyImpl.getMomentoIndex(key);
        return (M) this.momentos[momentoIndex];
    }

}
