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

import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.internal.structure.LinkFlowNode;
import net.officefloor.compile.internal.structure.LinkObjectNode;
import net.officefloor.compile.internal.structure.SectionInputNode;
import net.officefloor.compile.internal.structure.SectionNode;
import net.officefloor.compile.internal.structure.SectionObjectNode;
import net.officefloor.compile.internal.structure.SectionOutputNode;
import net.officefloor.compile.internal.structure.TaskFlowNode;
import net.officefloor.compile.internal.structure.TaskNode;
import net.officefloor.compile.internal.structure.WorkNode;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionInputType;
import net.officefloor.compile.section.SectionObjectType;
import net.officefloor.compile.section.SectionOutputType;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.spi.office.OfficeSubSection;
import net.officefloor.compile.spi.office.OfficeTask;
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
import net.officefloor.compile.spi.section.TaskFlow;
import net.officefloor.compile.spi.section.TaskObject;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.model.repository.ConfigurationContext;

/**
 * {@link SectionNode} implementation.
 * 
 * @author Daniel
 */
public class SectionNodeImpl implements SectionNode {

	/**
	 * Name of this {@link SubSection}.
	 */
	private final String sectionName;

	/**
	 * Class name of the {@link SectionSource}.
	 */
	private final String sectionSourceClassName;

	/**
	 * {@link PropertyList} to source this {@link OfficeSection}.
	 */
	private final PropertyList propertyList;

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
	public SectionNodeImpl(String sectionLocation, CompilerIssues issues) {
		this(null, null, null, sectionLocation, issues);
	}

