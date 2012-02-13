/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

package net.officefloor.compile.internal.structure;

import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.section.SectionWork;
import net.officefloor.compile.work.WorkType;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;

/**
 * {@link SectionWork} node.
 * 
 * @author Daniel Sagenschneider
 */
public interface WorkNode extends SectionWork {

	/**
	 * <p>
	 * Adds context of the {@link Office}.
	 * <p>
	 * The {@link WorkType} can not be retrieved until the {@link Office}
	 * context is provided.
	 * 
	 * @param officeLocation
	 *            Location of the {@link Office}.
	 */
	void addOfficeContext(String officeLocation);

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
	 * Obtains the {@link WorkType} for this {@link WorkNode}.
	 * 
	 * @return {@link WorkType} for this {@link WorkNode}. May be
	 *         <code>null</code> if can not load the {@link WorkType}.
	 */
	WorkType<?> getWorkType();

	/**
	 * Builds the {@link Work} for this {@link SectionWork}.
	 * 
	 * @param builder
	 *            {@link OfficeBuilder}.
	 */
	void buildWork(OfficeBuilder builder);

}