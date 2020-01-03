package net.officefloor.compile.spi.office;

import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.manage.Office;

/**
 * {@link OfficeSectionFunction} within the {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeSectionFunction {

	/**
	 * Obtains the name of the {@link OfficeSectionFunction}.
	 * 
	 * @return Name of the {@link OfficeSectionFunction}.
	 */
	String getOfficeFunctionName();

	/**
	 * Obtains the {@link ResponsibleTeam} responsible for this
	 * {@link OfficeSectionFunction}.
	 * 
	 * @return {@link ResponsibleTeam} responsible for this
	 *         {@link OfficeSectionFunction}.
	 */
	ResponsibleTeam getResponsibleTeam();

	/**
	 * <p>
	 * Adds an {@link OfficeAdministration} to be done before attempting this
	 * {@link OfficeSectionFunction}.
	 * <p>
	 * The order that the {@link OfficeAdministration} instances are added is the
	 * order they will be done before this {@link OfficeSectionFunction}.
	 * 
	 * @param administration
	 *            {@link OfficeAdministration} to be done before attempting this
	 *            {@link OfficeSectionFunction}.
	 */
	void addPreAdministration(OfficeAdministration administration);

	/**
	 * <p>
	 * Adds an {@link OfficeAdministration} to be done after completing this
	 * {@link OfficeSectionFunction}.
	 * <p>
	 * The order that the {@link OfficeAdministration} instances are added is the
	 * order they will be done after this {@link OfficeSectionFunction} is complete.
	 * 
	 * @param administration
	 *            {@link OfficeAdministration} to be done after completing this
	 *            {@link OfficeSectionFunction}.
	 */
	void addPostAdministration(OfficeAdministration administration);

	/**
	 * <p>
	 * Adds {@link Governance} for this {@link OfficeSectionFunction}.
	 * <p>
	 * This enables specifying specifically which {@link OfficeSectionFunction}
	 * instances require {@link Governance}.
	 * 
	 * @param governance
	 *            {@link OfficeGovernance}.
	 */
	void addGovernance(OfficeGovernance governance);

}