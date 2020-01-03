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