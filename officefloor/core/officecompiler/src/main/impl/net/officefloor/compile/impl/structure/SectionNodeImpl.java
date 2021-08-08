/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.compile.impl.structure;

import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.compile.impl.office.OfficeAvailableSectionInputTypeImpl;
import net.officefloor.compile.impl.section.OfficeSectionTypeImpl;
import net.officefloor.compile.impl.section.SectionSourceContextImpl;
import net.officefloor.compile.impl.section.SectionTypeImpl;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.impl.util.LinkUtil;
import net.officefloor.compile.impl.util.LoadTypeError;
import net.officefloor.compile.internal.structure.AutoWire;
import net.officefloor.compile.internal.structure.AutoWireLink;
import net.officefloor.compile.internal.structure.AutoWirer;
import net.officefloor.compile.internal.structure.CompileContext;
import net.officefloor.compile.internal.structure.FunctionFlowNode;
import net.officefloor.compile.internal.structure.FunctionNamespaceNode;
import net.officefloor.compile.internal.structure.GovernanceNode;
import net.officefloor.compile.internal.structure.LinkObjectNode;
import net.officefloor.compile.internal.structure.LinkTeamNode;
import net.officefloor.compile.internal.structure.ManagedFunctionNode;
import net.officefloor.compile.internal.structure.ManagedFunctionVisitor;
import net.officefloor.compile.internal.structure.ManagedObjectExtensionNode;
import net.officefloor.compile.internal.structure.ManagedObjectNode;
import net.officefloor.compile.internal.structure.ManagedObjectPoolNode;
import net.officefloor.compile.internal.structure.ManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.ManagedObjectSourceVisitor;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeBindings;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.internal.structure.SectionInputNode;
import net.officefloor.compile.internal.structure.SectionNode;
import net.officefloor.compile.internal.structure.SectionObjectNode;
import net.officefloor.compile.internal.structure.SectionOutputNode;
import net.officefloor.compile.issues.CompileError;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.office.OfficeAvailableSectionInputType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.OfficeFunctionType;
import net.officefloor.compile.section.OfficeSectionInputType;
import net.officefloor.compile.section.OfficeSectionManagedObjectType;
import net.officefloor.compile.section.OfficeSectionObjectType;
import net.officefloor.compile.section.OfficeSectionOutputType;
import net.officefloor.compile.section.OfficeSectionType;
import net.officefloor.compile.section.OfficeSubSectionType;
import net.officefloor.compile.section.SectionInputType;
import net.officefloor.compile.section.SectionObjectType;
import net.officefloor.compile.section.SectionOutputType;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.office.OfficeGovernance;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionFunction;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionManagedObject;
import net.officefloor.compile.spi.office.OfficeSectionManagedObjectSource;
import net.officefloor.compile.spi.office.OfficeSectionObject;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.spi.office.OfficeSubSection;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSource;
import net.officefloor.compile.spi.section.FunctionFlow;
import net.officefloor.compile.spi.section.SectionDependencyObjectNode;
import net.officefloor.compile.spi.section.SectionDependencyRequireNode;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionFlowSinkNode;
import net.officefloor.compile.spi.section.SectionFlowSourceNode;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.compile.spi.section.SectionFunctionNamespace;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.compile.spi.section.SectionManagedObjectPool;
import net.officefloor.compile.spi.section.SectionManagedObjectSource;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.spi.section.SubSectionInput;
import net.officefloor.compile.spi.section.SubSectionObject;
import net.officefloor.compile.spi.section.SubSectionOutput;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.UnknownFunctionException;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.source.AbstractSourceError;
import net.officefloor.frame.internal.structure.ManagedObjectScope;

