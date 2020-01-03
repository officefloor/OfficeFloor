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