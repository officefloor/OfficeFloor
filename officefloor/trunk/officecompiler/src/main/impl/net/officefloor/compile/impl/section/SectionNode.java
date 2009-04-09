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

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.section.SectionInputType;
import net.officefloor.compile.section.SectionObjectType;
import net.officefloor.compile.section.SectionOutputType;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.office.source.OfficeSection;
import net.officefloor.compile.spi.section.SectionBuilder;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.SectionTask;
import net.officefloor.compile.spi.section.SectionWork;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.spi.section.SubSectionInput;
import net.officefloor.compile.spi.section.SubSectionObject;
import net.officefloor.compile.spi.section.SubSectionOutput;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.work.source.WorkSource;

/**
 * Node within the hierarchy of {@link OfficeSection} instances.
 * 
 * @author Daniel
 */
public class SectionNode implements SectionBuilder, SectionType, SubSection {

	/**
	 * Name of this {@link SubSection}.
	 */
	private final String sectionName;

	/**
	 * Class name of the {@link SectionSource}.
	 */
	private final String sectionSourceClassName;

	/**
	 * Location of the {@link OfficeSection} being built by this
	 * {@link SectionBuilder}.
	 */
	private final String sectionLocation;

	/**
	 * {@link CompilerIssues} to report issues.
	 */
	private final CompilerIssues issues;

	/**
	 * {@link SectionInput} instances by their names.
	 */
	private final Map<String, SectionInputNode> inputs = new HashMap<String, SectionInputNode>();

	/**
	 * Listing of {@link SectionInputType} instances maintaining the order they
	 * were added.
	 */
	private final List<SectionInputType> inputTypes = new LinkedList<SectionInputType>();

	/**
	 * {@link SectionOutput} instances by their names.
	 */
	private final Map<String, SectionOutputNode> outputs = new HashMap<String, SectionOutputNode>();

	/**
	 * Listing of {@link SectionOutputType} instances maintaining the order they
	 * were added.
	 */
	private final List<SectionOutputType> outputTypes = new LinkedList<SectionOutputType>();

	/**
	 * {@link SectionObject} instances by their names.
	 */
	private final Map<String, SectionObjectNode> objects = new HashMap<String, SectionObjectNode>();

	/**
	 * Listing of {@link SectionObjectType} instances maintaining the order they
	 * were added.
	 */
	private final List<SectionObjectType> objectTypes = new LinkedList<SectionObjectType>();

	/**
	 * {@link WorkNode} instances by their {@link SectionWork} names.
	 */
	private final Map<String, WorkNode> workNodes = new HashMap<String, WorkNode>();

	/**
	 * Map of {@link TaskNode} instances for this {@link OfficeSection} by their
	 * {@link SectionTask} names.
	 */
	private final Map<String, TaskNode> taskNodes = new HashMap<String, TaskNode>();

	/**
	 * {@link SubSection} instances by their names.
	 */
	private final Map<String, SectionNode> subSections = new HashMap<String, SectionNode>();

	/**
	 * {@link SectionSource} for this {@link SectionNode}.
	 */
	private SectionSource sectionSource;

	/**
	 * Initiate.
	 * 
	 * @param sectionLocation
	 *            Location of the {@link OfficeSection} being built by this
	 *            {@link SectionBuilder}.
	 * @param issues
	 *            {@link CompilerIssues} to report issues.
	 */
	public SectionNode(String sectionLocation, CompilerIssues issues) {
		this(null, null, null, sectionLocation, issues);
	}

	/**
	 * Allows for the creation of {@link SubSection} instances.
	 * 
	 * @param sectionName
	 *            Name of this {@link SectionNode} as a {@link SubSection}.
	 * @param sectionSourceClassName
	 *            Class name of the {@link SectionSource} for this
	 *            {@link SectionNode}.
	 * @param sectionSource
	 *            {@link SectionSource} instance. May be <code>null</code>.
	 * @param sectionLocation
	 *            Location of this {@link SectionNode}.
	 * @param issues
	 *            {@link CompilerIssues}.
	 */
	private SectionNode(String sectionName, String sectionSourceClassName,
			SectionSource sectionSource, String sectionLocation,
			CompilerIssues issues) {
		this.sectionName = sectionName;
		this.sectionSourceClassName = sectionSourceClassName;
		this.sectionSource = sectionSource;
		this.sectionLocation = sectionLocation;
		this.issues = issues;
	}

	/**
	 * Adds an issue regarding the {@link OfficeSection} being built.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 */
	private void addIssue(String issueDescription) {
		this.issues.addIssue(LocationType.SECTION, this.sectionLocation, null,
				null, issueDescription);
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

	/*
	 * ===================== SubSection =================================
	 */

	@Override
	public String getSubSectionName() {
		return this.sectionName;
	}

	@Override
	public void addProperty(String name, String value) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement SubSection.addProperty");
	}

	@Override
	public SubSectionInput getSubSectionInput(String inputName) {
		// Obtain and return the section input for the name
		SectionInputNode input = this.inputs.get(inputName);
		if (input == null) {
			// Add the input
			input = new SectionInputNode(inputName);
			this.inputs.put(inputName, input);
		}
		return input;
	}

