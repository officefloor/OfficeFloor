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
package net.officefloor.compile.internal.structure;

import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.section.SectionWork;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.execute.Work;

/**
 * {@link SectionWork} node.
 * 
 * @author Daniel
 */
public interface WorkNode extends SectionWork {

	/**
	 * Obtains the {@link SectionNode} containing this {@link WorkNode}.
	 * 
	 * @return {@link SectionNode} containing this {@link WorkNode}.
	 */
	SectionNode getSectionNode();

	/**
	 * Obtains the name of this {@link Work} qualified with the
	 * {@link OfficeSection} hierarchy containing this {@link Work}.
	 * 
	 * @return Qualified name of this {@link Work}.
	 */
	String getQualifiedWorkName();

	/**
	 * Builds the {@link Work} for this {@link SectionWork}.
	 * 
	 * @param builder
	 *            {@link OfficeBuilder}.
	 */
	void buildWork(OfficeBuilder builder);

}