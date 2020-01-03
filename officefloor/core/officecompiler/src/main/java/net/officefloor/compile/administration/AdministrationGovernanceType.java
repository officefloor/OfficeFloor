package net.officefloor.compile.administration;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.governance.Governance;

/**
 * <code>Type definition</code> of a {@link Governance} used by
 * {@link Administration}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdministrationGovernanceType<F extends Enum<F>> {

	/**
	 * Obtains the name of the {@link Governance}.
	 * 
	 * @return Name of the {@link Governance}.
	 */
	String getGovernanceName();

	/**
	 * Obtains the index identifying the {@link Governance}.
	 * 
	 * @return Index identifying the {@link Governance}.
	 */
	int getIndex();

	/**
	 * Obtains the key identifying the {@link Governance}.
	 * 
	 * @return Key identifying the {@link Governance}.
	 */
	F getKey();

}