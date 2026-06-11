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

package net.officefloor.spring.starter.rest.govern;

import net.officefloor.web.ObjectResponse;

/**
 * REST service decorated with a single governance ({@code govern: [ tracking ]}).
 *
 * {@link TrackingGovernance#govern} is called before this method executes, so
 * {@link TrackingComponent#notificationCount} is already 1 by the time the response is sent.
 */
public class GovernedService {

    public void service(TrackingComponent tracking, ObjectResponse<String> response) {
        response.send(String.valueOf(tracking.notificationCount));
    }
}
