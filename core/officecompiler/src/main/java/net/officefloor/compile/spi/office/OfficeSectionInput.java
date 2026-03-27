/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
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

package net.officefloor.compile.spi.office;

import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.officefloor.ExternalServiceInput;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionService;
import net.officefloor.frame.api.build.OfficeFloorListener;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.InputManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * Input into the {@link OfficeSection}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeSectionInput extends OfficeFlowSinkNode {

	/**
	 * Obtains the {@link OfficeSection} containing this
	 * {@link OfficeSectionInput}.
	 * 
	 * @return {@link OfficeSection} containing this {@link OfficeSectionInput}.
	 */
	OfficeSection getOfficeSection();

	/**
	 * Obtains the name of this {@link OfficeSectionInput}.
	 * 
	 * @return Name of this {@link OfficeSectionInput}.
	 */
	String getOfficeSectionInputName();

	/**
	 * Adds an {@link ExecutionExplorer} for the execution tree from this
	 * {@link OfficeSectionInput}.
	 * 
	 * @param executionExplorer
	 *            {@link ExecutionExplorer}.
	 */
	void addExecutionExplorer(ExecutionExplorer executionExplorer);

    /**
     * See {@link DeployedOfficeInput} as allows {@link net.officefloor.compile.spi.office.source.OfficeSource} to configure inputs.
     *
     * @param <O>               Object type.
     * @param <M>               {@link ManagedObject} type.
     * @param objectType        Type of object provided to the
     *                          {@link ExternalServiceInput}.
     * @param managedObjectType Type of the {@link ManagedObject} to the
     *                          {@link ExternalServiceInput}.
     * @return {@link ExternalServiceInput}.
     */
    <O, M extends InputManagedObject> ExternalServiceInput<O, M> addExternalServiceInput(Class<O> objectType,
                                                                                         Class<M> managedObjectType);

    /**
     * Adds qualified {@link ExternalServiceInput}.
     *
     * @param <O>               Object type.
     * @param <M>               {@link ManagedObject} type.
     * @param objectType        Type of object provided to the
     *                          {@link ExternalServiceInput}.
     * @param typeQualifier     Type qualifier for the
     *                          {@link ExternalServiceInput}.
     * @param managedObjectType Type of the {@link ManagedObject} to the
     *                          {@link ExternalServiceInput}.
     * @return {@link ExternalServiceInput}.
     */
    <O, M extends InputManagedObject> ExternalServiceInput<O, M> addExternalServiceInput(Class<O> objectType,
                                                                                         String typeQualifier, Class<M> managedObjectType);

}
