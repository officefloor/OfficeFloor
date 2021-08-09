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

import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.officefloor.OfficeFloorDependencyRequireNode;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * {@link Object} required by the {@link Office} that is to be provided by the
 * {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeObject extends OfficeDependencyObjectNode, OfficeFloorDependencyRequireNode,
		DependentManagedObject, AdministerableManagedObject, GovernerableManagedObject {

	/**
	 * Obtains the name that the {@link OfficeSource} refers to this {@link Object}.
	 * 
	 * @return Name that the {@link OfficeSource} refers to this {@link Object}.
	 */
	String getOfficeObjectName();

	/**
	 * Specifies the type qualifier.
	 * 
	 * @param qualifier Type qualifier.
	 */
	void setTypeQualifier(String qualifier);

	/**
	 * <p>
	 * Adds an {@link OfficeAdministration} to be done before attempting load this
	 * {@link ManagedObject}.
	 * <p>
	 * The order that the {@link OfficeAdministration} instances are added is the
	 * order they will be done.
	 * 
	 * @param administration {@link OfficeAdministration} to be done before
	 *                       attempting load this {@link ManagedObject}.
	 */
	void addPreLoadAdministration(OfficeAdministration administration);

}
