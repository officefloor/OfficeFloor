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
 * Second class-based governance loaded from {@code officefloor/govern/audit.yml}.
 *
 * Uses {@link AuditExtension} (not {@link TrackingExtension}) so each governance governs
 * its own dedicated managed object.  This avoids the OfficeFloor frame constraint where
 * a managed object governed by multiple governances requires a locally-sequential governance
 * index, which fails when preceding governances (e.g. transaction) shift global indices.
 */
public class AuditGovernance {

    @Govern
    public void govern(AuditExtension extension) {
        extension.notifyAudited();
    }

    @Enforce
    public void enforce() {}
}
