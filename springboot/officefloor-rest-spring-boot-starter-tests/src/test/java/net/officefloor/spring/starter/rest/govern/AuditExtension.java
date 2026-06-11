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

/**
 * Governance extension interface for {@link AuditGovernance}.
 *
 * Kept distinct from {@link TrackingExtension} so each governance governs its own
 * dedicated managed object, avoiding the OfficeFloor limitation where a managed object
 * governed by multiple governances requires locally-sequential governance indices.
 */
public interface AuditExtension {

    void notifyAudited();
}
