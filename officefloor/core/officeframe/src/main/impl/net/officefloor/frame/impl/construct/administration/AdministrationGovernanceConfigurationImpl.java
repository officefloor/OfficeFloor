package net.officefloor.frame.impl.construct.administration;

import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.internal.configuration.AdministrationGovernanceConfiguration;

/**
 * {@link AdministrationGovernanceConfiguration} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class AdministrationGovernanceConfigurationImpl<G extends Enum<G>>
		implements AdministrationGovernanceConfiguration<G> {

	/**
	 * Key of the {@link Governance}.
	 */
	private final G key;

	/**
	 * Index of the {@link Governance}.
	 */
	private final int index;

	/**
	 * Name of the {@link Governance}.
	 */
	private final String governanceName;

	/**
	 * Instantiate.
	 * 
	 * @param key
	 *            Key of the {@link Governance}.
	 * @param index
	 *            Index of the {@link Governance}.
	 * @param governanceName
	 *            Name of the {@link Governance}.
	 */
	public AdministrationGovernanceConfigurationImpl(G key, int index, String governanceName) {
		this.key = key;
		this.index = index;
		this.governanceName = governanceName;
	}

	/*
	 * ================= AdministrationGovernanceConfiguration =================
	 */

	@Override
	public G getKey() {
		return this.key;
	}

	@Override
	public int getIndex() {
		return this.index;
	}

	@Override
	public String getGovernanceName() {
		return this.governanceName;
	}

}