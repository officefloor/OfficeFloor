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

package net.officefloor.plugin.section.clazz.loader;

import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.section.SubSection;

/**
 * {@link SubSection} with meta-data for {@link ClassSectionLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassSectionSubSection {

	/**
	 * {@link SubSection}.
	 */
	private final SubSection subSection;

	/**
	 * {@link SectionType}.
	 */
	private final SectionType sectionType;

	/**
	 * Instantiate.
	 * 
	 * @param subSection  {@link SubSection}.
	 * @param sectionType {@link SectionType}.
	 */
	public ClassSectionSubSection(SubSection subSection, SectionType sectionType) {
		this.subSection = subSection;
		this.sectionType = sectionType;
	}

	/**
	 * Obtains the {@link SubSection}.
	 * 
	 * @return {@link SubSection}.
	 */
	public SubSection getSubSection() {
		return subSection;
	}

	/**
	 * Obtains the {@link SectionType}.
	 * 
	 * @return {@link SectionType}.
	 */
	public SectionType getSectionType() {
		return sectionType;
	}

}
