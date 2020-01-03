package net.officefloor.frame.internal.configuration;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.governance.Governance;

/**
 * Configuration of linking {@link Governance} to {@link Administration}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdministrationGovernanceConfiguration<G extends Enum<G>> {

	/**
	 * Obtains the name of the {@link Governance} to link to the
	 * {@link Administration}.
	 * 
	 * @return Name of the {@link Governance} to link to the
	 *         {@link Administration}.
	 */
	String getGovernanceName();

	/**
	 * Obtains the index identifying the linked {@link Governance}.
	 * 
	 * @return Index identifying the linked {@link Governance}.
	 */
	int getIndex();

	/**
	 * Obtains the key identifying the linked {@link Governance}.
	 * 
	 * @return Key identifying the linked {@link Governance}. <code>null</code>
	 *         if indexed.
	 */
	G getKey();

}