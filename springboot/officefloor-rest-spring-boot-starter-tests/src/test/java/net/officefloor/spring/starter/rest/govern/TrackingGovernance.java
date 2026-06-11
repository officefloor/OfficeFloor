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

import net.officefloor.plugin.governance.clazz.Enforce;
import net.officefloor.plugin.governance.clazz.Govern;

/**
 * Class-based governance loaded from {@code officefloor/govern/tracking.yml}.
 *
 * The extension type ({@link TrackingExtension}) is inferred from the {@code @Govern} method
 * parameter.  OfficeFloor auto-wires this governance to any managed object implementing
 * {@link TrackingExtension} in functions decorated with {@code govern: [ tracking ]}.
 */
public class TrackingGovernance {

    @Govern
    public void govern(TrackingExtension extension) {
        extension.notifyGoverned();
    }

    @Enforce
    public void enforce() {}
}
