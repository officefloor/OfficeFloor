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
package net.officefloor.compile.impl.section;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.section.SectionInputType;
import net.officefloor.compile.section.SectionObjectType;
import net.officefloor.compile.section.SectionOutputType;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.section.source.SectionTypeBuilder;

/**
 * {@link SectionType} implementation.
 * 
 * @author Daniel
 */
public class SectionTypeImpl implements SectionTypeBuilder, SectionType {

	/**
	 * Listing of {@link SectionInputType} instances.
	 */
	private final List<SectionInputType> inputs = new LinkedList<SectionInputType>();

	/**
	 * Listing of {@link SectionOutputType} instances.
	 */
	private final List<SectionOutputType> outputs = new LinkedList<SectionOutputType>();

	/**
	 * Listing of {@link SectionObjectType} instances.
	 */
	private final List<SectionObjectType> objects = new LinkedList<SectionObjectType>();

	/*
	 * ======================== SectionTypeBuilder =============================
	 */

	@Override
	public void addInput(String inputName, String parameterType) {
		this.inputs.add(new SectionInputTypeImpl(inputName, parameterType));
	}

	@Override
	public void addOutput(String outputName, String argumentType,
			boolean isEscalationOnly) {
		this.outputs.add(new SectionOutputTypeImpl(outputName, argumentType,
				isEscalationOnly));
	}

	@Override
	public void addObject(String objectName, String objectType) {
		this.objects.add(new SectionObjectTypeImpl(objectName, objectType));
	}

	/*
	 * ===================== SectionType ===================================
	 */

	@Override
	public SectionInputType[] getInputTypes() {
		return this.inputs.toArray(new SectionInputType[0]);
	}

	@Override
	public SectionOutputType[] getOutputTypes() {
		return this.outputs.toArray(new SectionOutputType[0]);
	}

	@Override
	public SectionObjectType[] getObjectTypes() {
		return this.objects.toArray(new SectionObjectType[0]);
	}

}