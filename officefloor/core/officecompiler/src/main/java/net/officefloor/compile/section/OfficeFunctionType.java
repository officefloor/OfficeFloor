package net.officefloor.compile.section;

import net.officefloor.compile.object.ObjectDependencyType;
import net.officefloor.compile.spi.office.OfficeSectionFunction;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.frame.api.team.Team;

/**
 * <code>Type definition</code> of the {@link OfficeSectionFunction}.
 *
 * @author Daniel Sagenschneider
 */
public interface OfficeFunctionType {

	/**
	 * <p>
	 * Obtains the name of the {@link OfficeSectionFunction}.
	 * <p>
	 * This aids the {@link OfficeSource} in deciding the {@link Team}
	 * responsible for this {@link OfficeSectionFunction}.
	 * 
	 * @return Name of the {@link OfficeSectionFunction}.
	 */
	String getOfficeFunctionName();

	/**
	 * Obtains the {@link OfficeSubSectionType} directly containing this
	 * {@link OfficeFunctionType}.
	 * 
	 * @return {@link OfficeSubSectionType} directly containing this
	 *         {@link OfficeFunctionType}.
	 */
	OfficeSubSectionType getOfficeSubSectionType();

	/**
	 * <p>
	 * Obtains the {@link ObjectDependencyType} instances that this
	 * {@link OfficeSectionFunction} is dependent upon.
	 * <p>
	 * This aids the {@link OfficeSource} in deciding the {@link Team}
	 * responsible for this {@link OfficeSectionFunction}.
	 * 
	 * @return {@link ObjectDependencyType} instances that this
	 *         {@link OfficeSectionFunction} is dependent upon.
	 */
	ObjectDependencyType[] getObjectDependencies();

}