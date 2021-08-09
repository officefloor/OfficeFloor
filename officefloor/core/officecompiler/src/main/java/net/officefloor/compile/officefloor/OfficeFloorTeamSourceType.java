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
import net.officefloor.frame.api.team.source.TeamSource;

/**
 * <code>Type definition</code> of a {@link TeamSource} available to be
 * configured in the {@link OfficeFloor}.
 *
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorTeamSourceType {

	/**
	 * Obtains the name of the {@link TeamSource} within the {@link OfficeFloor}
	 * that may be configured.
	 * 
	 * @return Name of the {@link TeamSource} within the {@link OfficeFloor}
	 *         that may be configured.
	 */
	String getOfficeFloorTeamSourceName();

	/**
	 * Obtains the {@link OfficeFloorTeamSourcePropertyType} instances identify
	 * the {@link Property} instances that may be configured for this
	 * {@link TeamSource}.
	 * 
	 * @return {@link OfficeFloorTeamSourcePropertyType} instances.
	 */
	OfficeFloorTeamSourcePropertyType[] getOfficeFloorTeamSourcePropertyTypes();

}
