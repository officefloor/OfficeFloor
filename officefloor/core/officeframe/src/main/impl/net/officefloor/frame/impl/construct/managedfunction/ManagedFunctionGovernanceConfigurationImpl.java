package net.officefloor.frame.impl.construct.managedfunction;

import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.internal.configuration.ManagedFunctionGovernanceConfiguration;

/**
 * {@link ManagedFunctionGovernanceConfiguration} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionGovernanceConfigurationImpl implements
		ManagedFunctionGovernanceConfiguration {

	/**
	 * {@link Governance} name.
	 */
	private final String governanceName;

	/**
	 * Initiate.
	 * 
	 * @param governanceName
	 *            {@link Governance} name.
	 */
	public ManagedFunctionGovernanceConfigurationImpl(String governanceName) {
		this.governanceName = governanceName;
	}

	/*
	 * ===================== TaskGovernanceConfiguration =================
	 */

	@Override
	public String getGovernanceName() {
		return this.governanceName;
	}

}