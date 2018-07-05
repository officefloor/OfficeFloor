/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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