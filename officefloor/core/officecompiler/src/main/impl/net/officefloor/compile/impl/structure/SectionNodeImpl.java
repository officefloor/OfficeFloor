/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.compile.impl.structure;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.compile.impl.office.OfficeAvailableSectionInputTypeImpl;
import net.officefloor.compile.impl.section.OfficeSectionTypeImpl;
import net.officefloor.compile.impl.section.SectionSourceContextImpl;
import net.officefloor.compile.impl.section.SectionTypeImpl;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.impl.util.LoadTypeError;
import net.officefloor.compile.internal.structure.GovernanceNode;
import net.officefloor.compile.internal.structure.ManagedObjectNode;
import net.officefloor.compile.internal.structure.ManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeBindings;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.internal.structure.SectionInputNode;
import net.officefloor.compile.internal.structure.SectionNode;
import net.officefloor.compile.internal.structure.SectionObjectNode;
import net.officefloor.compile.internal.structure.SectionOutputNode;
import net.officefloor.compile.internal.structure.TaskFlowNode;
import net.officefloor.compile.internal.structure.TaskNode;
import net.officefloor.compile.internal.structure.WorkNode;
import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.office.OfficeAvailableSectionInputType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.OfficeSectionInputType;
import net.officefloor.compile.section.OfficeSectionManagedObjectType;
import net.officefloor.compile.section.OfficeSectionObjectType;
import net.officefloor.compile.section.OfficeSectionOutputType;
import net.officefloor.compile.section.OfficeSectionType;
import net.officefloor.compile.section.OfficeSubSectionType;
import net.officefloor.compile.section.OfficeTaskType;
import net.officefloor.compile.section.SectionInputType;
import net.officefloor.compile.section.SectionObjectType;
import net.officefloor.compile.section.SectionOutputType;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.office.OfficeGovernance;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionManagedObject;
import net.officefloor.compile.spi.office.OfficeSectionManagedObjectSource;
import net.officefloor.compile.spi.office.OfficeSectionObject;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.spi.office.OfficeSectionTask;
import net.officefloor.compile.spi.office.OfficeSubSection;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.section.ManagedObjectDependency;
import net.officefloor.compile.spi.section.ManagedObjectFlow;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.compile.spi.section.SectionManagedObjectSource;
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
import net.officefloor.compile.type.TypeContext;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.source.UnknownClassError;
import net.officefloor.frame.spi.source.UnknownPropertyError;
import net.officefloor.frame.spi.source.UnknownResourceError;

