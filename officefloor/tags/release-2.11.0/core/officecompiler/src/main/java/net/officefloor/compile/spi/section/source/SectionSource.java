/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.compile.spi.section.source;

import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.section.SectionDesigner;

/**
 * Sources the {@link SectionType}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SectionSource {

	/**
	 * <p>
	 * Obtains the {@link SectionSourceSpecification} for this
	 * {@link SectionSource}.
	 * <p>
	 * This enables the {@link SectionSourceContext} to be populated with the
	 * necessary details as per this {@link SectionSourceSpecification} in
	 * loading the {@link SectionType}.
	 * 
	 * @return {@link SectionSourceSpecification}.
	 */
	SectionSourceSpecification getSpecification();

	/**
	 * Sources the {@link OfficeSection} by constructing it via the input
	 * {@link SectionDesigner}.
	 * 
	 * @param designer
	 *            {@link SectionDesigner} to construct the structure of the
	 *            {@link OfficeSection}.
	 * @param context
	 *            {@link SectionSourceContext} to source details to construct
	 *            the {@link OfficeSection}.
	 * @throws Exception
	 *             If fails to construct the {@link OfficeSection}.
	 */
	void sourceSection(SectionDesigner designer, SectionSourceContext context)
			throws Exception;

}