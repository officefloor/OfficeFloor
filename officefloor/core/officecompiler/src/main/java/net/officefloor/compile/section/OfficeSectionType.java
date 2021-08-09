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
