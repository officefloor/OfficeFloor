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

package net.officefloor.spring.starter.rest.response;

import net.officefloor.spring.starter.rest.SpringServerHttpConnection;
import net.officefloor.web.build.HttpEscalationResponder;
import net.officefloor.web.build.HttpEscalationResponderContext;
import net.officefloor.web.build.HttpObjectResponder;
import net.officefloor.web.build.HttpObjectResponderContext;
import net.officefloor.web.build.HttpObjectResponderFactory;

import java.io.IOException;

/** {@link HttpObjectResponderFactory} for Spring. */
public class SpringHttpObjectResponderFactory implements HttpObjectResponderFactory,
        HttpObjectResponder<Object>, HttpEscalationResponder<Throwable> {

    /*
     * ================== HttpObjectResponderFactory ===============
     */

    @Override
    public String getContentType() {
        return "*/*";
    }

    @Override
    public <T> HttpObjectResponder<T> createHttpObjectResponder(Class<T> objectType) {
        return (HttpObjectResponder<T>) this;
    }

    @Override
    public <E extends Throwable> HttpEscalationResponder<E> createHttpEscalationResponder(Class<E> escalationType, boolean isOfficeFloorEscalation) {
        return isOfficeFloorEscalation ? null : (HttpEscalationResponder<E>) this;
    }

    /*
     * ===================== HttpObjectResponder ====================
     */

    @Override
    public void send(HttpObjectResponderContext<Object> context) throws IOException {

        // Capture response for Spring to handle
        SpringServerHttpConnection connection = (SpringServerHttpConnection) context.getServerHttpConnection();
        Object returnValue = context.getResponseObject();
        connection.setResponse(returnValue, context.getManagedFunctionObjectType(), null, null);
    }

    /*
     * ====================== HttpObjectResponder ========================
     */

    @Override
    public void send(HttpEscalationResponderContext<Throwable> context) throws IOException {

        // Capture failure for Spring to handle
        SpringServerHttpConnection springConnection = (SpringServerHttpConnection) context.getServerHttpConnection();
        Throwable escalation = context.getEscalation();
        springConnection.setResponse(null, null, null, escalation);
    }

}
