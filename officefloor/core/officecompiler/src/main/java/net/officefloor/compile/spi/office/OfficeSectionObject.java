package net.officefloor.compile.spi.office;

/**
 * Object required by the {@link OfficeSection}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeSectionObject extends OfficeDependencyRequireNode {

	/**
	 * Obtains the {@link OfficeSection} containing this
	 * {@link OfficeSectionOutput}.
	 * 
	 * @return {@link OfficeSection} containing this
	 *         {@link OfficeSectionOutput}.
	 */
	OfficeSection getOfficeSection();

	/**
	 * Obtains the name of this {@link OfficeSectionObject}.
	 * 
	 * @return Name of this {@link OfficeSectionObject}.
	 */
	String getOfficeSectionObjectName();

}