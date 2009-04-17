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
package net.officefloor.compile.impl.structure;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.section.SectionSourceContextImpl;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.internal.structure.ManagedObjectNode;
import net.officefloor.compile.internal.structure.SectionInputNode;
import net.officefloor.compile.internal.structure.SectionNode;
import net.officefloor.compile.internal.structure.SectionObjectNode;
import net.officefloor.compile.internal.structure.SectionOutputNode;
import net.officefloor.compile.internal.structure.TaskFlowNode;
import net.officefloor.compile.internal.structure.TaskNode;
import net.officefloor.compile.internal.structure.WorkNode;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.office.OfficeInputType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionInputType;
import net.officefloor.compile.section.SectionObjectType;
import net.officefloor.compile.section.SectionOutputType;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionManagedObject;
import net.officefloor.compile.spi.office.OfficeSectionObject;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.spi.office.OfficeSubSection;
import net.officefloor.compile.spi.office.OfficeTask;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.section.ManagedObjectDependency;
import net.officefloor.compile.spi.section.ManagedObjectFlow;
import net.officefloor.compile.spi.section.SectionBuilder;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionManagedObject;
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
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.model.repository.ConfigurationContext;

/**
 * {@link SectionNode} implementation.
 * 
 * @author Daniel
 */
public class SectionNodeImpl extends AbstractNode implements SectionNode {

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
	 * {@link SectionOutput} instances by their names.
	 */
	private final Map<String, SectionOutputNode> outputs = new HashMap<String, SectionOutputNode>();

	/**
	 * {@link SectionObject} instances by their names.
	 */
	private final Map<String, SectionObjectNode> objects = new HashMap<String, SectionObjectNode>();

	/**
	 * {@link ManagedObjectNode} instances by their {@link SectionManagedObject}
	 * names.
	 */
	private final Map<String, ManagedObjectNode> managedObjectNodes = new HashMap<String, ManagedObjectNode>();

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
	 * Allows for loading the {@link SectionType}.
	 * 
	 * @param sectionLocation
	 *            Location of the {@link OfficeSection} being built by this
	 *            {@link SectionBuilder}.
	 * @param issues
	 *            {@link CompilerIssues} to report issues.
	 */
	public SectionNodeImpl(String sectionLocation, CompilerIssues issues) {
		this.sectionName = null;
		this.sectionSourceClassName = null;
		this.propertyList = new PropertyListImpl();
		this.sectionLocation = sectionLocation;
		this.issues = issues;
	}

	/**
	 * Allows for loading a {@link OfficeSection}.
	 * 
	 * @param sectionName
	 *            Name of this {@link OfficeSection}.
	 * @param sectionSource
	 *            {@link SectionSource}.
	 * @param propertyList
	 *            {@link PropertyList}.
	 * @param sectionLocation
	 *            Location of this {@link SectionNode}.
	 * @param issues
	 *            {@link CompilerIssues}.
	 */
	public SectionNodeImpl(String sectionName, SectionSource sectionSource,
			PropertyList propertyList, String sectionLocation,
			CompilerIssues issues) {
		this.sectionName = sectionName;
		this.sectionSourceClassName = sectionSource.getClass().getName();
		this.sectionSource = sectionSource;
		this.propertyList = propertyList;
		this.sectionLocation = sectionLocation;
		this.issues = issues;
	}

	/**
	 * Allows for adding an {@link OfficeSection} to an {@link Office}.
	 * 
	 * @param sectionName
	 *            Name of the {@link OfficeSection}.
	 * @param sectionSourceClassName
	 *            Class name of the {@link SectionSource} for this
	 *            {@link OfficeSection}.
	 * @param propertyList
	 *            {@link PropertyList}.
	 * @param sectionLocation
	 *            Location of the {@link OfficeSection}.
	 * @param issues
	 *            {@link CompilerIssues}.
	 */
	public SectionNodeImpl(String sectionName, String sectionSourceClassName,
			PropertyList propertyList, String sectionLocation,
			CompilerIssues issues) {
		this.sectionName = sectionName;
		this.sectionSourceClassName = sectionSourceClassName;
		this.propertyList = propertyList;
		this.sectionLocation = sectionLocation;
		this.issues = issues;
	}

	/**
	 * Allows for obtaining the {@link DeployedOfficeInput}.
	 * 
	 * @param sectionName
	 *            Name of the {@link OfficeSection}.
	 */
	public SectionNodeImpl(String sectionName) {
		this.sectionName = sectionName;
		this.sectionSourceClassName = null;
		this.propertyList = new PropertyListImpl();
		this.sectionLocation = null;
		this.issues = null;
	}

