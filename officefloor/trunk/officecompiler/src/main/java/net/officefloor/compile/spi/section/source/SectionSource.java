/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
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
	 * @param sectionBuilder
	 *            {@link SectionDesigner} to construct the structure of the
	 *            {@link OfficeSection}.
	 * @param context
	 *            {@link SectionSourceContext} to source details to construct
	 *            the {@link OfficeSection}.
	 * @throws Exception
	 *             If fails to construct the {@link OfficeSection}.
	 */
	void sourceSection(SectionDesigner sectionBuilder,
			SectionSourceContext context) throws Exception;

}