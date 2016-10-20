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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.officefloor.compile.impl.office.OfficeSourceContextImpl;
import net.officefloor.compile.impl.office.OfficeTypeImpl;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.impl.util.LinkUtil;
import net.officefloor.compile.impl.util.LoadTypeError;
import net.officefloor.compile.impl.util.StringExtractor;
import net.officefloor.compile.internal.structure.AdministratorNode;
import net.officefloor.compile.internal.structure.BoundManagedObjectNode;
import net.officefloor.compile.internal.structure.EscalationNode;
import net.officefloor.compile.internal.structure.GovernanceNode;
import net.officefloor.compile.internal.structure.LinkOfficeNode;
import net.officefloor.compile.internal.structure.LinkSynchronousNode;
import net.officefloor.compile.internal.structure.ManagedObjectNode;
import net.officefloor.compile.internal.structure.ManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeFloorNode;
import net.officefloor.compile.internal.structure.OfficeInputNode;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.internal.structure.OfficeObjectNode;
import net.officefloor.compile.internal.structure.OfficeOutputNode;
import net.officefloor.compile.internal.structure.OfficeStartNode;
import net.officefloor.compile.internal.structure.OfficeTeamNode;
import net.officefloor.compile.internal.structure.SectionNode;
import net.officefloor.compile.internal.structure.TaskNode;
import net.officefloor.compile.internal.structure.TaskTeamNode;
import net.officefloor.compile.internal.structure.TeamNode;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.office.OfficeAvailableSectionInputType;
import net.officefloor.compile.office.OfficeInputType;
import net.officefloor.compile.office.OfficeManagedObjectType;
import net.officefloor.compile.office.OfficeOutputType;
import net.officefloor.compile.office.OfficeTeamType;
import net.officefloor.compile.office.OfficeType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.governance.source.GovernanceSource;
import net.officefloor.compile.spi.office.ManagedObjectTeam;
import net.officefloor.compile.spi.office.OfficeAdministrator;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeEscalation;
import net.officefloor.compile.spi.office.OfficeGovernance;
import net.officefloor.compile.spi.office.OfficeInput;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.spi.office.OfficeObject;
import net.officefloor.compile.spi.office.OfficeOutput;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionObject;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.spi.office.OfficeStart;
import net.officefloor.compile.spi.office.OfficeTeam;
import net.officefloor.compile.spi.office.TaskTeam;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.section.ManagedObjectDependency;
import net.officefloor.compile.spi.section.ManagedObjectFlow;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.type.TypeContext;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.administration.source.AdministratorSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.source.UnknownClassError;
import net.officefloor.frame.spi.source.UnknownPropertyError;
import net.officefloor.frame.spi.source.UnknownResourceError;

