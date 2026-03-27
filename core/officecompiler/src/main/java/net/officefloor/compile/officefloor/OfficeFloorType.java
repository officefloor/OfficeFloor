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

package net.officefloor.compile.officefloor;

import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.team.source.TeamSource;

/**
 * <code>Type definition</code> of an {@link OfficeFloor}.
 *
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorType {

	/**
	 * Obtains the {@link Property} instances to be configured for this
	 * {@link OfficeFloor}.
	 * 
	 * @return {@link Property} instances to be configured for this
	 *         {@link OfficeFloor}.
	 */
	OfficeFloorPropertyType[] getOfficeFloorPropertyTypes();

	/**
	 * Obtains the <code>type definitions</code> of the {@link ManagedObjectSource}
	 * instances that may be configured for the {@link OfficeFloor}.
	 * 
	 * @return {@link OfficeFloorManagedObjectSourceType} instances.
	 */
	OfficeFloorManagedObjectSourceType[] getOfficeFloorManagedObjectSourceTypes();

	/**
	 * Obtains the <code>type definitions</code> of the {@link TeamSource} instances
	 * that may be configured for the {@link OfficeFloor}.
	 * 
	 * @return {@link OfficeFloorTeamSourceType} instances.
	 */
	OfficeFloorTeamSourceType[] getOfficeFloorTeamSourceTypes();

}
