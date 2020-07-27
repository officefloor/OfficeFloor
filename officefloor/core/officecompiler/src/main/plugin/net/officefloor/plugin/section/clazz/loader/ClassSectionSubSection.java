/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
