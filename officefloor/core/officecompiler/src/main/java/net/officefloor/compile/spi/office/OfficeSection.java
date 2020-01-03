package net.officefloor.compile.spi.office;

import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.frame.api.manage.Office;

/**
 * {@link OfficeSection} of the {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeSection extends OfficeSubSection, PropertyConfigurable {

	/**
	 * Obtains the {@link OfficeSectionInput}.
	 * 
	 * @param inputName
	 *            Name of the {@link OfficeSectionInput} to obtain.
	 * @return {@link OfficeSectionInput}.
	 */
	OfficeSectionInput getOfficeSectionInput(String inputName);

	/**
	 * Obtains the {@link OfficeSectionOutput}.
	 * 
	 * @param outputName
	 *            Name of the {@link OfficeSectionOutput} to obtain.
	 * @return {@link OfficeSectionOutput}.
	 */
	OfficeSectionOutput getOfficeSectionOutput(String outputName);

	/**
	 * Obtains the {@link OfficeSectionObject}.
	 * 
	 * @param objectName
	 *            Name of the {@link OfficeSectionObject} to obtain.
	 * @return {@link OfficeSectionObject}.
	 */
	OfficeSectionObject getOfficeSectionObject(String objectName);

	/**
	 * <p>
	 * Specifies an {@link OfficeSection} that this {@link OfficeSection} will
	 * inherit its links from.
	 * <p>
	 * Typical example use would be creating an {@link OfficeSection} to render
	 * a web page. For headers and footers, the various links do not want to
	 * have to be configured for each {@link OfficeSection} page. This would
	 * clutter the graphical configuration. Hence the main page can configure
	 * these header and footer links, with all other pages inheriting the links
	 * from the main page.
	 * 
	 * @param superSection
	 *            Super {@link OfficeSection}.
	 */
	void setSuperOfficeSection(OfficeSection superSection);

}