	/**
	 * Allows for the creation of {@link SubSection} instances.
	 * 
	 * @param sectionName
	 *            Name of this {@link SectionNode} as a {@link SubSection}.
	 * @param sectionSourceClassName
	 *            Class name of the {@link SectionSource} for this
	 *            {@link SubSection}.
	 * @param sectionSource
	 *            {@link SectionSource} instance. May be <code>null</code>.
	 * @param sectionLocation
	 *            Location of this {@link SubSection}.
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
	 * @param cause
	 *            Cause of the issue.
	 */
	private void addIssue(String issueDescription, Throwable cause) {
		this.issues.addIssue(LocationType.SECTION, this.sectionLocation, null,
				null, issueDescription, cause);
	}

	/*
	 * =================== AbstractNode ==================================
	 */

	@Override
	protected void addIssue(String issueDescription) {
		this.issues.addIssue(LocationType.SECTION, this.sectionLocation, null,
				null, issueDescription);
	}

	/*
	 * ======================= SectionNode =================================
	 */

	@Override
	public void loadSection(String officeLocation,
			ConfigurationContext configurationContext, ClassLoader classLoader) {

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
			subSection.loadSection(officeLocation, configurationContext,
					classLoader);
		}

		// Load managed objects (require supported extension interfaces)
		for (ManagedObjectNode managedObject : this.managedObjectNodes.values()) {
			managedObject.addOfficeContext(officeLocation);
			managedObject.loadManagedObjectMetaData(configurationContext,
					classLoader);
		}

		// Add the office context for the tasks
		for (TaskNode task : this.taskNodes.values()) {
			task.addOfficeContext(officeLocation);
		}

		// Add the office context for the section outputs
		for (SectionOutputNode output : this.outputs.values()) {
			output.addOfficeContext(officeLocation);
		}

