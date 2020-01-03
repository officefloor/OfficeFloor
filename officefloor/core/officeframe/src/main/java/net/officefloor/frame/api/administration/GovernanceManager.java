package net.officefloor.frame.api.administration;

import net.officefloor.frame.api.governance.Governance;

/**
 * Manager over a particular {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public interface GovernanceManager {

	/**
	 * Activates the {@link Governance}.
	 */
	void activateGovernance();

	/**
	 * Enforces the {@link Governance}.
	 */
	void enforceGovernance();

	/**
	 * Disregarding the {@link Governance}.
	 */
	void disregardGovernance();

}