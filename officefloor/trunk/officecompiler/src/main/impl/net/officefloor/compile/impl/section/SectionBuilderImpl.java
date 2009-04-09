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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.compile.section.SectionInputType;
import net.officefloor.compile.section.SectionObjectType;
import net.officefloor.compile.section.SectionOutputType;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.section.SectionBuilder;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.SectionOutput;

/**
 * {@link SectionType} implementation.
 * 
 * @author Daniel
 */
public class SectionBuilderImpl implements SectionBuilder, SectionType {

	/**
	 * {@link SectionInput} instances by their names.
	 */
	private final Map<String, SectionInputImpl> inputs = new HashMap<String, SectionInputImpl>();

	/**
	 * Listing of {@link SectionInputType} instances maintaining the order they
	 * were added.
	 */
	private final List<SectionInputType> inputTypes = new LinkedList<SectionInputType>();

	/**
	 * {@link SectionOutput} instances by their names.
	 */
	private final Map<String, SectionOutputImpl> outputs = new HashMap<String, SectionOutputImpl>();

	/**
	 * Listing of {@link SectionOutputType} instances maintaining the order they
	 * were added.
	 */
	private final List<SectionOutputType> outputTypes = new LinkedList<SectionOutputType>();

	/**
	 * {@link SectionObject} instances by their names.
	 */
	private final Map<String, SectionObjectImpl> objects = new HashMap<String, SectionObjectImpl>();

	/**
	 * Listing of {@link SectionObjectType} instances maintaining the order they
	 * were added.
	 */
	private final List<SectionObjectType> objectTypes = new LinkedList<SectionObjectType>();

	/*
	 * ======================== SectionTypeBuilder =============================
	 */

	@Override
	public SectionInput addInput(String inputName, String parameterType) {
		// Obtain and return the section input for the name
		SectionInputImpl input = this.inputs.get(inputName);
		if (input == null) {
			input = new SectionInputImpl(inputName, parameterType);
			this.inputs.put(inputName, input);
			this.inputTypes.add(input);
		}
		return input;
	}

	@Override
	public SectionOutput addOutput(String outputName, String argumentType,
			boolean isEscalationOnly) {
		// Obtain and return the section output for the name
		SectionOutputImpl output = this.outputs.get(outputName);
		if (output == null) {
			output = new SectionOutputImpl(outputName, argumentType,
					isEscalationOnly);
			this.outputs.put(outputName, output);
			this.outputTypes.add(output);
		}
		return output;
	}

	@Override
	public SectionObject addObject(String objectName, String objectType) {
		// Obtain and return the section object for the name
		SectionObjectImpl object = this.objects.get(objectName);
		if (object == null) {
			object = new SectionObjectImpl(objectName, objectType);
			this.objects.put(objectName, object);
			this.objectTypes.add(object);
		}
		return object;
	}

	/*
	 * ===================== SectionType ===================================
	 */

	@Override
	public SectionInputType[] getSectionInputTypes() {
		return this.inputTypes.toArray(new SectionInputType[0]);
	}

	@Override
	public SectionOutputType[] getSectionOutputTypes() {
		return this.outputTypes.toArray(new SectionOutputType[0]);
	}

	@Override
	public SectionObjectType[] getSectionObjectTypes() {
		return this.objectTypes.toArray(new SectionObjectType[0]);
	}

}