/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
	 * @param <O>
	 *            Object type.
	 * @param <M>
	 *            {@link ManagedObject} type.
	 * @param objectType
	 *            Type of object provided to the {@link ExternalServiceInput}.
	 * @param managedObjectType
	 *            Type of the {@link ManagedObject} to the
	 *            {@link ExternalServiceInput}.
	 * @param cleanupEscalationHandler
	 *            {@link ExternalServiceCleanupEscalationHandler}.
	 * @return {@link ExternalServiceInput}.
	 */
	<O, M extends ManagedObject> ExternalServiceInput<O, M> addExternalServiceInput(Class<O> objectType,
			Class<? extends M> managedObjectType,
			ExternalServiceCleanupEscalationHandler<? super M> cleanupEscalationHandler);

}
