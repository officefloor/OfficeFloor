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

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.section.source.SectionSource;

/**
 * Context for the {@link OfficeSectionTransformer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeSectionTransformerContext {

	/**
	 * Obtains the name of the {@link OfficeSection}.
	 * 
	 * @return Name of the {@link OfficeSection}.
	 */
	String getOfficeSectionName();

	/**
	 * Obtains the {@link SectionSource} {@link Class} name of the
	 * {@link OfficeSection} being transformed.
	 * 
	 * @return {@link SectionSource} {@link Class} name of the
	 *         {@link OfficeSection} being transformed.
	 */
	String getSectionSourceClassName();

	/**
	 * Obtains the location of the {@link OfficeSection} being transformed.
	 * 
	 * @return Location of the {@link OfficeSection} being transformed.
	 */
	String getSectionLocation();

	/**
	 * Obtains the {@link PropertyList} of the {@link OfficeSection} being
	 * transformed.
	 * 
	 * @return {@link PropertyList} of the {@link OfficeSection} being
	 *         transformed.
	 */
	PropertyList getSectionProperties();

	/**
	 * Creates a new {@link PropertyList}.
	 * 
	 * @return New {@link PropertyList}.
	 */
	PropertyList createPropertyList();

	/**
	 * Specifies the transformed {@link OfficeSection}.
	 * 
	 * @param sectionSourceClassName
	 *            {@link SectionSource} {@link Class} name.
	 * @param sectionLocation
	 *            {@link SectionSource} location.
	 * @param sectionProperties
	 *            {@link OfficeSection} {@link PropertyList}.
	 */
	void setTransformedOfficeSection(String sectionSourceClassName, String sectionLocation,
			PropertyList sectionProperties);

	/**
	 * Specifies the transformed {@link OfficeSection}.
	 * 
	 * @param sectionSource
	 *            {@link SectionSource}.
	 * @param sectionLocation
	 *            {@link SectionSource} location.
	 * @param sectionProperties
	 *            {@link OfficeSection} {@link PropertyList}.
	 */
	void setTransformedOfficeSection(SectionSource sectionSource, String sectionLocation,
			PropertyList sectionProperties);

}
