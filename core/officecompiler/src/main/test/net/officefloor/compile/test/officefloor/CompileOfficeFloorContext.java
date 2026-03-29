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

package net.officefloor.compile.test.officefloor;

import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObject;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;

/**
 * Provides context for the {@link CompileOfficeFloorExtension}.
 * 
 * @author Daniel Sagenschneider
 */
public interface CompileOfficeFloorContext {

	/**
	 * Obtains the {@link OfficeFloorDeployer}.
	 * 
	 * @return {@link OfficeFloorDeployer}.
	 */
	OfficeFloorDeployer getOfficeFloorDeployer();

	/**
	 * Obtains the {@link DeployedOffice}.
	 * 
	 * @return {@link DeployedOffice}.
	 */
	DeployedOffice getDeployedOffice();

	/**
	 * Obtains the {@link OfficeFloorSourceContext}.
	 * 
	 * @return {@link OfficeFloorSourceContext}.
	 */
	OfficeFloorSourceContext getOfficeFloorSourceContext();

	/**
	 * Adds an {@link OfficeFloorManagedObject} for
	 * {@link ClassManagedObjectSource}.
	 * 
	 * @param managedObjectName
	 *            Name of the {@link OfficeManagedObject}.
	 * @param managedObjectClass
	 *            {@link Class} for the {@link ClassManagedObjectSource}.
	 * @param scope
	 *            {@link ManagedObjectScope}.
	 * @return {@link OfficeFloorManagedObject}.
	 */
	OfficeFloorManagedObject addManagedObject(String managedObjectName, Class<?> managedObjectClass,
			ManagedObjectScope scope);

}
