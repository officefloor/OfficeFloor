/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.compile.spi.section.source;

import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.section.Section;

/**
 * Sources the {@link SectionType}.
 * 
 * @author Daniel
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
	 * Sources the {@link SectionType} by populating it via the input
	 * {@link SectionTypeBuilder}.
	 * 
	 * @param sectionTypeBuilder
	 *            {@link SectionTypeBuilder} to be populated with the
	 *            <code>type definition</code> of the {@link SectionType}.
	 * @param context
	 *            {@link SectionSourceContext} to source details to populate the
	 *            {@link SectionType}.
	 * @throws Exception
	 *             If fails to populate the {@link SectionType}.
	 */
	void sourceSectionType(SectionTypeBuilder sectionTypeBuilder,
			SectionSourceContext context) throws Exception;

	/**
	 * Sources the {@link Section}.
	 * 
	 * @return {@link Section}.
	 */
	Section sourceSection();

}