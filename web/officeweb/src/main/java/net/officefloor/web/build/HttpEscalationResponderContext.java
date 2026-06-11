/*-
 * #%L
 * Web Plug-in
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

package net.officefloor.web.build;

import net.officefloor.server.http.ServerHttpConnection;

/**
 * Context for the {@link HttpEscalationResponder}.
 */
public interface HttpEscalationResponderContext<E extends Throwable> {

    /**
     * Obtains the response {@link net.officefloor.frame.api.escalate.Escalation} being sent.
     *
     * @return Response {@link net.officefloor.frame.api.escalate.Escalation} being sent.
     */
    E getEscalation();

    /**
     * <p>
     * Indicates if the {@link net.officefloor.frame.api.escalate.Escalation} is handled by {@link net.officefloor.frame.api.manage.OfficeFloor}.
     * <p>
     * Allows custom handling of the {@link net.officefloor.frame.api.escalate.Escalation}.
     *
     * @return <code>true</code> if handled by {@link net.officefloor.frame.api.manage.OfficeFloor}.
     */
    boolean isOfficeFloorEscalation();

    /**
     * Obtains the {@link ServerHttpConnection}.
     *
     * @return {@link ServerHttpConnection}.
     */
    ServerHttpConnection getServerHttpConnection();

}
