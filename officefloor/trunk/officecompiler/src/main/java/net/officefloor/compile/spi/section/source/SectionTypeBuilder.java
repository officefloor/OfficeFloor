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

import net.officefloor.compile.section.SectionInputType;
import net.officefloor.compile.section.SectionObjectType;
import net.officefloor.compile.section.SectionOutputType;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.work.TaskEscalationType;

/**
 * Builder to construct the {@link SectionType}.
 * 
 * @author Daniel
 */
public interface SectionTypeBuilder {

	/**
	 * Adds a {@link SectionInputType} to the {@link SectionType}.
	 * 
	 * @param inputName
	 *            Name of the {@link SectionInputType}.
	 * @param parameterType
	 *            Parameter type for the {@link SectionInputType}.
	 */
	void addInput(String inputName, String parameterType);

	/**
	 * Adds a {@link SectionOutputType} to the {@link SectionType}.
	 * 
	 * @param outputName
	 *            Name of the {@link SectionOutputType}.
	 * @param argumentType
	 *            Argument type for the {@link SectionOutputType}.
	 * @param isEscalationOnly
	 *            <code>true</code> if only {@link TaskEscalationType} instances
	 *            are using the {@link SectionOutputType}.
	 */
	void addOutput(String outputName, String argumentType,
			boolean isEscalationOnly);

	/**
	 * Adds a {@link SectionObjectType} to the {@link SectionType}.
	 * 
	 * @param objectName
	 *            Name of the {@link SectionObjectType}.
	 * @param objectType
	 *            Type required for the {@link SectionObjectType}.
	 */
	void addObject(String objectName, String objectType);

}