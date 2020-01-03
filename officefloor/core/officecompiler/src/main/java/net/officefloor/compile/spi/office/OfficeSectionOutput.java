package net.officefloor.compile.spi.office;

/**
 * Output from the {@link OfficeSection}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeSectionOutput extends OfficeFlowSourceNode {

	/**
	 * Obtains the {@link OfficeSection} containing this
	 * {@link OfficeSectionOutput}.
	 * 
	 * @return {@link OfficeSection} containing this
	 *         {@link OfficeSectionOutput}.
	 */
	OfficeSection getOfficeSection();

	/**
	 * Obtains the name of this {@link OfficeSectionOutput}.
	 * 
	 * @return Name of this {@link OfficeSectionOutput}.
	 */
	String getOfficeSectionOutputName();

}