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

import org.springframework.stereotype.Component;

/**
 * Spring-managed bean that implements {@link TrackingExtension}.
 *
 * Because it is a Spring bean it is available as a managed object in OfficeFloor.
 * Any governance whose {@code @Govern} method accepts {@link TrackingExtension} will
 * govern this bean when the function is decorated with that governance.
 *
 * {@code notificationCount} is package-private so the test can reset it before each assertion.
 */
@Component
public class TrackingComponent implements TrackingExtension {

    int notificationCount = 0;

    @Override
    public void notifyGoverned() {
        notificationCount++;
    }
}
