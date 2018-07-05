/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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