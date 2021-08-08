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

package net.officefloor.compile.impl.office;

import net.officefloor.compile.office.OfficeTeamType;
import net.officefloor.compile.section.TypeQualification;
import net.officefloor.compile.spi.office.OfficeTeam;

/**
 * {@link OfficeTeamType} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class OfficeTeamTypeImpl implements OfficeTeamType {

	/**
	 * Name of the {@link OfficeTeam}.
	 */
	private final String teamName;

	/**
	 * {@link TypeQualification} instances.
	 */
	private final TypeQualification[] typeQualifications;

	/**
	 * Instantiate.
	 * 
	 * @param teamName
	 *            Name of the {@link OfficeTeam}.
	 * @param typeQualifications
	 *            {@link TypeQualification} instances.
	 */
	public OfficeTeamTypeImpl(String teamName, TypeQualification[] typeQualifications) {
		this.teamName = teamName;
		this.typeQualifications = typeQualifications;
	}

	/*
	 * ==================== OfficeTeamType =========================
	 */

	@Override
	public String getOfficeTeamName() {
		return this.teamName;
	}

	@Override
	public TypeQualification[] getTypeQualification() {
		return this.typeQualifications;
	}

}
