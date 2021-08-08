/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
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