/**
 * {@link SectionNode} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class SectionNodeImpl extends AbstractNode implements SectionNode {

	/**
	 * Name of this {@link SubSection}.
	 */
	private final String sectionName;

	/**
	 * {@link PropertyList} to source this {@link OfficeSection}.
	 */
	private final PropertyList propertyList;

	/**
	 * Parent {@link OfficeSection} containing this {@link OfficeSection}.
	 */
	private final SectionNode parentSection;

	/**
	 * {@link OfficeNode} containing this {@link SectionNode}.
	 */
	private final OfficeNode office;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * {@link InitialisedState} for this {@link SectionNode}.
	 */
	private InitialisedState state;

	/**
	 * Initialised state of the {@link SectionNode}.
	 */
	private class InitialisedState {

		/**
		 * Class name of the {@link SectionSource}.
		 */
		private final String sectionSourceClassName;

		/**
		 * {@link SectionSource} for this {@link SectionNode}.
		 */
		private final SectionSource sectionSource;

		/**
		 * Location of the {@link OfficeSection} being built by this
		 * {@link SectionDesigner}.
		 */
		private final String sectionLocation;

		/**
		 * Initialise the state.
		 * 
		 * @param sectionSourceClassName
		 *            Class name of the {@link SectionSource}.
		 * @param sectionSource
		 *            {@link SectionSource} for this {@link SectionNode}.
		 * @param sectionLocation
		 *            Location of the {@link OfficeSection} being built by this
		 *            {@link SectionDesigner}.
		 */
		private InitialisedState(String sectionSourceClassName, SectionSource sectionSource, String sectionLocation) {
			this.sectionSourceClassName = sectionSourceClassName;
			this.sectionSource = sectionSource;
			this.sectionLocation = sectionLocation;
		}
	}

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
	 * {@link ManagedObjectSourceNode} instances by their
	 * {@link SectionManagedObjectSource} names.
	 */
	private final Map<String, ManagedObjectSourceNode> managedObjectSourceNodes = new HashMap<String, ManagedObjectSourceNode>();

	/**
	 * {@link ManagedObjectNode} instances by their {@link SectionManagedObject}
	 * name.
	 */
	private final Map<String, ManagedObjectNode> managedObjects = new HashMap<String, ManagedObjectNode>();

	/**
	 * Listing of {@link OfficeGovernance} instances providing
	 * {@link Governance} over this {@link SectionNode}.
	 */
	private final List<GovernanceNode> governances = new LinkedList<GovernanceNode>();

	/**
	 * {@link Node} instances by their {@link SectionWork} names.
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
	 * Initialises this {@link SectionNode} with the basic information.
	 * 
	 * @param sectionName
	 *            Name of this {@link OfficeSection}.
	 * @param parentSection
	 *            Optional parent {@link SectionNode}. May be <code>null</code>.
	 * @param office
	 *            {@link Office} containing the {@link OfficeSection}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public SectionNodeImpl(String sectionName, SectionNode parentSection, OfficeNode office, NodeContext context) {
		this.sectionName = sectionName;
		this.parentSection = parentSection;
		this.office = office;
		this.context = context;

		// Create the additional objects
		this.propertyList = this.context.createPropertyList();
	}

	/*
	 * ======================= Node =================================
	 */

	@Override
	public String getNodeName() {
		return this.sectionName;
	}

	@Override
	public String getNodeType() {
		return TYPE;
	}

	@Override
	public String getLocation() {
		return (this.state == null ? "[NOT INITIALISED]"
				: NodeUtil.getLocation(this.state.sectionSourceClassName, this.state.sectionSource,
						this.state.sectionLocation));
	}

	@Override
	public Node getParentNode() {
		return (this.parentSection != null ? this.parentSection : this.office);
	}

	@Override
	public boolean isInitialised() {
		return (this.state != null);
	}

	@Override
	public void initialise(String sectionSourceClassName, SectionSource sectionSource, String sectionLocation) {
		this.state = NodeUtil.initialise(this, this.context, this.state,
				() -> new InitialisedState(sectionSourceClassName, sectionSource, sectionLocation));
	}

	/*
	 * ======================= TaskRegistry =======================
	 */

	@Override
	public TaskNode getTaskNode(String taskName) {
		return NodeUtil.getNode(taskName, this.taskNodes, () -> this.context.createTaskNode(taskName));
	}

	@Override
	public TaskNode addTaskNode(String taskName, String taskTypeName, WorkNode workNode) {
		return NodeUtil.getInitialisedNode(taskName, this.taskNodes, this.context,
				() -> this.context.createTaskNode(taskName), (task) -> task.initialise(taskTypeName, workNode));
	}

	/*
	 * ======================= MangedObjectRegistry =======================
	 */

	@Override
	public ManagedObjectNode getManagedObjectNode(String managedObjectName) {
		return NodeUtil.getNode(managedObjectName, this.managedObjects,
				() -> this.context.createManagedObjectNode(managedObjectName));
	}

	@Override
	public ManagedObjectNode addManagedObjectNode(String managedObjectName, ManagedObjectScope managedObjectScope,
			ManagedObjectSourceNode managedObjectSourceNode) {
		return NodeUtil.getInitialisedNode(managedObjectName, this.managedObjects, this.context,
				() -> this.context.createManagedObjectNode(managedObjectName),
				(managedObject) -> managedObject.initialise(managedObjectScope, managedObjectSourceNode));
	}

	/*
	 * ======================= SectionNode =================================
	 */

	@Override
	public boolean sourceSection() {

		// Ensure the section is initialised
		if (!this.isInitialised()) {
			this.context.getCompilerIssues().addIssue(this, "Section is not initialised");
			return false; // must be initialised
		}

		// Determine if must instantiate
		SectionSource source = this.state.sectionSource;
		if (source == null) {

			// Obtain the section source class
			Class<? extends SectionSource> sectionSourceClass = this.context
					.getSectionSourceClass(this.state.sectionSourceClassName, this);
			if (sectionSourceClass == null) {
				return false; // must have section source class
			}

			// Obtain the section source
			source = CompileUtil.newInstance(sectionSourceClass, SectionSource.class, this,
					this.context.getCompilerIssues());
			if (source == null) {
				return false; // must have office source
			}
		}

		// Create the section source context
		SectionSourceContext context = new SectionSourceContextImpl(true, this.state.sectionLocation, this.propertyList,
				this, this.context);

		try {
			// Source the section type
			source.sourceSection(this, context);

		} catch (UnknownPropertyError ex) {
			this.addIssue("Missing property '" + ex.getUnknownPropertyName() + "' for "
					+ SectionSource.class.getSimpleName() + " " + source.getClass().getName());
			return false; // must have property

		} catch (UnknownClassError ex) {
			this.addIssue("Can not load class '" + ex.getUnknownClassName() + "' for "
					+ SectionSource.class.getSimpleName() + " " + source.getClass().getName());
			return false; // must have class

		} catch (UnknownResourceError ex) {
			this.addIssue("Can not obtain resource at location '" + ex.getUnknownResourceLocation() + "' for "
					+ SectionSource.class.getSimpleName() + " " + source.getClass().getName());
			return false; // must have resource

		} catch (LoadTypeError ex) {
			ex.addLoadTypeIssue(this, this.context.getCompilerIssues());
			return false; // must not fail in loading types

		} catch (Throwable ex) {
			this.addIssue("Failed to source " + SectionType.class.getSimpleName() + " definition from "
					+ SectionSource.class.getSimpleName() + " " + source.getClass().getName(), ex);
			return false; // must be successful
		}

		// Successfully sourced section
		return true;
	}

	@Override
	public boolean sourceSectionTree() {

		// Source this section
		boolean isSourced = this.sourceSection();
		if (!isSourced) {
			return false;
		}

		// Successful only if all sub sections are also sourced
		return CompileUtil.sourceTree(this.subSections, (subSection) -> subSection.getOfficeSectionName(),
				(subSection) -> subSection.sourceSectionTree());
	}

	@Override
	public SectionType loadSectionType(TypeContext typeContext) {

		// Obtain the listing of input types sorted by name
		SectionInputType[] inputTypes = CompileUtil.loadTypes(this.inputs, (input) -> input.getSectionInputName(),
				(input) -> input.loadSectionInputType(typeContext), SectionInputType[]::new);
		if (inputTypes == null) {
			return null;
		}

		// Obtain the listing of output types sorted by name
		SectionOutputType[] outputTypes = CompileUtil.loadTypes(this.outputs, (output) -> output.getSectionOutputName(),
				(output) -> output.loadSectionOutputType(typeContext), SectionOutputType[]::new);
		if (outputTypes == null) {
			return null;
		}

		// Obtain the listing of object types sorted by name
		SectionObjectType[] objectTypes = CompileUtil.loadTypes(this.objects, (object) -> object.getSectionObjectName(),
				(object) -> object.loadSectionObjectType(typeContext), SectionObjectType[]::new);
		if (objectTypes == null) {
			return null;
		}

		// Create and return the section type
		return new SectionTypeImpl(inputTypes, outputTypes, objectTypes);
	}

	@Override
	public OfficeSectionType loadOfficeSectionType(TypeContext typeContext) {

		// Load the section type
		SectionType sectionType = this.loadSectionType(typeContext);
		if (sectionType == null) {
			return null; // must load section type
		}

		// Obtain the section inputs
		OfficeSectionInputType[] inputTypes = CompileUtil.loadTypes(this.inputs,
				(input) -> input.getOfficeSectionInputName(), (input) -> input.loadOfficeSectionInputType(typeContext),
				OfficeSectionInputType[]::new);
		if (inputTypes == null) {
			return null; // must load types
		}

		// Add the office context for the section outputs
		OfficeSectionOutputType[] outputTypes = CompileUtil.loadTypes(this.outputs,
				(output) -> output.getOfficeSectionOutputName(),
				(output) -> output.loadOfficeSectionOutputType(typeContext), OfficeSectionOutputType[]::new);
		if (outputTypes == null) {
			return null; // must load types
		}

		// Add the office context for the section objects
		OfficeSectionObjectType[] objectTypes = CompileUtil.loadTypes(this.objects,
				(object) -> object.getOfficeSectionObjectName(),
				(object) -> object.loadOfficeSectionObjectType(typeContext), OfficeSectionObjectType[]::new);
		if (objectTypes == null) {
			return null; // must load types
		}

		// Create the office section type
		OfficeSectionTypeImpl officeSectionType = new OfficeSectionTypeImpl(this.sectionName, inputTypes, outputTypes,
				objectTypes);
		boolean isInitialised = this.initialiseSubSectionState(officeSectionType, null, typeContext);
		if (!isInitialised) {
			return null; // must be initialised
		}

		// Return the type
		return officeSectionType;
	}

	@Override
	public OfficeSectionType loadOfficeSubSectionType(OfficeSubSectionType parentSectionType, TypeContext typeContext) {

		// Create the office section type
		OfficeSectionTypeImpl officeSectionType = new OfficeSectionTypeImpl(this.sectionName,
				new OfficeSectionInputType[] {}, new OfficeSectionOutputType[] {}, new OfficeSectionObjectType[] {});
		boolean isInitialised = this.initialiseSubSectionState(officeSectionType, parentSectionType, typeContext);
		if (!isInitialised) {
			return null; // must be initialised
		}

		// Return the type
		return officeSectionType;
	}

	/**
	 * Initialises the {@link OfficeSectionTypeImpl} with the
	 * {@link OfficeSubSectionType} information.
	 * 
	 * @param sectionType
	 *            {@link OfficeSectionTypeImpl}.
	 * @param parentSectionType
	 *            Parent {@link OfficeSubSectionType}.
	 * @param typeContext
	 *            {@link TypeContext}.
	 * @return <code>true</code> if initialised {@link OfficeSubSectionType}
	 *         state.
	 */
	private boolean initialiseSubSectionState(OfficeSectionTypeImpl sectionType, OfficeSubSectionType parentSectionType,
			TypeContext typeContext) {

		// Load the sub sections
		OfficeSubSectionType[] subSections = CompileUtil.loadTypes(this.subSections,
				(subSection) -> subSection.getSubSectionName(),
				(subSection) -> subSection.loadOfficeSubSectionType(sectionType, typeContext),
				OfficeSubSectionType[]::new);
		if (subSections == null) {
			return false;
		}

		// Load managed object types
		OfficeSectionManagedObjectType[] managedObjectTypes = CompileUtil.loadTypes(this.managedObjects,
				(mos) -> mos.getOfficeSectionManagedObjectName(),
				(mos) -> mos.loadOfficeSectionManagedObjectType(typeContext), OfficeSectionManagedObjectType[]::new);
		if (managedObjectTypes == null) {
			return false;
		}

		// Add the office context for the tasks
		OfficeTaskType[] taskTypes = CompileUtil.loadTypes(this.taskNodes, (task) -> task.getOfficeTaskName(), task -> {
			// Obtain the work type of the work for the task
			WorkNode work = task.getWorkNode();
			FunctionNamespaceType<?> workType = typeContext.getOrLoadWorkType(work);
			if (workType == null) {
				return null;
			}

			// Load and return the task type
			return task.loadOfficeTaskType(sectionType, typeContext);
		}, OfficeTaskType[]::new);
		if (taskTypes == null) {
			return false;
		}

		// Initialise the sub section state
		sectionType.initialiseAsOfficeSubSectionType(parentSectionType, subSections, taskTypes, managedObjectTypes);
		return true;
	}

	@Override
	public OfficeAvailableSectionInputType[] loadOfficeAvailableSectionInputTypes(TypeContext typeContext) {
		return CompileUtil.loadTypes(this.inputs, (input) -> input.getOfficeSectionInputName(), (input) -> {

			// Load the input type
			OfficeSectionInputType inputType = input.loadOfficeSectionInputType(typeContext);
			if (inputType == null) {
				return null;
			}

			// Return the section input type
			return new OfficeAvailableSectionInputTypeImpl(this.sectionName, inputType.getOfficeSectionInputName(),
					inputType.getParameterType());
		}, OfficeAvailableSectionInputType[]::new);
	}

	@Override
	public GovernanceNode[] getGoverningGovernances() {

		// Create the listing of governances
		List<GovernanceNode> governingGovernances = new LinkedList<GovernanceNode>();

		// Add the parent governances (if have parent)
		SectionNode parent = this.getParentSectionNode();
		if (parent != null) {
			governingGovernances.addAll(Arrays.asList(parent.getGoverningGovernances()));
		}

		// Add governance for this particular section
		governingGovernances.addAll(this.governances);

		// Return the governing governances
		return governingGovernances.toArray(new GovernanceNode[governingGovernances.size()]);
	}

	@Override
	public String getSectionQualifiedName(String simpleName) {

		// Obtain the qualified name for this section
		String qualifiedName = this.sectionName + "." + simpleName;

		// Recursively determine the qualified name
		if (this.parentSection == null) {
			// Top level section
			return qualifiedName;
		} else {
			// Further parent sections
			return this.parentSection.getSectionQualifiedName(qualifiedName);
		}
	}

	@Override
	public void buildSection(OfficeBuilder officeBuilder, OfficeBindings officeBindings, TypeContext typeContext) {

		// Build the tasks (in deterministic order)
		this.taskNodes.values().stream()
				.sorted((a, b) -> CompileUtil.sortCompare(a.getSectionTaskName(), b.getSectionTaskName()))
				.forEachOrdered((task) -> officeBindings.buildTaskIntoOffice(task));

		// Build the managed object sources (in deterministic order)
		this.managedObjectSourceNodes.values().stream()
				.sorted((a, b) -> CompileUtil.sortCompare(a.getOfficeManagedObjectSourceName(),
						b.getOfficeManagedObjectSourceName()))
				.forEachOrdered((managedObjectSource) -> officeBindings
						.buildManagedObjectSourceIntoOffice(managedObjectSource));

		// Build the managed objects (in deterministic order)
		this.managedObjects.values().stream().sorted(
				(a, b) -> CompileUtil.sortCompare(a.getOfficeManagedObjectName(), b.getOfficeManagedObjectName()))
				.forEachOrdered((managedObject) -> officeBindings.buildManagedObjectIntoOffice(managedObject));

		// Build the sub sections (in deterministic order)
		this.subSections.values().stream()
				.sorted((a, b) -> CompileUtil.sortCompare(a.getSubSectionName(), b.getSubSectionName()))
				.forEachOrdered((subSection) -> subSection.buildSection(officeBuilder, officeBindings, typeContext));
	}

	@Override
	public SectionNode getParentSectionNode() {
		return this.parentSection;
	}

	@Override
	public OfficeNode getOfficeNode() {
		return this.office;
	}

	@Override
	public DeployedOfficeInput getDeployedOfficeInput(String inputName) {
		return NodeUtil.getNode(inputName, this.inputs, () -> this.context.createSectionInputNode(inputName, this));
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
		return NodeUtil.getNode(inputName, this.inputs, () -> this.context.createSectionInputNode(inputName, this));
	}

	@Override
	public SubSectionOutput getSubSectionOutput(String outputName) {
		return NodeUtil.getNode(outputName, this.outputs, () -> this.context.createSectionOutputNode(outputName, this));
	}

	@Override
	public SubSectionObject getSubSectionObject(String objectName) {
		return NodeUtil.getNode(objectName, this.objects, () -> this.context.createSectionObjectNode(objectName, this));
	}

	/*
	 * ======================== SectionDesigner =============================
	 */

	@Override
	public SectionInput addSectionInput(String inputName, String parameterType) {
		return NodeUtil.getInitialisedNode(inputName, this.inputs, this.context,
				() -> this.context.createSectionInputNode(inputName, this), (input) -> input.initialise(parameterType));
	}

	@Override
	public SectionOutput addSectionOutput(String outputName, String argumentType, boolean isEscalationOnly) {
		return NodeUtil.getInitialisedNode(outputName, this.outputs, this.context,
				() -> this.context.createSectionOutputNode(outputName, this),
				(output) -> output.initialise(argumentType, isEscalationOnly));
	}

	@Override
	public SectionObject addSectionObject(String objectName, String objectType) {
		return NodeUtil.getInitialisedNode(objectName, this.objects, this.context,
				() -> this.context.createSectionObjectNode(objectName, this),
				(object) -> object.initialise(objectType));
	}

	@Override
	public SectionManagedObjectSource addSectionManagedObjectSource(String managedObjectSourceName,
			String managedObjectSourceClassName) {
		return NodeUtil.getInitialisedNode(managedObjectSourceName, this.managedObjectSourceNodes, this.context,
				() -> this.context.createManagedObjectSourceNode(managedObjectSourceName, this),
				(managedObjectSource) -> managedObjectSource.initialise(managedObjectSourceClassName, null));
	}

	@Override
	public SectionManagedObjectSource addSectionManagedObjectSource(String managedObjectSourceName,
			ManagedObjectSource<?, ?> managedObjectSource) {
		return NodeUtil.getInitialisedNode(managedObjectSourceName, this.managedObjectSourceNodes, this.context,
				() -> this.context.createManagedObjectSourceNode(managedObjectSourceName, this),
				(managedObjectSourceNode) -> managedObjectSourceNode
						.initialise(managedObjectSource.getClass().getName(), managedObjectSource));
	}

	@Override
	public SectionWork addSectionWork(String workName, String workSourceClassName) {
		return NodeUtil.getInitialisedNode(workName, this.workNodes, this.context,
				() -> this.context.createWorkNode(workName, this),
				(work) -> work.initialise(workSourceClassName, null));
	}

	@Override
	public SectionWork addSectionWork(String workName, ManagedFunctionSource<?> workSource) {
		return NodeUtil.getInitialisedNode(workName, this.workNodes, this.context,
				() -> this.context.createWorkNode(workName, this),
				(work) -> work.initialise(workSource.getClass().getName(), workSource));
	}

	@Override
	public SubSection addSubSection(String subSectionName, String sectionSourceClassName, String location) {
		return NodeUtil.getInitialisedNode(subSectionName, this.subSections, this.context,
				() -> this.context.createSectionNode(subSectionName, this),
				(section) -> section.initialise(sectionSourceClassName, null, location));
	}

	@Override
	public SubSection addSubSection(String subSectionName, SectionSource sectionSource, String location) {
		return NodeUtil.getInitialisedNode(subSectionName, this.subSections, this.context,
				() -> this.context.createSectionNode(subSectionName, this),
				(section) -> section.initialise(sectionSource.getClass().getName(), sectionSource, location));
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
	public void link(TaskFlow taskFlow, SectionTask task, FlowInstigationStrategyEnum instigationStrategy) {
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
	public void link(TaskFlow taskFlow, SectionOutput sectionOutput, FlowInstigationStrategyEnum instigationStrategy) {
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
	public void link(SubSectionOutput subSectionOutput, SubSectionInput subSectionInput) {
		this.linkFlow(subSectionOutput, subSectionInput);
	}

	@Override
	public void link(SubSectionOutput subSectionOutput, SectionOutput sectionOutput) {
		this.linkFlow(subSectionOutput, sectionOutput);
	}

	@Override
	public void link(ManagedObjectFlow managedObjectFlow, SectionTask task) {
		this.linkFlow(managedObjectFlow, task);
	}

	@Override
	public void link(ManagedObjectFlow managedObjectFlow, SubSectionInput subSectionInput) {
		this.linkFlow(managedObjectFlow, subSectionInput);
	}

	@Override
	public void link(ManagedObjectFlow managedObjectFlow, SectionOutput sectionOutput) {
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
	private void loadFlowInstigationStrategy(TaskFlow taskFlow, FlowInstigationStrategyEnum instigationStrategy) {

		// Ensure a task flow node
		if (!(taskFlow instanceof TaskFlowNode)) {
			this.addIssue("Invalid task flow: " + taskFlow + " ["
					+ (taskFlow == null ? null : taskFlow.getClass().getName()) + "]");
			return; // can not load instigation strategy
		}

		// Load the instigation strategy
		((TaskFlowNode) taskFlow).setFlowInstigationStrategy(instigationStrategy);
	}

	@Override
	public void link(TaskObject taskObject, SectionObject sectionObject) {
		this.linkObject(taskObject, sectionObject);
	}

	@Override
	public void link(SubSectionObject subSectionObject, SectionObject sectionObject) {
		this.linkObject(subSectionObject, sectionObject);
	}

	@Override
	public void link(TaskObject taskObject, SectionManagedObject sectionManagedObject) {
		this.linkObject(taskObject, sectionManagedObject);
	}

	@Override
	public void link(SubSectionObject subSectionObject, SectionManagedObject sectionManagedObject) {
		this.linkObject(subSectionObject, sectionManagedObject);
	}

	@Override
	public void link(ManagedObjectDependency dependency, SectionObject sectionObject) {
		this.linkObject(dependency, sectionObject);
	}

	@Override
	public void link(ManagedObjectDependency dependency, SectionManagedObject sectionManagedObject) {
		this.linkObject(dependency, sectionManagedObject);
	}

	@Override
	public void addIssue(String issueDescription) {
		this.context.getCompilerIssues().addIssue(this, issueDescription);
	}

	@Override
	public void addIssue(String issueDescription, Throwable cause) {
		this.context.getCompilerIssues().addIssue(this, issueDescription, cause);
	}

	/*
	 * ==================== OfficeSection =================================
	 */

	@Override
	public String getOfficeSectionName() {
		return this.sectionName;
	}

	@Override
	public OfficeSectionInput getOfficeSectionInput(String inputName) {
		return NodeUtil.getNode(inputName, this.inputs, () -> this.context.createSectionInputNode(inputName, this));
	}

	@Override
	public OfficeSectionOutput getOfficeSectionOutput(String outputName) {
		return NodeUtil.getNode(outputName, this.outputs, () -> this.context.createSectionOutputNode(outputName, this));
	}

	@Override
	public OfficeSectionObject getOfficeSectionObject(String objectName) {
		return NodeUtil.getNode(objectName, this.objects, () -> this.context.createSectionObjectNode(objectName, this));
	}

	@Override
	public OfficeSubSection getOfficeSubSection(String sectionName) {
		return NodeUtil.getNode(sectionName, this.subSections, () -> this.context.createSectionNode(sectionName, this));
	}

	@Override
	public OfficeSectionTask getOfficeSectionTask(String taskName) {
		return NodeUtil.getNode(taskName, this.taskNodes, () -> this.context.createTaskNode(taskName));
	}

	@Override
	public OfficeSectionManagedObject getOfficeSectionManagedObject(String managedObjectName) {
		return NodeUtil.getNode(managedObjectName, this.managedObjects,
				() -> this.context.createManagedObjectNode(managedObjectName));
	}

	@Override
	public OfficeSectionManagedObjectSource getOfficeSectionManagedObjectSource(String managedObjectSourceName) {
		return NodeUtil.getNode(managedObjectSourceName, this.managedObjectSourceNodes,
				() -> this.context.createManagedObjectSourceNode(managedObjectSourceName, this));
	}

	@Override
	public void addGovernance(OfficeGovernance governance) {

		// Ensure governance node
		if (!(governance instanceof GovernanceNode)) {
			this.addIssue("Invalid governance: " + governance + " ["
					+ (governance == null ? null : governance.getClass().getName()) + "]");
			return; // can not add governance
		}
		GovernanceNode governanceNode = (GovernanceNode) governance;

		// Add the governance
		this.governances.add(governanceNode);
	}

}