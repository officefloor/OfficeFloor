package net.officefloor.compile.impl.administrator;

import net.officefloor.compile.administration.AdministrationGovernanceType;
import net.officefloor.frame.api.governance.Governance;

/**
 * {@link AdministrationGovernanceType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class AdministrationGovernanceTypeImpl<G extends Enum<G>> implements AdministrationGovernanceType<G> {

	/**
	 * Name of {@link Governance}.
	 */
	private final String governanceName;

	/**
	 * Index identifying the {@link AdministrationGovernanceType}.
	 */
	private final int index;

	/**
	 * Key identifying the {@link AdministrationGovernanceType}.
	 */
	private final G key;

	/**
	 * Instantiate.
	 * 
	 * @param governanceName
	 *            Name of {@link Governance}.
	 * @param index
	 *            Index identifying the {@link AdministrationGovernanceType}.
	 * @param key
	 *            Key identifying the {@link AdministrationGovernanceType}.
	 */
	public AdministrationGovernanceTypeImpl(String governanceName, int index, G key) {
		this.governanceName = governanceName;
		this.index = index;
		this.key = key;
	}

	/*
	 * ================= AdministrationGovernanceType ============
	 */

	@Override
	public String getGovernanceName() {
		return this.governanceName;
	}

	@Override
	public int getIndex() {
		return this.index;
	}

	@Override
	public G getKey() {
		return this.key;
	}

}