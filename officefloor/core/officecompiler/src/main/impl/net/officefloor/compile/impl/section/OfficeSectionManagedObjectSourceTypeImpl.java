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

package net.officefloor.compile.impl.section;

import net.officefloor.compile.section.OfficeSectionManagedObjectSourceType;
import net.officefloor.compile.section.OfficeSectionManagedObjectTeamType;
import net.officefloor.compile.spi.managedobject.ManagedObjectTeam;
import net.officefloor.compile.spi.office.OfficeSectionManagedObjectSource;

/**
 * {@link OfficeSectionManagedObjectSourceType} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class OfficeSectionManagedObjectSourceTypeImpl implements
		OfficeSectionManagedObjectSourceType {

	/**
	 * Name of the {@link OfficeSectionManagedObjectSource}.
	 */
	private final String managedObjectSourceName;

	/**
	 * {@link OfficeSectionManagedObjectTeamType} instances for the
	 * {@link ManagedObjectTeam} instances of the
	 * {@link OfficeSectionManagedObjectSource}.
	 */
	private final OfficeSectionManagedObjectTeamType[] teamTypes;

	/**
	 * Instantiate.
	 * 
	 * @param managedObjectSourceName
	 *            Name of the {@link OfficeSectionManagedObjectSource}.
	 * @param teamTypes
	 *            {@link OfficeSectionManagedObjectTeamType} instances for the
	 *            {@link ManagedObjectTeam} instances of the
	 *            {@link OfficeSectionManagedObjectSource}.
	 */
	public OfficeSectionManagedObjectSourceTypeImpl(
			String managedObjectSourceName,
			OfficeSectionManagedObjectTeamType[] teamTypes) {
		this.managedObjectSourceName = managedObjectSourceName;
		this.teamTypes = teamTypes;
	}

	/*
	 * ================== OfficeSectionManagedObjectSourceType ===============
	 */

	@Override
	public String getOfficeSectionManagedObjectSourceName() {
		return this.managedObjectSourceName;
	}

	@Override
	public OfficeSectionManagedObjectTeamType[] getOfficeSectionManagedObjectTeamTypes() {
		return this.teamTypes;
	}

}
