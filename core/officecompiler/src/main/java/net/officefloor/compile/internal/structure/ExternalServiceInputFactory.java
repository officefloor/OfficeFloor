/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.compile.internal.structure;

import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.officefloor.ExternalServiceInput;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.InputManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * Factory to create an {@link ExternalServiceInput} to {@link OfficeFloor}.
 *
 * @param <O> Type of object returned from the {@link ManagedObject}.
 * @param <M> Type of {@link ManagedObject}.
 * @author Daniel Sagenschneider
 */
public interface ExternalServiceInputFactory<O, M extends InputManagedObject> {

    /**
     * Creates the {@link ExternalServiceInput} for the {@link DeployedOfficeInput}.
     *
     * @param deployedOfficeInput {@link DeployedOfficeInput} to service.
     * @return {@link ExternalServiceInput}.
     */
    ExternalServiceInputNode<O, M> createExternalServiceInput(DeployedOfficeInput deployedOfficeInput);

}
