package net.officefloor.compile.section;

import net.officefloor.compile.spi.office.OfficeSection;

/**
 * <code>Type definition</code> of a section of the {@link OfficeSection}.
 *
 * @author Daniel Sagenschneider
 */
public interface OfficeSectionType extends OfficeSubSectionType {

	/**
	 * Obtains the {@link OfficeSectionInputType} instances for this
	 * {@link OfficeSection}.
	 * 
	 * @return {@link OfficeSectionInputType} instances for this
	 *         {@link OfficeSection}.
	 */
	OfficeSectionInputType[] getOfficeSectionInputTypes();

	/**
	 * Obtains the {@link OfficeSectionOutputType} instances for this
	 * {@link OfficeSection}.
	 * 
	 * @return {@link OfficeSectionOutputType} instances for this
	 *         {@link OfficeSection}.
	 */
	OfficeSectionOutputType[] getOfficeSectionOutputTypes();

	/**
	 * Obtains the {@link OfficeSectionObjectType} instances required by this
	 * {@link OfficeSection}.
	 * 
	 * @return {@link OfficeSectionObjectType} instances required by this
	 *         {@link OfficeSection}.
	 */
	OfficeSectionObjectType[] getOfficeSectionObjectTypes();

}