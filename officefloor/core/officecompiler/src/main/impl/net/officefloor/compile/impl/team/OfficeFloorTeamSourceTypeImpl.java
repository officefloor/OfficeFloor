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

package net.officefloor.compile.impl.team;

import net.officefloor.compile.officefloor.OfficeFloorTeamSourcePropertyType;
import net.officefloor.compile.officefloor.OfficeFloorTeamSourceType;
import net.officefloor.frame.api.team.Team;

/**
 * {@link OfficeFloorTeamSourceType} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class OfficeFloorTeamSourceTypeImpl implements OfficeFloorTeamSourceType {

	/**
	 * Name of {@link Team}.
	 */
	private final String name;

	/**
	 * Properties for the {@link Team}.
	 */
	private final OfficeFloorTeamSourcePropertyType[] properties;

	/**
	 * Initiate.
	 * 
	 * @param name
	 *            Name of {@link Team}.
	 * @param properties
	 *            Properties for the {@link Team}.
	 */
	public OfficeFloorTeamSourceTypeImpl(String name,
			OfficeFloorTeamSourcePropertyType[] properties) {
		this.name = name;
		this.properties = properties;
	}

	/*
	 * ===================== OfficeFloorTeamSourceType ========================
	 */

	@Override
	public String getOfficeFloorTeamSourceName() {
		return this.name;
	}

	@Override
	public OfficeFloorTeamSourcePropertyType[] getOfficeFloorTeamSourcePropertyTypes() {
		return this.properties;
	}

}