	/**
	 * Allows for the creation of the top level {@link OfficeSection}.
	 * 
	 * @param sectionSource
	 *            {@link SectionSource}.
	 * @param propertyList
	 *            {@link PropertyList}.
	 * @param sectionLocation
	 *            Location of this {@link SectionNode}.
	 * @param issues
	 *            {@link CompilerIssues}.
	 */
	public SectionNodeImpl(SectionSource sectionSource,
			PropertyList propertyList, String sectionLocation,
			CompilerIssues issues) {
		this.sectionName = null;
		this.sectionSourceClassName = sectionSource.getClass().getName();
		this.sectionSource = sectionSource;
		this.propertyList = propertyList;
		this.sectionLocation = sectionLocation;
		this.issues = issues;
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
	private SectionNodeImpl(String sectionName, String sectionSourceClassName,
			SectionSource sectionSource, String sectionLocation,
			CompilerIssues issues) {
		this.sectionName = sectionName;
		this.sectionSourceClassName = sectionSourceClassName;
		this.sectionSource = sectionSource;
		this.propertyList = new PropertyListImpl();
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

	/**
	 * Adds an issue regarding the {@link OfficeSection} being built.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 * @param cause
	 *            Cause of the issue.
	 */
	private void addIssue(String issueDescription, Throwable cause) {
		this.issues.addIssue(LocationType.SECTION, this.sectionLocation, null,
				null, issueDescription, cause);
	}

	/*
	 * ======================= SectionNode =================================
	 */

	@Override
	public void loadSection(ConfigurationContext configurationContext,
			ClassLoader classLoader) {

		// Ensure have instance of section source
		if (this.sectionSource == null) {
			// Obtain the section source class
			Class<? extends SectionSource> sectionSourceClass = CompileUtil
					.obtainClass(this.sectionSourceClassName,
							SectionSource.class, classLoader,
							LocationType.SECTION, this.sectionLocation, null,
							null, this.issues);
			if (sectionSourceClass == null) {
				return; // must have section source class
			}

			// Instantiate an instance of the section source
			this.sectionSource = CompileUtil.newInstance(sectionSourceClass,
					SectionSource.class, LocationType.SECTION,
					this.sectionLocation, null, null, issues);
			if (this.sectionSource == null) {
				return; // must instantiate section source
			}
		}

		// Create the section source context
		SectionSourceContext context = new SectionSourceContextImpl(
				this.sectionLocation, configurationContext, this.propertyList,
				classLoader);

		try {
			// Source the section
			this.sectionSource.sourceSection(this, context);

		} catch (Throwable ex) {
			// Indicate failure to source section
			this.addIssue("Faild to source "
					+ OfficeSection.class.getSimpleName(), ex);
			return; // can not load sub section as section load failure
		}

		// Load the sub sections
		for (SectionNode subSection : this.subSections.values()) {
			subSection.loadSection(configurationContext, classLoader);
		}
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
			input = new SectionInputNodeImpl(inputName, this.sectionLocation,
					this.issues);
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
			output = new SectionOutputNodeImpl(outputName,
					this.sectionLocation, this.issues);
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
			object = new SectionObjectNodeImpl(objectName,
					this.sectionLocation, this.issues);
			this.objects.put(objectName, object);
		}
		return object;
	}

	/*
	 * ======================== SectionTypeBuilder =============================
	 */

	@Override
	public SectionInput addSectionInput(String inputName, String parameterType) {
		// Obtain and return the section input for the name
		SectionInputNode input = this.inputs.get(inputName);
		if (input == null) {
			// Add the input
			input = new SectionInputNodeImpl(inputName, parameterType,
					this.sectionLocation, this.issues);
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
	public SectionOutput addSectionOutput(String outputName,
			String argumentType, boolean isEscalationOnly) {
		// Obtain and return the section output for the name
		SectionOutputNode output = this.outputs.get(outputName);
		if (output == null) {
			// Add the output
			output = new SectionOutputNodeImpl(outputName, argumentType,
					isEscalationOnly, this.sectionLocation, this.issues);
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
	public SectionObject addSectionObject(String objectName, String objectType) {
		// Obtain and return the section object for the name
		SectionObjectNode object = this.objects.get(objectName);
		if (object == null) {
			// Add the object
			object = new SectionObjectNodeImpl(objectName, objectType,
					this.sectionLocation, this.issues);
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
			work = new WorkNodeImpl(workName, workSourceClassName, workSource,
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
			subSection = new SectionNodeImpl(subSectionName,
					sectionSourceClassName, sectionSource, location,
					this.issues);
			this.subSections.put(subSectionName, subSection);
		} else {
			// Sub section already added
			this.addIssue("Sub section " + subSectionName + " already added");
		}
		return subSection;
	}

	@Override
	public void link(SectionInput sectionInput, SectionTask task) {
		this.linkFlow(sectionInput, task);
	}

	@Override
	public void link(SectionInput sectionInput, SubSectionInput subSectionInput) {
		this.linkFlow(sectionInput, subSectionInput);
	}

	@Override
	public void link(SectionInput sectionInput, SectionOutput sectionOutput) {
		this.linkFlow(sectionInput, sectionOutput);
	}

	@Override
	public void link(TaskFlow taskFlow, SectionTask task,
			FlowInstigationStrategyEnum instigationStrategy) {
		if (this.linkFlow(taskFlow, task)) {
			// Linked so specify instigation strategy
			this.loadFlowInstigationStrategy(taskFlow, instigationStrategy);
		}
	}

	@Override
	public void link(TaskFlow taskFlow, SubSectionInput subSectionInput,
			FlowInstigationStrategyEnum instigationStrategy) {
		if (this.linkFlow(taskFlow, subSectionInput)) {
			// Linked so specify instigation strategy
			this.loadFlowInstigationStrategy(taskFlow, instigationStrategy);
		}
	}

	@Override
	public void link(TaskFlow taskFlow, SectionOutput sectionOutput,
			FlowInstigationStrategyEnum instigationStrategy) {
		if (this.linkFlow(taskFlow, sectionOutput)) {
			// Linked so specify instigation strategy
			this.loadFlowInstigationStrategy(taskFlow, instigationStrategy);
		}
	}

	@Override
	public void link(SectionTask task, SectionTask nextTask) {
		this.linkFlow(task, nextTask);
	}

	@Override
	public void link(SectionTask task, SubSectionInput subSectionInput) {
		this.linkFlow(task, subSectionInput);
	}

	@Override
	public void link(SectionTask task, SectionOutput sectionOutput) {
		this.linkFlow(task, sectionOutput);
	}

	@Override
	public void link(SubSectionOutput subSectionOutput, SectionTask task) {
		this.linkFlow(subSectionOutput, task);
	}

	@Override
	public void link(SubSectionOutput subSectionOutput,
			SubSectionInput subSectionInput) {
		this.linkFlow(subSectionOutput, subSectionInput);
	}

	@Override
	public void link(SubSectionOutput subSectionOutput,
			SectionOutput sectionOutput) {
		this.linkFlow(subSectionOutput, sectionOutput);
	}

	/**
	 * Ensures both inputs are a {@link LinkFlowNode} and if so links them.
	 * 
	 * @param linkSource
	 *            Source {@link LinkFlowNode}.
	 * @param linkTarget
	 *            Target {@link LinkFlowNode}.
	 * @return <code>true</code> if linked.
	 */
	private boolean linkFlow(Object linkSource, Object linkTarget) {

		// Ensure the link source is link flow node
		if (!(linkSource instanceof LinkFlowNode)) {
			this.addIssue("Invalid link source: "
					+ linkSource
					+ " ["
					+ (linkSource == null ? null : linkSource.getClass()
							.getName()) + "]");
			return false; // can not link
		}

		// Ensure the link target is link flow node
		if (!(linkTarget instanceof LinkFlowNode)) {
			this.addIssue("Invalid link target: "
					+ linkTarget
					+ " ["
					+ (linkTarget == null ? null : linkTarget.getClass()
							.getName()
							+ "]"));
			return false; // can not link
		}

		// Link the nodes together
		return ((LinkFlowNode) linkSource)
				.linkFlowNode((LinkFlowNode) linkTarget);
	}

	/**
	 * Loads the {@link FlowInstigationStrategyEnum} for the {@link TaskFlow}.
	 * 
	 * @param taskFlow
	 *            {@link TaskFlow}.
	 * @param instigationStrategy
	 *            {@link FlowInstigationStrategyEnum}.
	 */
	private void loadFlowInstigationStrategy(TaskFlow taskFlow,
			FlowInstigationStrategyEnum instigationStrategy) {

		// Ensure a task flow node
		if (!(taskFlow instanceof TaskFlowNode)) {
			this.addIssue("Invalid task flow: " + taskFlow + " ["
					+ (taskFlow == null ? null : taskFlow.getClass().getName())
					+ "]");
			return; // can not load instigation strategy
		}

		// Load the instigation strategy
		((TaskFlowNode) taskFlow)
				.setFlowInstigationStrategy(instigationStrategy);
	}

	@Override
	public void link(TaskObject taskObject, SectionObject sectionObject) {
		this.linkObject(taskObject, sectionObject);
	}

	@Override
	public void link(SubSectionObject subSectionObject,
			SectionObject sectionObject) {
		this.linkObject(subSectionObject, sectionObject);
	}

	/**
	 * Ensures both inputs are a {@link LinkObjectNode} and if so links them.
	 * 
	 * @param linkSource
	 *            Source {@link LinkObjectNode}.
	 * @param linkTarget
	 *            Target {@link LinkObjectNode}.
	 * @return <code>true</code> if linked.
	 */
	private boolean linkObject(Object linkSource, Object linkTarget) {

		// Ensure the link source is link object node
		if (!(linkSource instanceof LinkObjectNode)) {
			this.addIssue("Invalid link source: "
					+ linkSource
					+ " ["
					+ (linkSource == null ? null : linkSource.getClass()
							.getName()) + "]");
			return false; // can not link
		}

		// Ensure the link target is link object node
		if (!(linkTarget instanceof LinkObjectNode)) {
			this.addIssue("Invalid link target: "
					+ linkTarget
					+ " ["
					+ (linkTarget == null ? null : linkTarget.getClass()
							.getName()
							+ "]"));
			return false; // can not link
		}

		// Link the nodes together
		return ((LinkObjectNode) linkSource)
				.linkObjectNode((LinkObjectNode) linkTarget);
	}

	/*
	 * ==================== OfficeSection =================================
	 */

	@Override
	public String getOfficeSectionName() {
		return this.sectionName;
	}

	@Override
	public OfficeSubSection[] getOfficeSubSections() {
		return this.subSections.values().toArray(new OfficeSubSection[0]);
	}

	@Override
	public OfficeSectionInput[] getOfficeSectionInputs() {
		return this.inputs.values().toArray(new OfficeSectionInput[0]);
	}

	@Override
	public OfficeSectionOutput[] getOfficeSectionOutputs() {
		return this.outputs.values().toArray(new OfficeSectionOutput[0]);
	}

	@Override
	public OfficeTask[] getOfficeTasks() {
		return this.taskNodes.values().toArray(new OfficeTask[0]);
	}

}