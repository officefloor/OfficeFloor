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
package net.officefloor.model.impl.section;

import net.officefloor.compile.spi.section.Section;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.SectionSourceSpecification;
import net.officefloor.compile.spi.section.source.SectionTypeBuilder;
import net.officefloor.model.section.SectionModel;

/**
 * {@link SectionSource} for a {@link SectionModel}.
 * 
 * @author Daniel
 */
public class SectionModelSectionSource implements SectionSource {

	/*
	 * ================== SectionSource ===========================
	 */

	@Override
	public SectionSourceSpecification getSpecification() {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement SectionSource.getSpecification");
	}

	@Override
	public Section sourceSection(SectionSourceContext context) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement SectionSource.sourceSection");
	}

	@Override
	public void sourceSectionType(SectionTypeBuilder sectionTypeBuilder,
			SectionSourceContext context) throws Exception {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement SectionSource.sourceSectionType");
	}

}