package net.officefloor.compile.impl.office;

import net.officefloor.compile.office.OfficeInputType;
import net.officefloor.compile.office.OfficeManagedObjectType;
import net.officefloor.compile.office.OfficeOutputType;
import net.officefloor.compile.office.OfficeAvailableSectionInputType;
import net.officefloor.compile.office.OfficeTeamType;
import net.officefloor.compile.office.OfficeType;

/**
 * {@link OfficeType} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class OfficeTypeImpl implements OfficeType {

	/**
	 * {@link OfficeInputType} instances.
	 */
	private final OfficeInputType[] inputs;

	/**
	 * {@link OfficeOutputType} instances.
	 */
	private final OfficeOutputType[] outputs;

	/**
	 * {@link OfficeTeamType} instances.
	 */
	private final OfficeTeamType[] teams;

	/**
	 * {@link OfficeManagedObjectType} instances.
	 */
	private final OfficeManagedObjectType[] managedObjects;

	/**
	 * {@link OfficeAvailableSectionInputType} instances.
	 */
	private final OfficeAvailableSectionInputType[] sectionInputs;

	/**
	 * Initiate.
	 * 
	 * @param inputs
	 *            {@link OfficeInputType} instances.
	 * @param outputs
	 *            {@link OfficeOutputType} instances.
	 * @param teams
	 *            {@link OfficeTeamType} instances.
	 * @param managedObjects
	 *            {@link OfficeManagedObjectType} instances.
	 * @param sectionInputs
	 *            {@link OfficeAvailableSectionInputType} instances.
	 */
	public OfficeTypeImpl(OfficeInputType[] inputs, OfficeOutputType[] outputs,
			OfficeTeamType[] teams, OfficeManagedObjectType[] managedObjects,
			OfficeAvailableSectionInputType[] sectionInputs) {
		this.inputs = inputs;
		this.outputs = outputs;
		this.teams = teams;
		this.managedObjects = managedObjects;
		this.sectionInputs = sectionInputs;
	}

	/**
	 * =================== OfficeType =========================
	 */

	@Override
	public OfficeInputType[] getOfficeInputTypes() {
		return this.inputs;
	}

	@Override
	public OfficeOutputType[] getOfficeOutputTypes() {
		return this.outputs;
	}

	@Override
	public OfficeTeamType[] getOfficeTeamTypes() {
		return this.teams;
	}

	@Override
	public OfficeManagedObjectType[] getOfficeManagedObjectTypes() {
		return this.managedObjects;
	}

	@Override
	public OfficeAvailableSectionInputType[] getOfficeSectionInputTypes() {
		return this.sectionInputs;
	}

}