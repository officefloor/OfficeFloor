/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.api.managedobject;

import net.officefloor.frame.api.managedobject.recycle.CleanupEscalation;

/**
 * {@link ManagedObject} passed in externally.
 */
public interface InputManagedObject extends ManagedObject {

    /**
     * Invoked to clean the {@link ManagedObject} at end of servicing.
     *
     * @param cleanupEscalations {@link CleanupEscalation} instances on failure of servicing.
     * @throws Throwable If fails to handle the {@link CleanupEscalation} instances.
     */
    void clean(CleanupEscalation[] cleanupEscalations) throws Throwable;

}
