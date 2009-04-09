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
package net.officefloor.compile.spi.section;

import net.officefloor.compile.section.SectionInputType;
import net.officefloor.compile.section.SectionObjectType;
import net.officefloor.compile.section.SectionOutputType;
import net.officefloor.compile.spi.office.source.OfficeSection;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.work.TaskEscalationType;

/**
 * Builder to construct the {@link OfficeSection}.
 * 
 * @author Daniel
 */
public interface SectionBuilder {

	/**
	 * Adds a {@link SectionInput} to the {@link OfficeSection} being built.
	 * 
	 * @param inputName
	 *            Name of the {@link SectionInput}.
	 * @param parameterType
	 *            Parameter type for the {@link SectionInputType}.
	 * @return {@link SectionInput} for linking.
	 */
	SectionInput addSectionInput(String inputName, String parameterType);

	/**
	 * Adds a {@link SectionOutput} to the {@link OfficeSection} being built.
	 * 
	 * @param outputName
	 *            Name of the {@link SectionOutput}.
	 * @param argumentType
	 *            Argument type for the {@link SectionOutputType}.
	 * @param isEscalationOnly
	 *            <code>true</code> if only {@link TaskEscalationType} instances
	 *            are using the {@link SectionOutputType}.
	 * @return {@link SectionOutput} for linking.
	 */
	SectionOutput addSectionOutput(String outputName, String argumentType,
			boolean isEscalationOnly);

	/**
	 * Adds a {@link SectionObject} to the {@link OfficeSection} being built.
	 * 
	 * @param objectName
	 *            Name of the {@link SectionObject}.
	 * @param objectType
	 *            Type required for the {@link SectionObjectType}.
	 * @return {@link SectionObject} for linking.
	 */
	SectionObject addSectionObject(String objectName, String objectType);

	/**
	 * Adds a {@link SectionWork} to the {@link OfficeSection} being built.
	 * 
	 * @param workName
	 *            Name of the {@link SectionWork}.
	 * @param workSourceClassName
	 *            Fully qualified class name of the {@link WorkSource}. This
	 *            allows adding the {@link SectionWork} without having to worry
	 *            if the {@link WorkSource} is available on the class path.
	 *            <b>This should be used over attempting to find the
	 *            {@link WorkSource}</b> - as should leave to compiler to find
	 *            the {@link WorkSource}.
	 * @return {@link SectionWork}.
	 */
	SectionWork addWork(String workName, String workSourceClassName);

	/**
	 * Adds a {@link SectionWork} to the {@link OfficeSection} being built.
	 * 
	 * @param workName
	 *            Name of the {@link SectionWork}.
	 * @param workSource
	 *            {@link WorkSource} to enable providing direct instances. This
	 *            should only be used should the {@link SectionSource} want to
	 *            create a {@link SectionWork} instance by supplying its own
	 *            instantiated {@link WorkSource} implementation.
	 * @return {@link SectionWork}.
	 */
	SectionWork addWork(String workName, WorkSource<?> workSource);

	/**
	 * Adds a {@link SubSection} to the {@link OfficeSection} being built.
	 * 
	 * @param subSectionName
	 *            Name of the {@link SubSection}.
	 * @param sectionSourceClassName
	 *            Fully qualified class name of the {@link SectionSource} for
	 *            the {@link SubSection}. This allows adding the
	 *            {@link SubSection} without having to worry if the
	 *            {@link SectionSource} is available on the class path. <b>This
	 *            should be used over attempting to find the
	 *            {@link SectionSource}</b> - as should leave to the compiler to
	 *            find the {@link SectionSource}.
	 * @param location
	 *            Location of the {@link SubSection}.
	 * @return {@link SubSection}.
	 */
	SubSection addSubSection(String subSectionName,
			String sectionSourceClassName, String location);

	/**
	 * Adds a {@link SubSection} to the {@link OfficeSection} being built.
	 * 
	 * @param subSectionName
	 *            Name of the {@link SubSection}.
	 * @param sectionSource
	 *            {@link SectionSource} to enable providing direct instances.
	 *            This should only be used should the {@link SectionSource} want
	 *            to create a {@link SubSection} instance by supplying its own
	 *            instantiated {@link SectionSource} implementation.
	 * @param location
	 *            Location of the {@link SubSection}.
	 * @return {@link SubSection}.
	 */
	SubSection addSubSection(String subSectionName,
			SectionSource sectionSource, String location);

	/**
	 * Links the {@link SectionInput} to be undertaken by the
	 * {@link SectionTask}.
	 * 
	 * @param sectionInput
	 *            {@link SectionInput}.
	 * @param task
	 *            {@link SectionTask}.
	 */
	void link(SectionInput sectionInput, SectionTask task);

	/**
	 * Links the {@link SectionInput} to be undertaken by the
	 * {@link SubSectionInput}.
	 * 
	 * @param sectionInput
	 *            {@link SectionInput}.
	 * @param subSectionInput
	 *            {@link SubSectionInput}.
	 */
	void link(SectionInput sectionInput, SubSectionInput subSectionInput);

	/**
	 * Links the {@link SubSectionInput} to be undertaken by the
	 * {@link SectionOutput}.
	 * 
	 * @param sectionInput
	 *            {@link SectionInput}.
	 * @param sectionOutput
	 *            {@link SectionOutput}.
	 */
	void link(SectionInput sectionInput, SectionOutput sectionOutput);

}