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

import net.officefloor.compile.impl.section.OfficeSectionTypeImpl;
import net.officefloor.compile.impl.section.SectionSourceContextImpl;
import net.officefloor.compile.impl.section.SectionTypeImpl;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.impl.util.LoadTypeError;
import net.officefloor.compile.impl.util.StringExtractor;
import net.officefloor.compile.internal.structure.GovernanceNode;
import net.officefloor.compile.internal.structure.ManagedObjectNode;
import net.officefloor.compile.internal.structure.ManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.internal.structure.SectionInputNode;
import net.officefloor.compile.internal.structure.SectionNode;
import net.officefloor.compile.internal.structure.SectionObjectNode;
import net.officefloor.compile.internal.structure.SectionOutputNode;
import net.officefloor.compile.internal.structure.TaskFlowNode;
import net.officefloor.compile.internal.structure.TaskNode;
import net.officefloor.compile.internal.structure.WorkNode;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.OfficeSectionInputType;
import net.officefloor.compile.section.OfficeSectionManagedObjectSourceType;
import net.officefloor.compile.section.OfficeSectionObjectType;
import net.officefloor.compile.section.OfficeSectionOutputType;
import net.officefloor.compile.section.OfficeSectionType;
import net.officefloor.compile.section.OfficeSubSectionType;
import net.officefloor.compile.section.OfficeTaskType;
import net.officefloor.compile.section.SectionInputType;
import net.officefloor.compile.section.SectionObjectType;
import net.officefloor.compile.section.SectionOutputType;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.office.OfficeGovernance;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionManagedObjectSource;
import net.officefloor.compile.spi.office.OfficeSectionObject;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.spi.office.OfficeSubSection;
import net.officefloor.compile.spi.office.OfficeTask;
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
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
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
	private InitialisedState state = null;

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
	public SectionNodeImpl(String sectionName, SectionNode parentSection,
			OfficeNode office, NodeContext context) {
		this.sectionName = sectionName;
		this.parentSection = parentSection;
		this.office = office;
		this.context = context;
	}

	/*
	 * ======================= Node =================================
	 */

	@Override
	public String getNodeName() {
		// TODO implement Node.getNodeName
		throw new UnsupportedOperationException(
				"TODO implement Node.getNodeName");

	}

	@Override
	public String getNodeType() {
		// TODO implement Node.getNodeType
		throw new UnsupportedOperationException(
				"TODO implement Node.getNodeType");

	}

	@Override
	public String getLocation() {
		// TODO implement Node.getLocation
		throw new UnsupportedOperationException(
				"TODO implement Node.getLocation");

	}

	@Override
	public Node getParentNode() {
		// TODO implement Node.getParentNode
		throw new UnsupportedOperationException(
				"TODO implement Node.getParentNode");

	}

	/*
	 * ======================= MangedObjectRegistry =======================
	 */

	@Override
	public ManagedObjectNode getManagedObjectNode(String managedObjectName) {
		// TODO implement ManagedObjectRegistry.getManagedObjectNode
		throw new UnsupportedOperationException(
				"TODO implement ManagedObjectRegistry.getManagedObjectNode");

	}

	@Override
	public ManagedObjectNode createManagedObjectNode(String managedObjectName,
			ManagedObjectScope managedObjectScope) {
		// TODO implement ManagedObjectRegistry.createManagedObjectNode
		throw new UnsupportedOperationException(
				"TODO implement ManagedObjectRegistry.createManagedObjectNode");

	}

	/*
	 * ======================= SectionNode =================================
	 */

	@Override
	public SectionNode initialise(SectionSource sectionSource,
			String sectionSourceClassName, String sectionLocation,
			PropertyList propertyList) {

		// Ensure not already initialise
		if (this.isInitialised()) {
			throw new IllegalStateException("SectionNode " + this.sectionName
					+ " already initialised");
		}

		// Load the initialised state
		this.state = new InitialisedState(sectionSourceClassName,
				sectionSource, sectionLocation, propertyList);

		// Return this for stringing together with constructor
		return this;
	}

	@Override
	public boolean isInitialised() {
		return (this.state != null);
	}

	@Override
	public boolean sourceSection() {

		// Determine if must instantiate
		SectionSource source = this.state.sectionSource;
		if (source == null) {

			// Obtain the section source class
			Class<? extends SectionSource> sectionSourceClass = this.context
					.getSectionSourceClass(this.state.sectionSourceClassName,
							this);
			if (sectionSourceClass == null) {
				return false; // must have section source class
			}

			// Obtain the section source
			source = CompileUtil
					.newInstance(sectionSourceClass, SectionSource.class, this,
							this.context.getCompilerIssues());
			if (source == null) {
				return false; // must have office source
			}
		}

		// Create the section source context
		SectionSourceContext context = new SectionSourceContextImpl(true,
				this.state.sectionLocation, this.state.propertyList,
				this.context);

		try {
			// Source the section type
			source.sourceSection(this, context);

		} catch (UnknownPropertyError ex) {
			this.addIssue("Missing property '" + ex.getUnknownPropertyName()
					+ "' for " + SectionSource.class.getSimpleName() + " "
					+ source.getClass().getName());
			return false; // must have property

		} catch (UnknownClassError ex) {
			this.addIssue("Can not load class '" + ex.getUnknownClassName()
					+ "' for " + SectionSource.class.getSimpleName() + " "
					+ source.getClass().getName());
			return false; // must have class

		} catch (UnknownResourceError ex) {
			this.addIssue("Can not obtain resource at location '"
					+ ex.getUnknownResourceLocation() + "' for "
					+ SectionSource.class.getSimpleName() + " "
					+ source.getClass().getName());
			return false; // must have resource

		} catch (LoadTypeError ex) {
			this.addIssue("Failure loading " + ex.getType().getSimpleName()
					+ " from source " + ex.getSourceClassName());
			return false; // must not fail in loading types

		} catch (Throwable ex) {
			this.addIssue(
					"Failed to source " + SectionType.class.getSimpleName()
							+ " definition from "
							+ SectionSource.class.getSimpleName() + " "
							+ source.getClass().getName(), ex);
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
		return this.subSections
				.values()
				.stream()
				.sorted((a, b) -> CompileUtil.sortCompare(
						a.getOfficeSectionName(), b.getOfficeSectionName()))
				.allMatch(t -> t.sourceSectionTree());
	}

	@Override
	public SectionType loadSectionType() {

		// Obtain the listing of input types sorted by name
		SectionInputType[] inputTypes = this.inputs
				.values()
				.stream()
				.sorted((a, b) -> CompileUtil.sortCompare(
						a.getSectionInputName(), b.getSectionInputName()))
				.toArray(SectionInputType[]::new);
		for (int i = 0; i < inputTypes.length; i++) {
			if (CompileUtil.isBlank(inputTypes[i].getSectionInputName())) {
				this.addIssue("Null name for input " + i);
				return null; // must have names for inputs
			}
		}

		// Obtain the listing of output types sorted by name
		SectionOutputType[] outputTypes = this.outputs
				.values()
				.stream()
				.sorted((a, b) -> CompileUtil.sortCompare(
						a.getSectionOutputName(), b.getSectionOutputName()))
				.toArray(SectionOutputType[]::new);
		for (int i = 0; i < outputTypes.length; i++) {
			if (CompileUtil.isBlank(outputTypes[i].getSectionOutputName())) {
				this.addIssue("Null name for output " + i);
				return null; // must have names for outputs
			}
		}

		// Obtain the listing of object types sorted by name
		SectionObjectType[] objectTypes = this.objects
				.values()
				.stream()
				.sorted((a, b) -> CompileUtil.sortCompare(
						a.getSectionObjectName(), b.getSectionObjectName()))
				.toArray(SectionObjectType[]::new);
		for (int i = 0; i < objectTypes.length; i++) {
			SectionObjectType objectType = objectTypes[i];
			if (CompileUtil.isBlank(objectType.getSectionObjectName())) {
				this.addIssue("Null name for object " + i);
				return null; // must have names for objects
			}
			if (CompileUtil.isBlank(objectType.getObjectType())) {
				this.addIssue("Null type for object " + i + " (name="
						+ objectType.getSectionObjectName() + ")");
				return null; // must have types for objects
			}
		}

		// Create and return the section type
		return new SectionTypeImpl(inputTypes, outputTypes, objectTypes);
	}

	@Override
	public OfficeSectionType loadOfficeSectionType() {

		// Load the section type
		SectionType sectionType = this.loadSectionType();
		if (sectionType == null) {
			return null; // must load section type
		}

		// Obtain the section inputs
		OfficeSectionInputType[] inputTypes = this.inputs
				.values()
				.stream()
				.sorted((a, b) -> CompileUtil.sortCompare(
						a.getOfficeSectionInputName(),
						b.getOfficeSectionInputName()))
				.toArray(OfficeSectionInputType[]::new);

		// Add the office context for the section outputs
		OfficeSectionOutputType[] outputTypes = this.outputs
				.values()
				.stream()
				.sorted((a, b) -> CompileUtil.sortCompare(
						a.getOfficeSectionOutputName(),
						b.getOfficeSectionOutputName()))
				.toArray(OfficeSectionOutputType[]::new);

		// Add the office context for the section objects
		OfficeSectionObjectType[] objectTypes = this.objects
				.values()
				.stream()
				.sorted((a, b) -> CompileUtil.sortCompare(
						a.getOfficeSectionObjectName(),
						b.getOfficeSectionObjectName()))
				.toArray(OfficeSectionObjectType[]::new);

		// Create the office section type
		OfficeSectionTypeImpl officeSectionType = new OfficeSectionTypeImpl(
				this.sectionName, inputTypes, outputTypes, objectTypes);
		this.initialiseSubSectionState(officeSectionType, null);

		// Return the type
		return officeSectionType;
	}

	@Override
	public OfficeSectionType loadOfficeSubSectionType(
			OfficeSubSectionType parentSectionType) {

		// Create the office section type
		OfficeSectionTypeImpl officeSectionType = new OfficeSectionTypeImpl(
				this.sectionName, new OfficeSectionInputType[] {},
				new OfficeSectionOutputType[] {},
				new OfficeSectionObjectType[] {});
		this.initialiseSubSectionState(officeSectionType, parentSectionType);

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
	 */
	private void initialiseSubSectionState(OfficeSectionTypeImpl sectionType,
			OfficeSubSectionType parentSectionType) {

		// Load the sub sections
		OfficeSubSectionType[] subSections = this.subSections
				.values()
				.stream()
				.sorted((a, b) -> CompileUtil.sortCompare(
						a.getSubSectionName(), b.getSubSectionName()))
				.map(t -> t.loadOfficeSubSectionType(sectionType))
				.filter(t -> (t != null)).toArray(OfficeSubSectionType[]::new);

		// Load managed object sources
		OfficeSectionManagedObjectSource[] managedObjectSources = this.managedObjectSourceNodes
				.values()
				.stream()
				.sorted((a, b) -> CompileUtil.sortCompare(
						a.getOfficeSectionManagedObjectSourceName(),
						b.getOfficeSectionManagedObjectSourceName()))
				.toArray(OfficeSectionManagedObjectSource[]::new);
		for (ManagedObjectSourceNode managedObjectSource : this.managedObjectSourceNodes
				.values()) {
			managedObjectSource.loadManagedObjectType();
		}
		OfficeSectionManagedObjectSourceType[] managedObjectSourceTypes = this.managedObjectSourceNodes
				.values()
				.stream()
				.sorted((a, b) -> CompileUtil.sortCompare(
						a.getOfficeSectionManagedObjectSourceName(),
						b.getOfficeSectionManagedObjectSourceName()))
				.toArray(OfficeSectionManagedObjectSourceType[]::new);

		// Add the office context for the tasks
		OfficeTaskType[] taskTypes = this.taskNodes
				.values()
				.stream()
				.sorted((a, b) -> CompileUtil.sortCompare(
						a.getOfficeTaskName(), b.getOfficeTaskName()))
				.map(t -> t.loadOfficeTaskType()).filter(t -> (t != null))
				.toArray(OfficeTaskType[]::new);

		// Initialise the sub section state
		sectionType.initialiseAsOfficeSubSectionType(parentSectionType,
				subSections, taskTypes, managedObjectSourceTypes);
	}

	@Override
	public GovernanceNode[] getGoverningGovernances() {

		// Create the listing of governances
		List<GovernanceNode> governingGovernances = new LinkedList<GovernanceNode>();

		// Add the parent governances (if have parent)
		SectionNode parent = this.getParentSectionNode();
		if (parent != null) {
			governingGovernances.addAll(Arrays.asList(parent
					.getGoverningGovernances()));
		}

		// Add governance for this particular section
		governingGovernances.addAll(this.governances);

		// Return the governing governances
		return governingGovernances
				.toArray(new GovernanceNode[governingGovernances.size()]);
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
	public void buildSection(OfficeFloorBuilder officeFloorBuilder,
			OfficeNode officeNode, OfficeBuilder officeBuilder) {

		// Build the work of this section (in deterministic order)
		WorkNode[] works = CompileUtil.toSortedArray(this.workNodes.values(),
				new WorkNode[0], new StringExtractor<WorkNode>() {
					@Override
					public String toString(WorkNode work) {
						return work.getSectionWorkName();
					}
				});
		for (WorkNode work : works) {
			work.buildWork(officeBuilder);
		}

		// Build the managed object sources for office (in deterministic order)
		ManagedObjectSourceNode[] managedObjectSources = CompileUtil
				.toSortedArray(this.managedObjectSourceNodes.values(),
						new ManagedObjectSourceNode[0],
						new StringExtractor<ManagedObjectSourceNode>() {
							@Override
							public String toString(
									ManagedObjectSourceNode object) {
								return object
										.getOfficeManagedObjectSourceName();
							}
						});
		for (ManagedObjectSourceNode mos : managedObjectSources) {
			mos.buildManagedObject(officeFloorBuilder, officeNode,
					officeBuilder);
		}

		// Build the sub sections (in deterministic order)
		SectionNode[] subSections = CompileUtil.toSortedArray(
				this.subSections.values(), new SectionNode[0],
				new StringExtractor<SectionNode>() {
					@Override
					public String toString(SectionNode subSection) {
						return subSection.getSubSectionName();
					}
				});
		for (SectionNode subSection : subSections) {
			subSection.buildSection(officeFloorBuilder, officeNode,
					officeBuilder);
		}
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
		// Obtain and return the section input
		SectionInputNode input = this.inputs.get(inputName);
		if (input == null) {
			// Add the section input
			input = this.context.createSectionInputNode(inputName, this);
			this.inputs.put(inputName, input);
		}
		return input;
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
		this.state.propertyList.addProperty(name).setValue(value);
	}

	@Override
	public SubSectionInput getSubSectionInput(String inputName) {
		// Obtain and return the section input for the name
		SectionInputNode input = this.inputs.get(inputName);
		if (input == null) {
			// Add the input
			input = this.context.createSectionInputNode(inputName, this);
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
					this.state.sectionLocation, this, this.context);
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
			object = this.context.createSectionObjectNode(objectName, this);
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
			input = this.context.createSectionInputNode(inputName, this)
					.initialise(parameterType);
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
			output = this.context.createSectionOutputNode(outputName, this)
					.initialise(argumentType, isEscalationOnly);
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
			object = this.context.createSectionObjectNode(objectName, this)
					.initialise(objectType);
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
	public SectionManagedObjectSource addSectionManagedObjectSource(
			String managedObjectSourceName, String managedObjectSourceClassName) {
		// Obtain and return the section managed object source for the name
		ManagedObjectSourceNode managedObjectSource = this.managedObjectSourceNodes
				.get(managedObjectSourceName);
		if (managedObjectSource == null) {
			// Add the section managed object source
			managedObjectSource = this.context.createManagedObjectSourceNode(
					managedObjectSourceName, managedObjectSourceClassName,
					null, this);
			this.managedObjectSourceNodes.put(managedObjectSourceName,
					managedObjectSource);
		} else {
			// Section managed object source already added
			this.addIssue("Section managed object source "
					+ managedObjectSourceName + " already added");
		}
		return managedObjectSource;
	}

	@Override
	public SectionManagedObjectSource addSectionManagedObjectSource(
			String managedObjectSourceName,
			ManagedObjectSource<?, ?> managedObjectSource) {
		// Obtain and return the section managed object source for the name
		ManagedObjectSourceNode managedObjectSourceNode = this.managedObjectSourceNodes
				.get(managedObjectSourceName);
		if (managedObjectSourceNode == null) {
			// Add the section managed object source
			managedObjectSourceNode = this.context
					.createManagedObjectSourceNode(managedObjectSourceName,
							managedObjectSource.getClass().getName(),
							managedObjectSource, this);
			this.managedObjectSourceNodes.put(managedObjectSourceName,
					managedObjectSourceNode);
		} else {
			// Section managed object source already added
			this.addIssue("Section managed object source "
					+ managedObjectSourceName + " already added");
		}
		return managedObjectSourceNode;
	}

	@Override
	public SectionWork addSectionWork(String workName,
			String workSourceClassName) {
		// Obtain and return the section work for the name
		WorkNode work = this.workNodes.get(workName);
		if (work == null) {
			// Add the section work
			work = this.context.createWorkNode(workName, workSourceClassName,
					null, this);
			this.workNodes.put(workName, work);
		} else {
			// Section work already added
			this.addIssue("Section work " + workName + " already added");
		}
		return work;
	}

	@Override
	public SectionWork addSectionWork(String workName, WorkSource<?> workSource) {
		// Obtain and return the section work for the name
		WorkNode work = this.workNodes.get(workName);
		if (work == null) {
			// Add the section work
			work = this.context.createWorkNode(workName, workSource.getClass()
					.getName(), workSource, this);
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
			subSection = this.context.createSectionNode(subSectionName, this)
					.initialise(sectionSource, sectionSourceClassName,
							location, null);
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

	@Override
	public void addIssue(String issueDescription) {
		this.context.getCompilerIssues().addIssue(this, issueDescription);
	}

	@Override
	public void addIssue(String issueDescription, Throwable cause) {
		this.context.getCompilerIssues()
				.addIssue(this, issueDescription, cause);
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
		throw new UnsupportedOperationException("TODO implement");
	}

	@Override
	public OfficeSectionOutput getOfficeSectionOutput(String outputName) {
		throw new UnsupportedOperationException("TODO implement");
	}

	@Override
	public OfficeSectionObject getOfficeSectionObject(String objectName) {
		throw new UnsupportedOperationException("TODO implement");
	}

	@Override
	public OfficeSubSection getOfficeSubSection(String sectionName) {
		throw new UnsupportedOperationException("TODO implement");
	}

	@Override
	public OfficeTask getOfficeTask(String taskName) {
		throw new UnsupportedOperationException("TODO implement");
	}

	@Override
	public OfficeSectionManagedObjectSource getOfficeSectionManagedObjectSource(
			String managedObjectSourceName) {
		throw new UnsupportedOperationException("TODO implement");
	}

	@Override
	public void addGovernance(OfficeGovernance governance) {

		// Ensure governance node
		if (!(governance instanceof GovernanceNode)) {
			this.addIssue("Invalid governance: "
					+ governance
					+ " ["
					+ (governance == null ? null : governance.getClass()
							.getName()) + "]");
			return; // can not add governance
		}
		GovernanceNode governanceNode = (GovernanceNode) governance;

		// Add the governance
		this.governances.add(governanceNode);
	}

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
		 * {@link PropertyList} to source this {@link OfficeSection}.
		 */
		private final PropertyList propertyList;

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
		 * @param propertyList
		 *            {@link PropertyList} to source this {@link OfficeSection}.
		 */
		private InitialisedState(String sectionSourceClassName,
				SectionSource sectionSource, String sectionLocation,
				PropertyList propertyList) {
			this.sectionSourceClassName = sectionSourceClassName;
			this.sectionSource = sectionSource;
			this.sectionLocation = sectionLocation;
			this.propertyList = (propertyList != null ? propertyList
					: SectionNodeImpl.this.context.createPropertyList());
		}
	}

}