	@Override
	public SubSectionOutput getSubSectionOutput(String outputName) {
		// Obtain and return the section output for the name
		SectionOutputNode output = this.outputs.get(outputName);
		if (output == null) {
			// Add the output
			output = new SectionOutputNode(outputName);
			this.outputs.put(outputName, output);
		}
		return output;
	}

	@Override
	public SubSectionObject getSubSectionObject(String objectName) {
		// Obtain and return the section object for the name
		SectionObjectNode object = this.objects.get(objectName);
		if (object == null) {
			// Add the object
			object = new SectionObjectNode(objectName);
			this.objects.put(objectName, object);
		}
		return object;
	}

	/*
	 * ======================== SectionTypeBuilder =============================
	 */

	@Override
	public SectionInput addInput(String inputName, String parameterType) {
		// Obtain and return the section input for the name
		SectionInputNode input = this.inputs.get(inputName);
		if (input == null) {
			// Add the input
			input = new SectionInputNode(inputName, parameterType);
			this.inputs.put(inputName, input);
			this.inputTypes.add(input);
		} else {
			// Added but determine if requires initialising
			if (!input.isInitialised()) {
				// Initialise as not yet initialised
				input.initialise(parameterType);
			} else {
				// Input already added and initialised
				this.addIssue("Input " + inputName + " already added");
			}
		}
		return input;
	}

	@Override
	public SectionOutput addOutput(String outputName, String argumentType,
			boolean isEscalationOnly) {
		// Obtain and return the section output for the name
		SectionOutputNode output = this.outputs.get(outputName);
		if (output == null) {
			// Add the output
			output = new SectionOutputNode(outputName, argumentType,
					isEscalationOnly);
			this.outputs.put(outputName, output);
			this.outputTypes.add(output);
		} else {
			// Added but determine if requires initialising
			if (!output.isInitialised()) {
				// Initialise as not yet initialised
				output.initialise(argumentType, isEscalationOnly);
			} else {
				// Output already added and initialised
				this.addIssue("Output " + outputName + " already added");
			}
		}
		return output;
	}

	@Override
	public SectionObject addObject(String objectName, String objectType) {
		// Obtain and return the section object for the name
		SectionObjectNode object = this.objects.get(objectName);
		if (object == null) {
			// Add the object
			object = new SectionObjectNode(objectName, objectType);
			this.objects.put(objectName, object);
			this.objectTypes.add(object);
		} else {
			// Added but determine if requires initialising
			if (!object.isInitialised()) {
				// Initialise as not yet initialised
				object.initialise(objectType);
			} else {
				// Object already added and initialised
				this.addIssue("Object " + objectName + " already added");
			}
		}
		return object;
	}

	@Override
	public SectionWork addWork(String workName, String workSourceClassName) {
		return this.addWork(workName, workSourceClassName, null);
	}

	@Override
	public SectionWork addWork(String workName, WorkSource<?> workSource) {
		return this.addWork(workName, workSource.getClass().getName(),
				workSource);
	}

	/**
	 * Adds a {@link SectionWork}.
	 * 
	 * @param workName
	 *            Name of the {@link SectionWork}.
	 * @param workSourceClassName
	 *            Class name of the {@link WorkSource}.
	 * @param workSource
	 *            {@link WorkSource} instance. May be <code>null</code>.
	 * @return {@link SectionWork}.
	 */
	private SectionWork addWork(String workName, String workSourceClassName,
			WorkSource<?> workSource) {
		// Obtain and return the section work for the name
		WorkNode work = this.workNodes.get(workName);
		if (work == null) {
			// Add the section work
			work = new WorkNode(workName, workSourceClassName, workSource,
					this.sectionLocation, this.taskNodes, this.issues);
			this.workNodes.put(workName, work);
		} else {
			// Section work already added
			this.addIssue("Section work " + workName + " already added");
		}
		return work;
	}

	@Override
	public SubSection addSubSection(String subSectionName,
			String sectionSourceClassName, String location) {
		return this.addSubSection(subSectionName, sectionSourceClassName, null,
				location);
	}

	@Override
	public SubSection addSubSection(String subSectionName,
			SectionSource sectionSource, String location) {
		return this.addSubSection(subSectionName, sectionSource.getClass()
				.getName(), sectionSource, location);
	}

	/**
	 * Adds a {@link SubSection}.
	 * 
	 * @param subSectionName
	 *            Name of the {@link SubSection}.
	 * @param sectionSourceClassName
	 *            Class name of the {@link SectionSource}.
	 * @param sectionSource
	 *            {@link SectionSource} instance. May be <code>null</code>.
	 * @param location
	 *            Location of the {@link SubSection}.
	 * @return {@link SubSection}.
	 */
	private SubSection addSubSection(String subSectionName,
			String sectionSourceClassName, SectionSource sectionSource,
			String location) {
		// Obtain and return the sub section for the name
		SectionNode subSection = this.subSections.get(subSectionName);
		if (subSection == null) {
			// Add the sub section
			subSection = new SectionNode(subSectionName,
					sectionSourceClassName, sectionSource, location,
					this.issues);
			this.subSections.put(subSectionName, subSection);
		} else {
			// Sub section already added
			this.addIssue("Sub section " + subSectionName + " already added");
		}
		return subSection;
	}

}