		// Add the office context for the section objects
		for (SectionObjectNode object : this.objects.values()) {
			object.addOfficeContext(officeLocation);
		}
	}

	@Override
	public OfficeInputType[] getOfficeInputTypes() {
		return this.inputs.values().toArray(new OfficeInputType[0]);
	}

	@Override
	public DeployedOfficeInput getDeployedOfficeInput(String inputName) {
		// Obtain and return the section input
		SectionInputNode input = this.inputs.get(inputName);
		if (input == null) {
			// Add the section input
			input = new SectionInputNodeImpl(inputName, this,
					this.sectionLocation, this.issues);
			this.inputs.put(inputName, input);
		}
		return input;
	}

	/*
	 * ===================== SectionType ===================================
	 */

	@Override
	public SectionInputType[] getSectionInputTypes() {
		// Return the listing of input types sorted by name
		SectionInputType[] inputTypes = this.inputs.values().toArray(
				new SectionInputType[0]);
		Arrays.sort(inputTypes, new Comparator<SectionInputType>() {
			@Override
			public int compare(SectionInputType a, SectionInputType b) {
				return a.getSectionInputName().compareTo(
						b.getSectionInputName());
			}
		});
		return inputTypes;
	}

	@Override
	public SectionOutputType[] getSectionOutputTypes() {
		// Return the listing of output types sorted by name
		SectionOutputType[] outputTypes = this.outputs.values().toArray(
				new SectionOutputType[0]);
		Arrays.sort(outputTypes, new Comparator<SectionOutputType>() {
			@Override
			public int compare(SectionOutputType a, SectionOutputType b) {
				return a.getSectionOutputName().compareTo(
						b.getSectionOutputName());
			}
		});
		return outputTypes;
	}

	@Override
	public SectionObjectType[] getSectionObjectTypes() {
		// Return the listing of object types sorted by name
		SectionObjectType[] objectTypes = this.objects.values().toArray(
				new SectionObjectType[0]);
		Arrays.sort(objectTypes, new Comparator<SectionObjectType>() {
			@Override
			public int compare(SectionObjectType a, SectionObjectType b) {
				return a.getSectionObjectName().compareTo(
						b.getSectionObjectName());
			}
		});
		return objectTypes;
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
		this.propertyList.addProperty(name).setValue(value);
	}

	@Override
	public SubSectionInput getSubSectionInput(String inputName) {
		// Obtain and return the section input for the name
		SectionInputNode input = this.inputs.get(inputName);
		if (input == null) {
			// Add the input
			input = new SectionInputNodeImpl(inputName, this,
					this.sectionLocation, this.issues);
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
			input = new SectionInputNodeImpl(inputName, this, parameterType,
					this.sectionLocation, this.issues);
			this.inputs.put(inputName, input);
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
	public SectionManagedObject addManagedObject(String managedObjectName,
			String managedObjectSourceClassName) {
		// Obtain and return the section managed object for the name
		ManagedObjectNode managedObject = this.managedObjectNodes
				.get(managedObjectName);
		if (managedObject == null) {
			// Add the section managed object
			managedObject = new ManagedObjectNodeImpl(managedObjectName,
					managedObjectSourceClassName, this.sectionLocation,
					this.issues);
			this.managedObjectNodes.put(managedObjectName, managedObject);
		} else {
			// Section managed object already added
			this.addIssue("Section managed object " + managedObjectName
					+ " already added");
		}
		return managedObject;
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

	@Override
	public void link(ManagedObjectFlow managedObjectFlow, SectionTask task) {
		this.linkFlow(managedObjectFlow, task);
	}

	@Override
	public void link(ManagedObjectFlow managedObjectFlow,
			SubSectionInput subSectionInput) {
		this.linkFlow(managedObjectFlow, subSectionInput);
	}

	@Override
	public void link(ManagedObjectFlow managedObjectFlow,
			SectionOutput sectionOutput) {
		this.linkFlow(managedObjectFlow, sectionOutput);
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

	@Override
	public void link(TaskObject taskObject,
			SectionManagedObject sectionManagedObject) {
		this.linkObject(taskObject, sectionManagedObject);
	}

	@Override
	public void link(SubSectionObject subSectionObject,
			SectionManagedObject sectionManagedObject) {
		this.linkObject(subSectionObject, sectionManagedObject);
	}

	@Override
	public void link(ManagedObjectDependency dependency,
			SectionObject sectionObject) {
		this.linkObject(dependency, sectionObject);
	}

	@Override
	public void link(ManagedObjectDependency dependency,
			SectionManagedObject sectionManagedObject) {
		this.linkObject(dependency, sectionManagedObject);
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
		// Return the sub sections sorted by name
		OfficeSubSection[] subSections = this.subSections.values().toArray(
				new OfficeSubSection[0]);
		Arrays.sort(subSections, new Comparator<OfficeSubSection>() {
			@Override
			public int compare(OfficeSubSection a, OfficeSubSection b) {
				return a.getOfficeSectionName().compareTo(
						b.getOfficeSectionName());
			}
		});
		return subSections;
	}

	@Override
	public OfficeSectionInput[] getOfficeSectionInputs() {
		// Return the section inputs sorted by name
		OfficeSectionInput[] sectionInputs = this.inputs.values().toArray(
				new OfficeSectionInput[0]);
		Arrays.sort(sectionInputs, new Comparator<OfficeSectionInput>() {
			@Override
			public int compare(OfficeSectionInput a, OfficeSectionInput b) {
				return a.getOfficeSectionInputName().compareTo(
						b.getOfficeSectionInputName());
			}
		});
		return sectionInputs;
	}

	@Override
	public OfficeSectionOutput[] getOfficeSectionOutputs() {
		// Return the section outputs sorted by name
		OfficeSectionOutput[] sectionOutputs = this.outputs.values().toArray(
				new OfficeSectionOutput[0]);
		Arrays.sort(sectionOutputs, new Comparator<OfficeSectionOutput>() {
			@Override
			public int compare(OfficeSectionOutput a, OfficeSectionOutput b) {
				return a.getOfficeSectionOutputName().compareTo(
						b.getOfficeSectionOutputName());
			}
		});
		return sectionOutputs;
	}

	@Override
	public OfficeSectionObject[] getOfficeSectionObjects() {
		// Return the section objects sorted by name
		OfficeSectionObject[] sectionObjects = this.objects.values().toArray(
				new OfficeSectionObject[0]);
		Arrays.sort(sectionObjects, new Comparator<OfficeSectionObject>() {
			@Override
			public int compare(OfficeSectionObject a, OfficeSectionObject b) {
				return a.getOfficeSectionObjectName().compareTo(
						b.getOfficeSectionObjectName());
			}
		});
		return sectionObjects;
	}

	@Override
	public OfficeSectionManagedObject[] getOfficeSectionManagedObjects() {
		// Return the section managed objects sorted by name
		OfficeSectionManagedObject[] sectionMos = this.managedObjectNodes
				.values().toArray(new OfficeSectionManagedObject[0]);
		Arrays.sort(sectionMos, new Comparator<OfficeSectionManagedObject>() {
			@Override
			public int compare(OfficeSectionManagedObject a,
					OfficeSectionManagedObject b) {
				return a.getOfficeSectionManagedObjectName().compareTo(
						b.getOfficeSectionManagedObjectName());
			}
		});
		return sectionMos;
	}

	@Override
	public OfficeTask[] getOfficeTasks() {
		// Return the office tasks sorted by name
		OfficeTask[] officeTasks = this.taskNodes.values().toArray(
				new OfficeTask[0]);
		Arrays.sort(officeTasks, new Comparator<OfficeTask>() {
			@Override
			public int compare(OfficeTask a, OfficeTask b) {
				return a.getOfficeTaskName().compareTo(b.getOfficeTaskName());
			}
		});
		return officeTasks;
	}

}