/**
 * {@link SectionNode} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class SectionNodeImpl implements SectionNode {

	/**
	 * Indicates if this {@link SectionNode} is named. For {@link SectionType} need
	 * to create unnamed {@link SectionNode}.
	 */
	private final boolean isSectionNamed;

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
		 * @param sectionSourceClassName Class name of the {@link SectionSource}.
		 * @param sectionSource          {@link SectionSource} for this
		 *                               {@link SectionNode}.
		 * @param sectionLocation        Location of the {@link OfficeSection} being
		 *                               built by this {@link SectionDesigner}.
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
	 * {@link ManagedObjectPoolNode} instances by thier
	 * {@link SectionManagedObjectPool} names.
	 */
	private final Map<String, ManagedObjectPoolNode> managedObjectPoolNodes = new HashMap<>();

	/**
	 * {@link ManagedObjectNode} instances by their {@link SectionManagedObject}
	 * name.
	 */
	private final Map<String, ManagedObjectNode> managedObjects = new HashMap<String, ManagedObjectNode>();

	/**
	 * Listing of {@link OfficeGovernance} instances providing {@link Governance}
	 * over this {@link SectionNode}.
	 */
	private final List<GovernanceNode> governances = new LinkedList<GovernanceNode>();

	/**
	 * {@link Node} instances by their {@link SectionFunctionNamespace} names.
	 */
	private final Map<String, FunctionNamespaceNode> namespaceNodes = new HashMap<String, FunctionNamespaceNode>();

	/**
	 * Map of {@link ManagedFunctionNode} instances for this {@link OfficeSection}
	 * by their {@link SectionFunction} names.
	 */
	private final Map<String, ManagedFunctionNode> functionNodes = new HashMap<String, ManagedFunctionNode>();

	/**
	 * {@link SubSection} instances by their names.
	 */
	private final Map<String, SectionNode> subSections = new HashMap<String, SectionNode>();

	/**
	 * Super {@link SectionNode}.
	 */
	private SectionNode superSectionNode = null;

	/**
	 * {@link SectionSource} used to source the {@link OfficeSection}.
	 */
	private SectionSource usedSectionSource = null;

	/**
	 * Initialises this {@link SectionNode} with the basic information.
	 * 
	 * @param isSectionNamed Indicates if named.
	 * @param sectionName    Name of this {@link OfficeSection}.
	 * @param parentSection  Optional parent {@link SectionNode}. May be
	 *                       <code>null</code>.
	 * @param office         {@link Office} containing the {@link OfficeSection}.
	 * @param context        {@link NodeContext}.
	 */
	public SectionNodeImpl(boolean isSectionNamed, String sectionName, SectionNode parentSection, OfficeNode office,
			NodeContext context) {
		this.isSectionNamed = isSectionNamed;
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
		return this.isSectionNamed ? this.sectionName : this.getParentNode().getNodeName();
	}

	@Override
	public String getQualifiedName() {
		return this.isSectionNamed ? SectionNode.super.getQualifiedName() : this.getParentNode().getQualifiedName();
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
	public Node[] getChildNodes() {
		return NodeUtil.getChildNodes(this.inputs, this.outputs, this.objects, this.subSections, this.functionNodes,
				this.managedObjectSourceNodes, this.managedObjects);
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
	 * ===================== ManagedFunctionRegistry =======================
	 */

	@Override
	public ManagedFunctionNode addManagedFunctionNode(String functionName, String functionTypeName,
			FunctionNamespaceNode namespaceNode) {
		return NodeUtil.getInitialisedNode(functionName, this.functionNodes, this.context,
				() -> this.context.createFunctionNode(functionName, this),
				(function) -> function.initialise(functionTypeName, namespaceNode));
	}

	/*
	 * ======================= MangedObjectRegistry =======================
	 */

	@Override
	public ManagedObjectNode getManagedObjectNode(String managedObjectName) {
		return NodeUtil.getNode(managedObjectName, this.managedObjects,
				() -> this.context.createManagedObjectNode(managedObjectName, this));
	}

	@Override
	public ManagedObjectNode addManagedObjectNode(String managedObjectName, ManagedObjectScope managedObjectScope,
			ManagedObjectSourceNode managedObjectSourceNode) {
		return NodeUtil.getInitialisedNode(managedObjectName, this.managedObjects, this.context,
				() -> this.context.createManagedObjectNode(managedObjectName, this),
				(managedObject) -> managedObject.initialise(managedObjectScope, managedObjectSourceNode));
	}

	/*
	 * ======================= SectionNode =================================
	 */

	@Override
	public boolean sourceSection(ManagedFunctionVisitor managedFunctionVisitor,
			ManagedObjectSourceVisitor managedObjectSourceVisitor, CompileContext compileContext,
			boolean isLoadingType) {

		// Ensure the section is initialised
		if (!this.isInitialised()) {
			this.context.getCompilerIssues().addIssue(this, "Section '" + this.sectionName + "' is not initialised");
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

		// Keep track of the section source
		this.usedSectionSource = source;

		// Obtain the overridden properties
		String qualifiedName = this.getQualifiedName();
		PropertyList overriddenProperties = this.context.overrideProperties(this, qualifiedName, this.office,
				this.propertyList);

		// Create the section source context
		String[] additionalProfiles = this.context.additionalProfiles(this.office);
		SectionSourceContext context = new SectionSourceContextImpl(isLoadingType, this.state.sectionLocation,
				additionalProfiles, overriddenProperties, this, this.context);

		try {
			// Source the section type
			source.sourceSection(this, context);

		} catch (AbstractSourceError ex) {
			ex.addIssue(new SourceIssuesIssueTarget(this));
			return false; // can not carry on

		} catch (LoadTypeError ex) {
			ex.addLoadTypeIssue(this, this.context.getCompilerIssues());
			return false; // must not fail in loading types

		} catch (CompileError ex) {
			return false; // issue already reported

		} catch (Throwable ex) {
			this.addIssue("Failed to source " + SectionType.class.getSimpleName() + " definition from "
					+ SectionSource.class.getSimpleName() + " " + source.getClass().getName(), ex);
			return false; // must be successful
		}

		// Successfully sourced section
		return true;
	}

	@Override
	public boolean sourceSectionTree(ManagedFunctionVisitor managedFunctionVisitor,
			ManagedObjectSourceVisitor managedObjectSourceVisitor, CompileContext compileContext,
			boolean isLoadingType) {

		// Source this section
		boolean isSourced = this.sourceSection(managedFunctionVisitor, managedObjectSourceVisitor, compileContext,
				isLoadingType);
		if (!isSourced) {
			return false;
		}

		// Ensure all functions are sourced
		isSourced = CompileUtil.source(this.functionNodes, (function) -> function.getSectionFunctionName(),
				(function) -> function.souceManagedFunction(managedFunctionVisitor, compileContext));
		if (!isSourced) {
			return false;
		}

		// Ensure all managed object sources are sourced
		isSourced = CompileUtil.source(this.managedObjectSourceNodes,
				(managedObjectSource) -> managedObjectSource.getSectionManagedObjectSourceName(),
				(managedObjectSource) -> managedObjectSource.sourceManagedObjectSource(managedObjectSourceVisitor,
						compileContext));
		if (!isSourced) {
			return false;
		}

		// Ensure all managed objects are sourced
		isSourced = CompileUtil.source(this.managedObjects,
				(managedObject) -> managedObject.getSectionManagedObjectName(),
				(managedObject) -> managedObject.sourceManagedObject(compileContext));
		if (!isSourced) {
			return false;
		}

		// Ensure all managed object pools are sourced
		isSourced = CompileUtil.source(this.managedObjectPoolNodes, (pool) -> pool.getSectionManagedObjectPoolName(),
				(pool) -> pool.sourceManagedObjectPool(compileContext));
		if (!isSourced) {
			return false;
		}

		// Ensure all sub sections are also sourced
		isSourced = CompileUtil.source(this.subSections, (subSection) -> subSection.getOfficeSectionName(),
				(subSection) -> subSection.sourceSectionTree(managedFunctionVisitor, managedObjectSourceVisitor,
						compileContext, isLoadingType));
		if (!isSourced) {
			return false;
		}

		// Successfully sourced section
		return true;
	}

	@Override
	public boolean sourceInheritance(CompileContext compileContext) {

		// Iterate over outputs linking via inheritance
		boolean isValid[] = new boolean[] { true };
		this.outputs.values().stream()
				.sorted((a, b) -> CompileUtil.sortCompare(a.getSectionOutputName(), b.getSectionOutputName()))
				.forEachOrdered((output) -> {

					// Determine if output already linked
					if (output.getLinkedFlowNode() != null) {
						return; // already linked
					}

					// Search the inheritance hierarchy for link
					SectionNode searchSection = this;
					boolean isCyclicInheritance = false;
					Deque<SectionNode> inheritanceHierarchy = new LinkedList<SectionNode>();
					do {

						// Obtain link configuration from search section
						SectionOutputNode parentSectionOutput = searchSection
								.getSectionOutputNode(output.getSectionOutputName());
						if ((parentSectionOutput != null) && (parentSectionOutput.getLinkedFlowNode() != null)) {
							// Inherit the link
							output.linkFlowNode(parentSectionOutput.getLinkedFlowNode());

							// Link configured
							return;
						}

						// Use super section in next iteration if no link
						searchSection = searchSection.getSuperSection();

						// Determine if cyclic inheritance
						if (inheritanceHierarchy.contains(searchSection)) {
							isCyclicInheritance = true;
						}
						inheritanceHierarchy.push(searchSection);

					} while ((!isCyclicInheritance) && (searchSection != null));

					// Provide issue if cyclic inheritance hierarchy
					if (isCyclicInheritance) {
						// Flag invalid
						isValid[0] = false;

						// Cyclic inheritance, so provide issue
						StringBuilder hierarchyLog = new StringBuilder();
						for (Iterator<SectionNode> iterator = inheritanceHierarchy.iterator(); iterator.hasNext();) {
							hierarchyLog.append(iterator.next().getQualifiedName() + " : ");
						}
						this.context.getCompilerIssues().addIssue(this,
								"Cyclic section inheritance hierarchy ( " + hierarchyLog.toString() + "... )");
					}
				});

		// Return whether valid
		return isValid[0];
	}

	@Override
	public SectionNode getSuperSection() {
		return this.superSectionNode;
	}

	@Override
	public SectionOutputNode getSectionOutputNode(String outputName) {
		return this.outputs.get(outputName);
	}

	@Override
	public SectionType loadSectionType(CompileContext compileContext) {

		// Obtain the listing of input types sorted by name
		SectionInputType[] inputTypes = CompileUtil.loadTypes(this.inputs, (input) -> input.getSectionInputName(),
				(input) -> input.loadSectionInputType(compileContext), SectionInputType[]::new);
		if (inputTypes == null) {
			return null;
		}

		// Obtain the listing of output types sorted by name
		SectionOutputType[] outputTypes = CompileUtil.loadTypes(this.outputs, (output) -> output.getSectionOutputName(),
				(output) -> output.loadSectionOutputType(compileContext), SectionOutputType[]::new);
		if (outputTypes == null) {
			return null;
		}

		// Obtain the listing of object types sorted by name
		SectionObjectType[] objectTypes = CompileUtil.loadTypes(this.objects, (object) -> object.getSectionObjectName(),
				(object) -> object.loadSectionObjectType(compileContext), SectionObjectType[]::new);
		if (objectTypes == null) {
			return null;
		}

		// Create and return the section type
		return new SectionTypeImpl(inputTypes, outputTypes, objectTypes);
	}

	@Override
	public OfficeSectionType loadOfficeSectionType(CompileContext compileContext) {

		// Load the section type
		SectionType sectionType = this.loadSectionType(compileContext);
		if (sectionType == null) {
			return null; // must load section type
		}

		// Obtain the section inputs
		OfficeSectionInputType[] inputTypes = CompileUtil.loadTypes(this.inputs,
				(input) -> input.getOfficeSectionInputName(),
				(input) -> input.loadOfficeSectionInputType(compileContext), OfficeSectionInputType[]::new);
		if (inputTypes == null) {
			return null; // must load types
		}

		// Add the office context for the section outputs
		OfficeSectionOutputType[] outputTypes = CompileUtil.loadTypes(this.outputs,
				(output) -> output.getOfficeSectionOutputName(),
				(output) -> output.loadOfficeSectionOutputType(compileContext), OfficeSectionOutputType[]::new);
		if (outputTypes == null) {
			return null; // must load types
		}

		// Add the office context for the section objects
		OfficeSectionObjectType[] objectTypes = CompileUtil.loadTypes(this.objects,
				(object) -> object.getOfficeSectionObjectName(),
				(object) -> object.loadOfficeSectionObjectType(compileContext), OfficeSectionObjectType[]::new);
		if (objectTypes == null) {
			return null; // must load types
		}

		// Create the office section type
		OfficeSectionTypeImpl officeSectionType = new OfficeSectionTypeImpl(this.sectionName, inputTypes, outputTypes,
				objectTypes);
		boolean isInitialised = this.initialiseSubSectionState(officeSectionType, null, compileContext);
		if (!isInitialised) {
			return null; // must be initialised
		}

		// Return the type
		return officeSectionType;
	}

	@Override
	public OfficeSectionType loadOfficeSubSectionType(OfficeSubSectionType parentSectionType,
			CompileContext compileContext) {

		// Create the office section type
		OfficeSectionTypeImpl officeSectionType = new OfficeSectionTypeImpl(this.sectionName,
				new OfficeSectionInputType[] {}, new OfficeSectionOutputType[] {}, new OfficeSectionObjectType[] {});
		boolean isInitialised = this.initialiseSubSectionState(officeSectionType, parentSectionType, compileContext);
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
	 * @param sectionType       {@link OfficeSectionTypeImpl}.
	 * @param parentSectionType Parent {@link OfficeSubSectionType}.
	 * @param compileContext    {@link CompileContext}.
	 * @return <code>true</code> if initialised {@link OfficeSubSectionType} state.
	 */
	private boolean initialiseSubSectionState(OfficeSectionTypeImpl sectionType, OfficeSubSectionType parentSectionType,
			CompileContext compileContext) {

		// Load the sub sections
		OfficeSubSectionType[] subSections = CompileUtil.loadTypes(this.subSections,
				(subSection) -> subSection.getSubSectionName(),
				(subSection) -> subSection.loadOfficeSubSectionType(sectionType, compileContext),
				OfficeSubSectionType[]::new);
		if (subSections == null) {
			return false;
		}

		// Load managed object types
		OfficeSectionManagedObjectType[] managedObjectTypes = CompileUtil.loadTypes(this.managedObjects,
				(mos) -> mos.getOfficeSectionManagedObjectName(),
				(mos) -> mos.loadOfficeSectionManagedObjectType(compileContext), OfficeSectionManagedObjectType[]::new);
		if (managedObjectTypes == null) {
			return false;
		}

		// Add the office context for the functions
		OfficeFunctionType[] functionTypes = CompileUtil.loadTypes(this.functionNodes,
				(function) -> function.getOfficeFunctionName(),
				function -> function.loadOfficeFunctionType(sectionType, compileContext), OfficeFunctionType[]::new);
		if (functionTypes == null) {
			return false;
		}

		// Initialise the sub section state
		sectionType.initialiseAsOfficeSubSectionType(parentSectionType, subSections, functionTypes, managedObjectTypes);
		return true;
	}

	@Override
	public OfficeAvailableSectionInputType[] loadOfficeAvailableSectionInputTypes(CompileContext compileContext) {
		return CompileUtil.loadTypes(this.inputs, (input) -> input.getOfficeSectionInputName(), (input) -> {

			// Load the input type
			OfficeSectionInputType inputType = input.loadOfficeSectionInputType(compileContext);
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
		String qualifiedName = this.sectionName + (simpleName != null ? "." + simpleName : "");

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
	public void loadAutoWireExtensionTargets(AutoWirer<ManagedObjectExtensionNode> autoWirer,
			CompileContext compileContext) {

		// Load the objects
		this.managedObjects.values().forEach((mo) -> {

			// Load the type
			ManagedObjectType<?> moType = mo.getManagedObjectSourceNode().loadManagedObjectType(compileContext);
			if (moType == null) {
				return;
			}

			// Load the extensions
			for (Class<?> extensionType : moType.getExtensionTypes()) {
				autoWirer.addAutoWireTarget(mo, new AutoWire(extensionType));
			}
		});

		// Load the sub sections
		this.subSections.values()
				.forEach((subSection) -> subSection.loadAutoWireExtensionTargets(autoWirer, compileContext));
	}

	@Override
	public void autoWireObjects(AutoWirer<LinkObjectNode> autoWirer, CompileContext compileContext) {

		// Auto-wire the objects
		this.objects.values().forEach((object) -> {

			// Ignore if already configured
			if (object.getLinkedObjectNode() != null) {
				return;
			}

			// Obtain the type
			SectionObjectType objectType = object.loadSectionObjectType(compileContext);
			if (objectType == null) {
				return; // must have type
			}

			// Auto-wire the object
			AutoWireLink<SectionObjectNode, LinkObjectNode>[] links = autoWirer.getAutoWireLinks(object,
					new AutoWire(objectType.getTypeQualifier(), objectType.getObjectType()));
			if (links.length == 1) {
				LinkUtil.linkAutoWireObjectNode(object, links[0].getTargetNode(this.office), this.office, autoWirer,
						compileContext, this.context.getCompilerIssues(), (link) -> object.linkObjectNode(link));
			}
		});
	}

	@Override
	public void autoWireTeams(AutoWirer<LinkTeamNode> autoWirer, CompileContext compileContext) {

		// Associate responsibility for the functions
		this.functionNodes.values()
				.forEach((function) -> function.autoWireManagedFunctionResponsibility(autoWirer, compileContext));

		// Auto-wire managed object source teams
		this.managedObjectSourceNodes.values().stream()
				.sorted((a, b) -> CompileUtil.sortCompare(a.getQualifiedName(), b.getQualifiedName()))
				.forEachOrdered((mos) -> mos.autoWireTeams(autoWirer, compileContext));

		// Auto-wire the sub sections
		this.subSections.values().stream()
				.sorted((a, b) -> CompileUtil.sortCompare(a.getSubSectionName(), b.getSubSectionName()))
				.forEachOrdered((subSection) -> subSection.autoWireTeams(autoWirer, compileContext));
	}

	@Override
	public void loadManagedFunctionNodes(Map<String, ManagedFunctionNode> managedFunctionNodes) {

		// Load the managed functions from this section
		this.functionNodes.values().stream()
				.sorted((a, b) -> CompileUtil.sortCompare(a.getSectionFunctionName(), b.getSectionFunctionName()))
				.forEachOrdered((function) -> {
					String functionName = function.getQualifiedFunctionName();
					managedFunctionNodes.put(functionName, function);
				});

		// Load the sub section managed functions
		this.subSections.values().stream()
				.sorted((a, b) -> CompileUtil.sortCompare(a.getSubSectionName(), b.getSubSectionName()))
				.forEachOrdered((section) -> section.loadManagedFunctionNodes(managedFunctionNodes));
	}

	@Override
	public boolean runExecutionExplorers(Map<String, ManagedFunctionNode> managedFunctions,
			CompileContext compileContext) {

		// Run execution explorers for the inputs (in deterministic order)
		return this.inputs.values().stream()
				.sorted((a, b) -> CompileUtil.sortCompare(a.getSectionInputName(), b.getSectionInputName()))
				.allMatch((input) -> input.runExecutionExplorers(managedFunctions, compileContext));
	}

	@Override
	public void buildSection(OfficeBuilder officeBuilder, OfficeBindings officeBindings,
			CompileContext compileContext) {

		// Register as possible MBean
		String qualifiedName = this.getQualifiedName();
		compileContext.registerPossibleMBean(SectionSource.class, qualifiedName, this.usedSectionSource);

		// Build the functions (in deterministic order)
		this.functionNodes.values().stream()
				.sorted((a, b) -> CompileUtil.sortCompare(a.getSectionFunctionName(), b.getSectionFunctionName()))
				.forEachOrdered((function) -> officeBindings.buildManagedFunctionIntoOffice(function));

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
				.forEachOrdered((subSection) -> subSection.buildSection(officeBuilder, officeBindings, compileContext));
	}

	@Override
	public void loadExternalServicing(Office office) throws UnknownFunctionException {

		// Load the section inputs
		SectionInputNode[] inputNodes = this.inputs.values().stream()
				.sorted((a, b) -> CompileUtil.sortCompare(a.getSectionInputName(), b.getSectionInputName()))
				.toArray(SectionInputNode[]::new);
		for (SectionInputNode inputNode : inputNodes) {
			inputNode.loadExternalServicing(office);
		}

		// Load the sub sections
		SectionNode[] subSectionNodes = this.subSections.values().stream()
				.sorted((a, b) -> CompileUtil.sortCompare(a.getSubSectionName(), b.getSubSectionName()))
				.toArray(SectionNode[]::new);
		for (SectionNode subSectionNode : subSectionNodes) {
			subSectionNode.loadExternalServicing(office);
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
	public SectionManagedObjectPool addManagedObjectPool(String managedObjectPoolName,
			String managedObjectPoolSourceClassName) {
		return NodeUtil.getInitialisedNode(managedObjectPoolName, this.managedObjectPoolNodes, this.context,
				() -> this.context.createManagedObjectPoolNode(managedObjectPoolName, this),
				(pool) -> pool.initialise(managedObjectPoolSourceClassName, null));
	}

	@Override
	public SectionManagedObjectPool addManagedObjectPool(String managedObjectPoolName,
			ManagedObjectPoolSource managedObjectPoolSource) {
		return NodeUtil.getInitialisedNode(managedObjectPoolName, this.managedObjectPoolNodes, this.context,
				() -> this.context.createManagedObjectPoolNode(managedObjectPoolName, this),
				(pool) -> pool.initialise(managedObjectPoolSource.getClass().getName(), managedObjectPoolSource));
	}

	@Override
	public SectionFunctionNamespace addSectionFunctionNamespace(String namespaceName,
			String managedFunctionSourceClassName) {
		return NodeUtil.getInitialisedNode(namespaceName, this.namespaceNodes, this.context,
				() -> this.context.createFunctionNamespaceNode(namespaceName, this),
				(work) -> work.initialise(managedFunctionSourceClassName, null));
	}

	@Override
	public SectionFunctionNamespace addSectionFunctionNamespace(String namespaceName,
			ManagedFunctionSource managedFunctionSource) {
		return NodeUtil.getInitialisedNode(namespaceName, this.namespaceNodes, this.context,
				() -> this.context.createFunctionNamespaceNode(namespaceName, this),
				(work) -> work.initialise(managedFunctionSource.getClass().getName(), managedFunctionSource));
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
	public void link(SectionManagedObjectSource managedObjectSource, SectionManagedObjectPool managedObjectPool) {
		LinkUtil.linkPool(managedObjectSource, managedObjectPool, this.context.getCompilerIssues(), this);
	}

	@Override
	public void link(SectionFlowSourceNode flowSourceNode, SectionFlowSinkNode flowSinkNode) {
		LinkUtil.linkFlow(flowSourceNode, flowSinkNode, this.context.getCompilerIssues(), this);
	}

	@Override
	public void link(FunctionFlow functionFlow, SectionFlowSinkNode sectionSinkNode, boolean isSpawnThreadState) {
		if (LinkUtil.linkFlow(functionFlow, sectionSinkNode, this.context.getCompilerIssues(), this)) {
			// Ensure a function flow node
			if (!(functionFlow instanceof FunctionFlowNode)) {
				this.addIssue("Invalid function flow: " + functionFlow + " ["
						+ (functionFlow == null ? null : functionFlow.getClass().getName()) + "]");
				return; // can not load spawning
			}

			// Load whether spawns thread state
			((FunctionFlowNode) functionFlow).setSpawnThreadState(isSpawnThreadState);
		}
	}

	@Override
	public void link(SectionDependencyRequireNode dependencyRequireNode,
			SectionDependencyObjectNode dependencyObjectNode) {
		LinkUtil.linkObject(dependencyRequireNode, dependencyObjectNode, this.context.getCompilerIssues(), this);
	}

	@Override
	public CompileError addIssue(String issueDescription) {
		return this.context.getCompilerIssues().addIssue(this, issueDescription);
	}

	@Override
	public CompileError addIssue(String issueDescription, Throwable cause) {
		return this.context.getCompilerIssues().addIssue(this, issueDescription, cause);
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
	public void setSuperOfficeSection(OfficeSection superSection) {
		this.superSectionNode = (SectionNode) superSection;
	}

	@Override
	public OfficeSubSection getOfficeSubSection(String sectionName) {
		return NodeUtil.getNode(sectionName, this.subSections, () -> this.context.createSectionNode(sectionName, this));
	}

	@Override
	public OfficeSectionFunction getOfficeSectionFunction(String functionName) {
		return NodeUtil.getNode(functionName, this.functionNodes,
				() -> this.context.createFunctionNode(functionName, this));
	}

	@Override
	public OfficeSectionManagedObject getOfficeSectionManagedObject(String managedObjectName) {
		return NodeUtil.getNode(managedObjectName, this.managedObjects,
				() -> this.context.createManagedObjectNode(managedObjectName, this));
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

	/*
	 * ================== OfficeSectionTransformerContext ==================
	 */

	@Override
	public String getSectionSourceClassName() {
		return this.state.sectionSourceClassName;
	}

	@Override
	public String getSectionLocation() {
		return this.state.sectionLocation;
	}

	@Override
	public PropertyList getSectionProperties() {
		return this.propertyList;
	}

	@Override
	public PropertyList createPropertyList() {
		return this.context.createPropertyList();
	}

	@Override
	public void setTransformedOfficeSection(String sectionSourceClassName, String sectionLocation,
			PropertyList sectionProperties) {

		// Load the transformation
		this.state = new InitialisedState(sectionSourceClassName, null, sectionLocation);
		this.propertyList.clear();
		for (Property property : sectionProperties) {
			this.propertyList.addProperty(property.getName(), property.getLabel()).setValue(property.getValue());
		}
	}

	@Override
	public void setTransformedOfficeSection(SectionSource sectionSource, String sectionLocation,
			PropertyList sectionProperties) {

		// Load the transformation
		this.state = new InitialisedState(sectionSource.getClass().getName(), sectionSource, sectionLocation);
		this.propertyList.clear();
		for (Property property : sectionProperties) {
			this.propertyList.addProperty(property.getName(), property.getLabel()).setValue(property.getValue());
		}
	}

}
