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

package net.officefloor.compile.spi.officefloor;

import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionService;
import net.officefloor.frame.api.build.OfficeFloorListener;
import net.officefloor.frame.api.manage.FunctionManager;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * Input into a {@link DeployedOffice}.
 * 
 * @author Daniel Sagenschneider
 */
public interface DeployedOfficeInput extends OfficeFloorFlowSinkNode {

	/**
	 * Obtains the name of the {@link DeployedOfficeInput}.
	 * 
	 * @return Name of the {@link DeployedOfficeInput}.
	 */
	String getDeployedOfficeInputName();

	/**
	 * Obtains the {@link DeployedOffice} containing this
	 * {@link DeployedOfficeInput}.
	 * 
	 * @return {@link DeployedOffice} containing this {@link DeployedOfficeInput}.
	 */
	DeployedOffice getDeployedOffice();

	/**
	 * <p>
	 * Obtains the {@link FunctionManager} to externally trigger this
	 * {@link DeployedOfficeInput}.
	 * <p>
	 * This allows {@link OfficeFloorExtensionService} instances to obtain the
	 * {@link FunctionManager} for external triggering of service handling.
	 * 
	 * @return {@link FunctionManager} to externally trigger this
	 *         {@link DeployedOfficeInput}.
	 */
	FunctionManager getFunctionManager();

	/**
	 * <p>
	 * Adds an {@link ExternalServiceInput} to externally trigger this
	 * {@link DeployedOfficeInput}.
	 * <p>
	 * This allows {@link OfficeFloorExtensionService} instances to run external
	 * services (running within their own {@link Thread}) to use {@link OfficeFloor}
	 * to service.
	 * <p>
	 * Should the external service require running within the {@link OfficeFloor}
	 * open/close life-cycle, add an {@link OfficeFloorListener} to the
	 * {@link OfficeFloorDeployer}.
	 * <p>
	 * An example use case is running {@link OfficeFloor} within a JEE server and
	 * having {@link OfficeFloor} service the Servlet requests.
	 * <p>
	 * Note should more complex interaction be required with {@link OfficeFloor},
	 * consider creating a {@link ManagedObjectSource} and invoking services through
	 * the {@link ManagedObjectExecuteContext}.
	 *
	 * @param <O>                      Object type.
	 * @param <M>                      {@link ManagedObject} type.
	 * @param objectType               Type of object provided to the
	 *                                 {@link ExternalServiceInput}.
	 * @param managedObjectType        Type of the {@link ManagedObject} to the
	 *                                 {@link ExternalServiceInput}.
	 * @param cleanupEscalationHandler {@link ExternalServiceCleanupEscalationHandler}.
	 * @return {@link ExternalServiceInput}.
	 */
	<O, M extends ManagedObject> ExternalServiceInput<O, M> addExternalServiceInput(Class<O> objectType,
			Class<? extends M> managedObjectType,
			ExternalServiceCleanupEscalationHandler<? super M> cleanupEscalationHandler);

	/**
	 * Adds qualified {@link ExternalServiceInput}.
	 * 
	 * @param <O>                      Object type.
	 * @param <M>                      {@link ManagedObject} type.
	 * @param objectType               Type of object provided to the
	 *                                 {@link ExternalServiceInput}.
	 * @param typeQualifier            Type qualifier for the
	 *                                 {@link ExternalServiceInput}.
	 * @param managedObjectType        Type of the {@link ManagedObject} to the
	 *                                 {@link ExternalServiceInput}.
	 * @param cleanupEscalationHandler {@link ExternalServiceCleanupEscalationHandler}.
	 * @return {@link ExternalServiceInput}.
	 */
	<O, M extends ManagedObject> ExternalServiceInput<O, M> addExternalServiceInput(Class<O> objectType,
			String typeQualifier, Class<? extends M> managedObjectType,
			ExternalServiceCleanupEscalationHandler<? super M> cleanupEscalationHandler);

}