/**
 * {@link OfficeNode} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeNodeImpl extends AbstractNode implements OfficeNode {

	/**
	 * Name of this {@link DeployedOffice}.
	 */
	private final String officeName;

	/**
	 * Class name of the {@link OfficeSource}.
	 */
	private final String officeSourceClassName;

	/**
	 * {@link OfficeSource} instance to use rather than instantiating the
	 * {@link OfficeSource} class.
	 */
	private final OfficeSource officeSource;

	/**
	 * {@link PropertyList} to source the {@link Office}.
	 */
	private final PropertyList properties;

	/**
	 * Location of the {@link Office}.
	 */
	private final String officeLocation;

	/**
	 * Parent {@link OfficeFloorNode}.
	 */
	private final OfficeFloorNode officeFloor;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * {@link OfficeObjectNode} instances by their {@link OfficeObject} name.
	 */
	private final Map<String, OfficeObjectNode> objects = new HashMap<String, OfficeObjectNode>();

	/**
	 * {@link OfficeTeamNode} instances by their {@link OfficeTeam} name.
	 */
	private final Map<String, OfficeTeamNode> teams = new HashMap<String, OfficeTeamNode>();

	/**
	 * {@link OfficeInputNode} instances by their name.
	 */
	private final Map<String, OfficeInputNode> inputs = new HashMap<String, OfficeInputNode>();

	/**
	 * {@link OfficeOutputNode} instances by their name.
	 */
	private final Map<String, OfficeOutputNode> outputs = new HashMap<String, OfficeOutputNode>();

	/**
	 * {@link SectionNode} instances by their {@link OfficeSection} name.
	 */
	private final Map<String, SectionNode> sections = new HashMap<String, SectionNode>();

	/**
	 * {@link ManagedObjectSourceNode} instances by their
	 * {@link OfficeManagedObjectSource} name.
	 */
	private final Map<String, ManagedObjectSourceNode> managedObjectSources = new HashMap<String, ManagedObjectSourceNode>();

	/**
	 * {@link ManagedObjectNode} instances by their {@link OfficeManagedObject}
	 * name.
	 */
	private final Map<String, ManagedObjectNode> managedObjects = new HashMap<String, ManagedObjectNode>();

	/**
	 * {@link AdministratorNode} instances by their {@link OfficeAdministrator}
	 * name.
	 */
	private final Map<String, AdministratorNode> administrators = new HashMap<String, AdministratorNode>();

	/**
	 * {@link GovernanceNode} instances by their {@link OfficeGovernance} name.
	 */
	private final Map<String, GovernanceNode> governances = new HashMap<String, GovernanceNode>();

	/**
	 * {@link EscalationNode} instances by their {@link OfficeEscalation} type.
	 */
	private final Map<String, EscalationNode> escalations = new HashMap<String, EscalationNode>();

	/**
	 * {@link OfficeStartNode} instances by their {@link OfficeStart} name.
	 */
	private final Map<String, OfficeStartNode> starts = new HashMap<String, OfficeStartNode>();

	/**
	 * Initialise with all parameters.
	 * 
	 * @param officeName
	 *            Name of the {@link DeployedOffice}.
	 * @param officeSourceClassName
	 *            {@link OfficeSource} class name.
	 * @param officeSource
	 *            Optional instantiated {@link OfficeSource}. May be
	 *            <code>null</code>.
	 * @param officeLocation
	 *            Location of the {@link Office}.
	 * @param officeFloor
	 *            Parent {@link OfficeFloorNode}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public OfficeNodeImpl(String officeName, String officeSourceClassName,
			OfficeSource officeSource, String officeLocation,
			OfficeFloorNode officeFloor, NodeContext context) {
		this.officeName = officeName;
		this.officeSourceClassName = officeSourceClassName;
		this.officeSource = officeSource;
		this.officeLocation = officeLocation;
		this.officeFloor = officeFloor;
		this.context = context;

		// Create additional objects
		this.properties = this.context.createPropertyList();
	}

	/*
	 * ================== Node ===================================
	 */

	@Override
	public String getNodeName() {
		return this.officeName;
	}

	@Override
	public String getNodeType() {
		return TYPE;
	}

	@Override
	public String getLocation() {
		return this.officeLocation;
	}

	@Override
	public Node getParentNode() {
		return this.officeFloor;
	}

	@Override
	public boolean isInitialised() {
		// TODO implement Node.isInitialised
		throw new UnsupportedOperationException(
				"TODO implement Node.isInitialised");

	}

	@Override
	public void initialise() {
		// TODO implement OfficeNode.initialise
		throw new UnsupportedOperationException(
				"TODO implement OfficeNode.initialise");

	}

	/*
	 * ================== ManagedObjectRegistry ==================
	 */

	@Override
	public ManagedObjectNode getOrCreateManagedObjectNode(
			String managedObjectName, ManagedObjectScope managedObjectScope,
			ManagedObjectSourceNode managedObjectSourceNode) {
		return NodeUtil.getInitialisedNode(managedObjectName,
				this.managedObjects, this.context, () -> this.context
						.createManagedObjectNode(managedObjectName,
								managedObjectScope, managedObjectSourceNode), (
						managedObject) -> managedObject.initialise());
	}

	/*
	 * ================== OfficeNode ===================================
	 */

	@Override
	public OfficeFloorNode getOfficeFloorNode() {
		return this.officeFloor;
	}

	/**
	 * Sources the {@link Office}.
	 * 
	 * @return <true> to indicate sourced, otherwise <false> with issues
	 *         reported to the {@link CompilerIssues}.
	 */
	private boolean sourceOffice() {

		// Determine if must instantiate
		OfficeSource source = this.officeSource;
		if (source == null) {

			// Obtain the office source class
			Class<? extends OfficeSource> officeSourceClass = this.context
					.getOfficeSourceClass(this.officeSourceClassName, this);
			if (officeSourceClass == null) {
				return false; // must have office source class
			}

			// Obtain the office source
			source = CompileUtil.newInstance(officeSourceClass,
					OfficeSource.class, this, this.context.getCompilerIssues());
			if (source == null) {
				return false; // must have office source
			}
		}

		// Create the office source context
		OfficeSourceContext context = new OfficeSourceContextImpl(false,
				this.officeLocation, properties, this, this.context);

		try {
			// Source the office
			source.sourceOffice(this, context);

		} catch (UnknownPropertyError ex) {
			this.addIssue("Missing property '" + ex.getUnknownPropertyName()
					+ "' for " + OfficeSource.class.getSimpleName() + " "
					+ source.getClass().getName());
			return false; // must have property

		} catch (UnknownClassError ex) {
			this.addIssue("Can not load class '" + ex.getUnknownClassName()
					+ "' for " + OfficeSource.class.getSimpleName() + " "
					+ source.getClass().getName());
			return false; // must have class

		} catch (UnknownResourceError ex) {
			this.addIssue("Can not obtain resource at location '"
					+ ex.getUnknownResourceLocation() + "' for "
					+ OfficeSource.class.getSimpleName() + " "
					+ source.getClass().getName());
			return false; // must have resource

		} catch (LoadTypeError ex) {
			ex.addLoadTypeIssue(this, this.context.getCompilerIssues());
			return false; // must not fail in loading types

		} catch (Throwable ex) {
			this.addIssue(
					"Failed to source " + OfficeType.class.getSimpleName()
							+ " definition from "
							+ OfficeSource.class.getSimpleName() + " "
							+ source.getClass().getName(), ex);
			return false; // must be successful
		}

		// As here, successfully sourced
		return true;
	}

	@Override
	public boolean sourceOfficeWithTopLevelSections() {

		// Source the office
		boolean isSourced = this.sourceOffice();
		if (!isSourced) {
			return false;
		}

		// Source the top level sections
		isSourced = this.sections
				.values()
				.stream()
				.sorted((a, b) -> CompileUtil.sortCompare(
						a.getOfficeSectionName(), b.getOfficeSectionName()))
				.allMatch((section) -> section.sourceSection());
		if (!isSourced) {
			return false; // must source all top level sections
		}

		// As here, successfully loaded the office
		return true;
	}

	@Override
	public boolean sourceOfficeTree() {

		// Source the office
		boolean isSourced = this.sourceOffice();
		if (!isSourced) {
			return false;
		}

		// Source the all section trees
		isSourced = this.sections
				.values()
				.stream()
				.sorted((a, b) -> CompileUtil.sortCompare(
						a.getOfficeSectionName(), b.getOfficeSectionName()))
				.allMatch((section) -> section.sourceSectionTree());
		if (!isSourced) {
			return false; // must source all top level sections
		}

		// As here, successfully loaded the office
		return true;
	}

	@Override
	public OfficeType loadOfficeType(TypeContext typeContext) {

		// Copy the inputs into an array (in deterministic order)
		OfficeInputNode[] inputs = this.inputs
				.values()
				.stream()
				.sorted((a, b) -> CompileUtil.sortCompare(
						a.getOfficeInputName(), b.getOfficeInputName()))
				.toArray(OfficeInputNode[]::new);
		OfficeInputNode[] originalInputs = new OfficeInputNode[inputs.length];
		System.arraycopy(inputs, 0, originalInputs, 0, inputs.length);

		// Copy the outputs into an array (in deterministic order)
		OfficeOutputNode[] outputs = this.outputs
				.values()
				.stream()
				.sorted((a, b) -> CompileUtil.sortCompare(
						a.getOfficeOutputName(), b.getOfficeOutputName()))
				.toArray(OfficeOutputNode[]::new);
		OfficeOutputNode[] originalOutputs = new OfficeOutputNode[outputs.length];
		System.arraycopy(outputs, 0, originalOutputs, 0, outputs.length);

		// Remove inputs used to handle response
		for (int o = 0; o < outputs.length; o++) {
			OfficeOutputNode output = originalOutputs[o];
			LinkSynchronousNode possibleInput = output
					.getLinkedSynchronousNode();
			if (possibleInput != null) {
				for (int i = 0; i < inputs.length; i++) {
					if (inputs[i] == possibleInput) {
						inputs[i] = null; // not include in input types
					}
				}
			}
		}

		// Remove outputs used to send responses
		for (int i = 0; i < inputs.length; i++) {
			OfficeInputNode input = originalInputs[i];
			LinkSynchronousNode possibleOuput = input
					.getLinkedSynchronousNode();
			if (possibleOuput != null) {
				for (int o = 0; o < outputs.length; o++) {
					if (outputs[o] == possibleOuput) {
						outputs[o] = null; // not include in output types
					}
				}
			}
		}

		// Create the listing of input types
		OfficeInputType[] inputTypes = Arrays.asList(inputs).stream()
				.filter((input) -> (input != null))
				.map((input) -> input.loadOfficeInputType(typeContext))
				.filter((inputType) -> (inputType != null))
				.toArray(OfficeInputType[]::new);

		// Create the listing of output types
		OfficeOutputType[] outputTypes = Arrays.asList(outputs).stream()
				.filter((output) -> (output != null))
				.map((output) -> output.loadOfficeOutputType(typeContext))
				.filter((outputType) -> (outputType != null))
				.toArray(OfficeOutputType[]::new);

		// Create the listing of architect added object types
		OfficeManagedObjectType[] moTypes = this.objects
				.values()
				.stream()
				.sorted((a, b) -> CompileUtil.sortCompare(
						a.getOfficeObjectName(), b.getOfficeObjectName()))
				.map((object) -> object
						.loadOfficeManagedObjectType(typeContext))
				.filter((type) -> (type != null))
				.toArray(OfficeManagedObjectType[]::new);

		// Ensure all objects have names and types
		for (int i = 0; i < moTypes.length; i++) {
			OfficeManagedObjectType moType = moTypes[i];

			// Ensure have name
			String moName = moType.getOfficeManagedObjectName();
			if (CompileUtil.isBlank(moName)) {
				this.addIssue("Null name for managed object " + i);
				return null; // must have name
			}

			// Ensure have type
			if (CompileUtil.isBlank(moType.getObjectType())) {
				this.addIssue("Null type for managed object " + i + " (name="
						+ moName + ")");
				return null; // must have type
			}
		}

		// Copy architect added team types into an array
		OfficeTeamType[] teamTypes = this.teams
				.values()
				.stream()
				.sorted((a, b) -> CompileUtil.sortCompare(
						a.getOfficeTeamName(), b.getOfficeTeamName()))
				.map((team) -> team.loadOfficeTeamType(typeContext))
				.filter((type) -> (type != null))
				.toArray(OfficeTeamType[]::new);

		// Ensure all teams have names
		for (int i = 0; i < teamTypes.length; i++) {
			OfficeTeamType teamType = teamTypes[i];
			if (CompileUtil.isBlank(teamType.getOfficeTeamName())) {
				this.addIssue("Null name for team " + i);
				return null; // must have name
			}
		}

		// Create the listing of office section inputs
		OfficeAvailableSectionInputType[] sectionInputTypes = this.sections
				.values()
				.stream()
				.map((section) -> section
						.loadOfficeAvailableSectionInputTypes(typeContext))
				.filter((types) -> (types != null))
				.flatMap((types) -> Arrays.asList(types).stream())
				.sorted((a, b) -> CompileUtil.sortCompare(
						a.getOfficeSectionName() + "."
								+ a.getOfficeSectionInputName(),
						b.getOfficeSectionName() + "."
								+ b.getOfficeSectionInputName()))
				.toArray(OfficeAvailableSectionInputType[]::new);

		// Load the type
		OfficeType officeType = new OfficeTypeImpl(inputTypes, outputTypes,
				teamTypes, moTypes, sectionInputTypes);

		// Type loaded successfully
		return officeType;
	}

	@Override
	public OfficeBuilder buildOffice(OfficeFloorBuilder builder,
			TypeContext typeContext) {

		// Build this office
		OfficeBuilder officeBuilder = builder.addOffice(this.officeName);

		// Register the teams for the office
		for (OfficeTeamNode team : this.teams.values()) {

			// Obtain the office team name
			String officeTeamName = team.getOfficeTeamName();

			// Obtain the OfficeFloor team name
			TeamNode officeFloorTeam = LinkUtil.retrieveTarget(team,
					TeamNode.class, this.context.getCompilerIssues());
			if (officeFloorTeam == null) {
				continue; // OfficeFloor team not linked
			}
			String officeFloorTeamName = officeFloorTeam
					.getOfficeFloorTeamName();

			// Register the team to the office
			officeBuilder.registerTeam(officeTeamName, officeFloorTeamName);
		}

		// Build the governance for the office (in deterministic order)
		GovernanceNode[] governanceNodes = CompileUtil.toSortedArray(
				this.governances.values(), new GovernanceNode[0],
				new StringExtractor<GovernanceNode>() {
					@Override
					public String toString(GovernanceNode object) {
						return object.getOfficeGovernanceName();
					}
				});
		for (GovernanceNode governance : governanceNodes) {
			governance.buildGovernance(officeBuilder, typeContext);
		}

		// Load the office objects (in deterministic order)
		OfficeObjectNode[] officeObjects = CompileUtil.toSortedArray(
				this.objects.values(), new OfficeObjectNode[0],
				(object) -> object.getOfficeObjectName());

		// Configure governance for the linked office floor managed objects.
		// Must be before managed objects as building them builds dependencies.
		Map<OfficeObjectNode, BoundManagedObjectNode> officeObjectToBoundMo = new HashMap<OfficeObjectNode, BoundManagedObjectNode>();
		for (OfficeObjectNode objectNode : officeObjects) {

			// Obtain the office object name
			String officeObjectName = objectNode.getOfficeObjectName();

			// Obtain the office floor managed object node
			BoundManagedObjectNode managedObjectNode = LinkUtil.retrieveTarget(
					objectNode, BoundManagedObjectNode.class,
					this.context.getCompilerIssues());
			if (managedObjectNode == null) {
				continue; // office floor managed object not linked
			}

			// Load governances for linked office floor managed object
			GovernanceNode[] governances = objectNode.getGovernances();
			for (GovernanceNode governance : governances) {
				managedObjectNode.addGovernance(governance, this);
			}
		}

		// Build the office floor managed objects for the office.
		// Ensure the managed objects are only built into the office once.
		Set<BoundManagedObjectNode> builtManagedObjects = new HashSet<BoundManagedObjectNode>();
		for (OfficeObjectNode objectNode : officeObjects) {

			// Obtain the OfficeFloor managed object node
			BoundManagedObjectNode managedObjectNode = officeObjectToBoundMo
					.get(objectNode);
			if (managedObjectNode == null) {
				continue; // not linked and reported in adding governance
			}

			// Determine if already built the managed object
			if (builtManagedObjects.contains(managedObjectNode)) {
				continue; // already built into office (reusing)
			}

			// Have the managed object build itself into this office
			managedObjectNode.buildOfficeManagedObject(this, officeBuilder,
					typeContext);
			builtManagedObjects.add(managedObjectNode);
		}

		// Load the managed object sources for office (in deterministic order)
		ManagedObjectSourceNode[] managedObjectSources = CompileUtil
				.toSortedArray(this.managedObjectSources.values(),
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
			typeContext.getOrLoadManagedObjectType(mos);
		}

		// Build the managed object sources for office (in deterministic order)
		for (ManagedObjectSourceNode mos : managedObjectSources) {
			mos.buildManagedObject(builder, this, officeBuilder, typeContext);
		}

		// Build the sections of the office (in deterministic order)
		SectionNode[] sections = CompileUtil.toSortedArray(
				this.sections.values(), new SectionNode[0],
				new StringExtractor<SectionNode>() {
					@Override
					public String toString(SectionNode section) {
						return section.getOfficeSectionName();
					}
				});
		for (SectionNode section : sections) {
			section.buildSection(builder, officeBuilder, typeContext);
		}

		// Build the administrators for the office (in deterministic order)
		AdministratorNode[] admins = CompileUtil.toSortedArray(
				this.administrators.values(), new AdministratorNode[0],
				new StringExtractor<AdministratorNode>() {
					@Override
					public String toString(AdministratorNode object) {
						return object.getOfficeAdministratorName();
					}
				});
		for (AdministratorNode admin : admins) {
			admin.buildAdministrator(officeBuilder);
		}

		// Build the list of escalations of the office
		List<EscalationStruct> escalationStructs = new LinkedList<OfficeNodeImpl.EscalationStruct>();
		for (EscalationNode node : this.escalations.values()) {
			// Obtain the escalation type
			String escalationTypeName = node.getOfficeEscalationType();
			Class<? extends Throwable> type = CompileUtil.obtainClass(
					escalationTypeName, Throwable.class, null,
					this.context.getRootSourceContext(), this,
					this.context.getCompilerIssues());
			if (type == null) {
				// Failed to obtain escalation type
				this.context.getCompilerIssues().addIssue(this,
						"Unknown escalation type " + escalationTypeName);
				continue; // ignore this escalation
			}

			// Add the escalation struct
			escalationStructs.add(new EscalationStruct(type, node));
		}

		// Order by more specific escalation first. Allows finer handling first.
		Collections.sort(escalationStructs, new Comparator<EscalationStruct>() {
			@Override
			public int compare(EscalationStruct a, EscalationStruct b) {

				// Compare based on type
				if (a.type != b.type) {
					if (a.type.isAssignableFrom(b.type)) {
						return 1; // a is super type
					} else if (b.type.isAssignableFrom(a.type)) {
						return -1; // b is super type
					}
				}

				// Either same type or no inheritance relationship.
				// Therefore sort alphabetically to have ordering.
				return String.CASE_INSENSITIVE_ORDER.compare(a.type.getName(),
						b.type.getName());
			}
		});

		// Build the escalation handling for the office (in deterministic order)
		for (EscalationStruct escalation : escalationStructs) {

			// Obtain the target task
			TaskNode task = LinkUtil.findTarget(escalation.node,
					TaskNode.class, this.context.getCompilerIssues());
			if (task == null) {
				continue; // task not linked
			}

			// Build the escalation handling
			String workName = task.getWorkNode().getQualifiedWorkName();
			String taskName = task.getOfficeTaskName();
			officeBuilder.addEscalation(escalation.type, workName, taskName);
		}

		// Build the start-up triggers for the office (in deterministic order)
		OfficeStartNode[] startTriggers = CompileUtil.toSortedArray(
				this.starts.values(), new OfficeStartNode[0],
				new StringExtractor<OfficeStartNode>() {
					@Override
					public String toString(OfficeStartNode object) {
						return object.getOfficeStartName();
					}
				});
		for (OfficeStartNode start : startTriggers) {

			// Obtain the target task
			TaskNode task = LinkUtil.findTarget(start, TaskNode.class,
					this.context.getCompilerIssues());
			if (task == null) {
				continue; // task not linked
			}

			// Build the start-up trigger
			String workName = task.getWorkNode().getQualifiedWorkName();
			String taskName = task.getOfficeTaskName();
			officeBuilder.addStartupTask(workName, taskName);
		}

		// Return the office builder
		return officeBuilder;
	}

	/*
	 * ===================== OfficeArchitect ================================
	 */

	@Override
	public OfficeObject addOfficeObject(String officeManagedObjectName,
			String objectType) {
		return NodeUtil.getInitialisedNode(officeManagedObjectName,
				this.objects, this.context, () -> this.context
						.createOfficeObjectNode(officeManagedObjectName, this),
				(managedObject) -> managedObject.initialise(objectType));
	}

	@Override
	public OfficeInput addOfficeInput(String inputName, String parameterType) {
		return NodeUtil.getInitialisedNode(inputName, this.inputs,
				this.context, () -> this.context.createOfficeInputNode(
						inputName, parameterType, this), (input) -> input
						.initialise());
	}

	@Override
	public OfficeOutput addOfficeOutput(String outputName, String argumentType) {
		return NodeUtil.getInitialisedNode(outputName, this.outputs,
				this.context, () -> this.context.createOfficeOutputNode(
						outputName, argumentType, this), (output) -> output
						.initialise());
	}

	@Override
	public OfficeTeam addOfficeTeam(String officeTeamName) {
		return NodeUtil.getInitialisedNode(officeTeamName, this.teams,
				this.context,
				() -> this.context.createOfficeTeamNode(officeTeamName, this),
				(team) -> team.initialise());
	}

	@Override
	public OfficeSection addOfficeSection(String sectionName,
			String sectionSourceClassName, String sectionLocation) {
		return NodeUtil.getInitialisedNode(sectionName, this.sections, context,
				() -> this.context.createSectionNode(sectionName, this), (
						section) -> section.initialise(sectionSourceClassName,
						null, sectionLocation));
	}

	@Override
	public OfficeSection addOfficeSection(String sectionName,
			SectionSource sectionSource, String sectionLocation) {
		return NodeUtil.getInitialisedNode(sectionName, this.sections, context,
				() -> this.context.createSectionNode(sectionName, this), (
						section) -> section.initialise(sectionSource.getClass()
						.getName(), sectionSource, sectionLocation));
	}

	@Override
	public OfficeManagedObjectSource addOfficeManagedObjectSource(
			String managedObjectSourceName, String managedObjectSourceClassName) {
		return NodeUtil.getInitialisedNode(managedObjectSourceName,
				this.managedObjectSources, this.context, () -> this.context
						.createManagedObjectSourceNode(managedObjectSourceName,
								managedObjectSourceClassName, null, this), (
						managedObjectSource) -> managedObjectSource
						.initialise());
	}

	@Override
	public OfficeManagedObjectSource addOfficeManagedObjectSource(
			String managedObjectSourceName,
			ManagedObjectSource<?, ?> managedObjectSource) {
		return NodeUtil.getInitialisedNode(managedObjectSourceName,
				this.managedObjectSources, this.context, () -> this.context
						.createManagedObjectSourceNode(managedObjectSourceName,
								managedObjectSource.getClass().getName(),
								managedObjectSource, this), (
						managedObjectSourceNode) -> managedObjectSourceNode
						.initialise());
	}

	@Override
	public OfficeGovernance addOfficeGovernance(String governanceName,
			String governanceSourceClassName) {
		return NodeUtil.getInitialisedNode(governanceName, this.governances,
				this.context, () -> this.context.createGovernanceNode(
						governanceName, governanceSourceClassName, null, this),
				(governance) -> governance.initialise());
	}

	@Override
	public OfficeGovernance addOfficeGovernance(String governanceName,
			GovernanceSource<?, ?> governanceSource) {
		return NodeUtil.getInitialisedNode(governanceName, this.governances,
				this.context, () -> this.context.createGovernanceNode(
						governanceName, governanceSource.getClass().getName(),
						governanceSource, this), (governance) -> governance
						.initialise());
	}

	@Override
	public OfficeAdministrator addOfficeAdministrator(String administratorName,
			String administratorSourceClassName) {
		return NodeUtil.getInitialisedNode(administratorName,
				this.administrators, this.context, () -> this.context
						.createAdministratorNode(administratorName,
								administratorSourceClassName, null, this), (
						administrator) -> administrator.initialise());
	}

	@Override
	public OfficeAdministrator addOfficeAdministrator(String administratorName,
			AdministratorSource<?, ?> administratorSource) {
		return NodeUtil.getInitialisedNode(administratorName,
				this.administrators, this.context, () -> this.context
						.createAdministratorNode(administratorName,
								administratorSource.getClass().getName(),
								administratorSource, this),
				(administrator) -> administrator.initialise());
	}

	@Override
	public OfficeEscalation addOfficeEscalation(String escalationTypeName) {
		return NodeUtil.getInitialisedNode(escalationTypeName,
				this.escalations, this.context, () -> this.context
						.createEscalationNode(escalationTypeName, this), (
						escalation) -> escalation.initialise());
	}

	@Override
	public OfficeStart addOfficeStart(String startName) {
		return NodeUtil.getInitialisedNode(startName, this.starts,
				this.context, () -> this.context.createOfficeStartNode(
						startName, this), (start) -> start.initialise());
	}

	@Override
	public void link(OfficeInput input, OfficeOutput output) {
		this.linkSynchronous(input, output);
	}

	@Override
	public void link(OfficeInput input, OfficeSectionInput sectionInput) {
		this.linkFlow(input, sectionInput);
	}

	@Override
	public void link(OfficeOutput output, OfficeInput input) {
		this.linkSynchronous(output, input);
	}

	@Override
	public void link(OfficeSectionOutput sectionOutput, OfficeOutput output) {
		this.linkFlow(sectionOutput, output);
	}

	@Override
	public void link(OfficeSectionObject sectionObject,
			OfficeManagedObject managedObject) {
		this.linkObject(sectionObject, managedObject);
	}

	@Override
	public void link(OfficeSectionObject sectionObject,
			OfficeObject managedObject) {
		this.linkObject(sectionObject, managedObject);
	}

	@Override
	public void link(ManagedObjectDependency dependency,
			OfficeManagedObject managedObject) {
		this.linkObject(dependency, managedObject);
	}

	@Override
	public void link(ManagedObjectDependency dependency,
			OfficeObject managedObject) {
		this.linkObject(dependency, managedObject);
	}

	@Override
	public void link(ManagedObjectFlow flow, OfficeSectionInput input) {
		this.linkFlow(flow, input);
	}

	@Override
	public void link(OfficeSectionOutput output, OfficeSectionInput input) {
		this.linkFlow(output, input);
	}

	@Override
	public void link(OfficeEscalation escalation, OfficeSectionInput input) {
		this.linkFlow(escalation, input);
	}

	@Override
	public void link(TaskTeam team, OfficeTeam officeTeam) {
		this.linkTeam(team, officeTeam);
	}

	@Override
	public void link(ManagedObjectTeam team, OfficeTeam officeTeam) {
		this.linkTeam(team, officeTeam);
	}

	@Override
	public void link(OfficeGovernance governance, OfficeTeam officeTeam) {
		this.linkTeam(governance, officeTeam);
	}

	@Override
	public void link(OfficeAdministrator administrator, OfficeTeam officeTeam) {
		this.linkTeam(administrator, officeTeam);
	}

	@Override
	public void link(OfficeStart start, OfficeSectionInput input) {
		this.linkFlow(start, input);
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
	 * =================== DeployedOffice =====================================
	 */

	@Override
	public String getDeployedOfficeName() {
		return this.officeName;
	}

	@Override
	public void addProperty(String name, String value) {
		this.properties.addProperty(name).setValue(value);
	}

	@Override
	public DeployedOfficeInput getDeployedOfficeInput(String sectionName,
			String inputName) {
		SectionNode section = NodeUtil.getNode(sectionName, this.sections,
				() -> this.context.createSectionNode(sectionName, this));
		return section.getDeployedOfficeInput(inputName);
	}

	@Override
	public OfficeObject getDeployedOfficeObject(String officeManagedObjectName) {
		return NodeUtil.getNode(officeManagedObjectName, this.objects,
				() -> this.context.createOfficeObjectNode(
						officeManagedObjectName, this));
	}

	@Override
	public OfficeTeam getDeployedOfficeTeam(String officeTeamName) {
		return NodeUtil.getNode(officeTeamName, this.teams,
				() -> this.context.createOfficeTeamNode(officeTeamName, this));
	}

	/*
	 * ================== LinkOfficeNode ===============================
	 */

	/**
	 * Linked {@link LinkOfficeNode}.
	 */
	private LinkOfficeNode linkedOfficeNode;

	@Override
	public boolean linkOfficeNode(LinkOfficeNode node) {

		// Link
		this.linkedOfficeNode = node;
		return true;
	}

	@Override
	public LinkOfficeNode getLinkedOfficeNode() {
		return this.linkedOfficeNode;
	}

	/**
	 * Structure containing details of the {@link EscalationNode}.
	 * 
	 * 
	 * @author Daniel Sagenschneider
	 */
	private class EscalationStruct {

		/**
		 * Type of {@link Escalation}.
		 */
		public final Class<? extends Throwable> type;

		/**
		 * {@link EscalationNode}.
		 */
		public final EscalationNode node;

		/**
		 * Initiate.
		 * 
		 * @param type
		 *            Type of {@link Escalation}.
		 * @param node
		 *            {@link EscalationNode}.
		 */
		public EscalationStruct(Class<? extends Throwable> type,
				EscalationNode node) {
			this.type = type;
			this.node = node;
		}
	}

}