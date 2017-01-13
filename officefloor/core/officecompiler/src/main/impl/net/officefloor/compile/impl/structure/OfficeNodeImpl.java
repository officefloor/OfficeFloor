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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.compile.impl.office.OfficeSourceContextImpl;
import net.officefloor.compile.impl.office.OfficeTypeImpl;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.impl.util.LinkUtil;
import net.officefloor.compile.impl.util.LoadTypeError;
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
import net.officefloor.compile.internal.structure.OfficeBindings;
import net.officefloor.compile.internal.structure.OfficeFloorNode;
import net.officefloor.compile.internal.structure.OfficeInputNode;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.internal.structure.OfficeObjectNode;
import net.officefloor.compile.internal.structure.OfficeOutputNode;
import net.officefloor.compile.internal.structure.OfficeStartNode;
import net.officefloor.compile.internal.structure.OfficeTeamNode;
import net.officefloor.compile.internal.structure.SectionNode;
import net.officefloor.compile.internal.structure.TaskNode;
import net.officefloor.compile.internal.structure.TeamNode;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.office.OfficeAvailableSectionInputType;
import net.officefloor.compile.office.OfficeInputType;
import net.officefloor.compile.office.OfficeManagedObjectType;
import net.officefloor.compile.office.OfficeOutputType;
import net.officefloor.compile.office.OfficeTeamType;
import net.officefloor.compile.office.OfficeType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.administration.source.AdministratorSource;
import net.officefloor.compile.spi.governance.source.GovernanceSource;
import net.officefloor.compile.spi.office.ManagedObjectTeam;
import net.officefloor.compile.spi.office.OfficeAdministrator;
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
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.profile.Profiler;
import net.officefloor.frame.api.source.UnknownClassError;
import net.officefloor.frame.api.source.UnknownPropertyError;
import net.officefloor.frame.api.source.UnknownResourceError;
import net.officefloor.frame.internal.structure.ManagedObjectScope;

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
	 * {@link PropertyList} to source the {@link Office}.
	 */
	private final PropertyList properties;

	/**
	 * Parent {@link OfficeFloorNode}.
	 */
	private final OfficeFloorNode officeFloor;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * Initialised state.
	 */
	private InitialisedState state;

	/**
	 * Initialised state.
	 */
	private static class InitialisedState {

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
		 * Location of the {@link Office}.
		 */
		private final String officeLocation;

		/**
		 * Instantiate.
		 * 
		 * @param officeSourceClassName
		 *            Class name of the {@link OfficeSource}.
		 * @param officeSource
		 *            {@link OfficeSource} instance to use rather than
		 *            instantiating the {@link OfficeSource} class.
		 * @param officeLocation
		 *            Location of the {@link Office}.
		 */
		public InitialisedState(String officeSourceClassName, OfficeSource officeSource, String officeLocation) {
			this.officeSourceClassName = officeSourceClassName;
			this.officeSource = officeSource;
			this.officeLocation = officeLocation;
		}
	}

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
	 * @param officeFloor
	 *            Parent {@link OfficeFloorNode}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public OfficeNodeImpl(String officeName, OfficeFloorNode officeFloor, NodeContext context) {
		this.officeName = officeName;
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
		return (this.state == null ? "[NOT INITIALISED]"
				: NodeUtil.getLocation(this.state.officeSourceClassName, this.state.officeSource,
						this.state.officeLocation));
	}

	@Override
	public Node getParentNode() {
		return this.officeFloor;
	}

	@Override
	public boolean isInitialised() {
		return (this.state != null);
	}

	@Override
	public void initialise(String officeSourceClassName, OfficeSource officeSource, String officeLocation) {
		this.state = NodeUtil.initialise(this, this.context, this.state,
				() -> new InitialisedState(officeSourceClassName, officeSource, officeLocation));
	}

	/*
	 * ================== ManagedObjectRegistry ==================
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

		// Ensure the office is initialised
		if (!this.isInitialised()) {
			this.context.getCompilerIssues().addIssue(this, "Office is not initialised");
			return false; // must be initialised
		}

		// Determine if must instantiate
		OfficeSource source = this.state.officeSource;
		if (source == null) {

			// Obtain the office source class
			Class<? extends OfficeSource> officeSourceClass = this.context
					.getOfficeSourceClass(this.state.officeSourceClassName, this);
			if (officeSourceClass == null) {
				return false; // must have office source class
			}

			// Obtain the office source
			source = CompileUtil.newInstance(officeSourceClass, OfficeSource.class, this,
					this.context.getCompilerIssues());
			if (source == null) {
				return false; // must have office source
			}
		}

		// Create the office source context
		OfficeSourceContext context = new OfficeSourceContextImpl(false, this.state.officeLocation, properties, this,
				this.context);

		try {
			// Source the office
			source.sourceOffice(this, context);

		} catch (UnknownPropertyError ex) {
			this.addIssue("Missing property '" + ex.getUnknownPropertyName() + "' for "
					+ OfficeSource.class.getSimpleName() + " " + source.getClass().getName());
			return false; // must have property

		} catch (UnknownClassError ex) {
			this.addIssue("Can not load class '" + ex.getUnknownClassName() + "' for "
					+ OfficeSource.class.getSimpleName() + " " + source.getClass().getName());
			return false; // must have class

		} catch (UnknownResourceError ex) {
			this.addIssue("Can not obtain resource at location '" + ex.getUnknownResourceLocation() + "' for "
					+ OfficeSource.class.getSimpleName() + " " + source.getClass().getName());
			return false; // must have resource

		} catch (LoadTypeError ex) {
			ex.addLoadTypeIssue(this, this.context.getCompilerIssues());
			return false; // must not fail in loading types

		} catch (Throwable ex) {
			this.addIssue("Failed to source " + OfficeType.class.getSimpleName() + " definition from "
					+ OfficeSource.class.getSimpleName() + " " + source.getClass().getName(), ex);
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
		isSourced = CompileUtil.sourceTree(this.sections, (section) -> section.getOfficeSectionName(),
				(section) -> section.sourceSection());
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
		isSourced = CompileUtil.sourceTree(this.sections, (section) -> section.getOfficeSectionName(),
				(section) -> section.sourceSectionTree());
		if (!isSourced) {
			return false; // must source all top level sections
		}

		// As here, successfully loaded the office
		return true;
	}

	@Override
	public OfficeType loadOfficeType(TypeContext typeContext) {

		// Copy the inputs into an array (in deterministic order)
		OfficeInputNode[] inputs = this.inputs.values().stream()
				.sorted((a, b) -> CompileUtil.sortCompare(a.getOfficeInputName(), b.getOfficeInputName()))
				.toArray(OfficeInputNode[]::new);
		OfficeInputNode[] originalInputs = new OfficeInputNode[inputs.length];
		System.arraycopy(inputs, 0, originalInputs, 0, inputs.length);

		// Copy the outputs into an array (in deterministic order)
		OfficeOutputNode[] outputs = this.outputs.values().stream()
				.sorted((a, b) -> CompileUtil.sortCompare(a.getOfficeOutputName(), b.getOfficeOutputName()))
				.toArray(OfficeOutputNode[]::new);
		OfficeOutputNode[] originalOutputs = new OfficeOutputNode[outputs.length];
		System.arraycopy(outputs, 0, originalOutputs, 0, outputs.length);

		// Remove inputs used to handle response
		for (int o = 0; o < outputs.length; o++) {
			OfficeOutputNode output = originalOutputs[o];
			LinkSynchronousNode possibleInput = output.getLinkedSynchronousNode();
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
			LinkSynchronousNode possibleOuput = input.getLinkedSynchronousNode();
			if (possibleOuput != null) {
				for (int o = 0; o < outputs.length; o++) {
					if (outputs[o] == possibleOuput) {
						outputs[o] = null; // not include in output types
					}
				}
			}
		}

		// Create the listing of input types
		OfficeInputType[] inputTypes = CompileUtil.loadTypes(
				Arrays.asList(inputs).stream().filter((input) -> input != null), (input) -> input.getOfficeInputName(),
				(input) -> input.loadOfficeInputType(typeContext), OfficeInputType[]::new);
		if (inputTypes == null) {
			return null;
		}

		// Create the listing of output types
		OfficeOutputType[] outputTypes = CompileUtil.loadTypes(
				Arrays.asList(outputs).stream().filter((output) -> output != null),
				(output) -> output.getOfficeOutputName(), (output) -> output.loadOfficeOutputType(typeContext),
				OfficeOutputType[]::new);
		if (outputTypes == null) {
			return null;
		}

		// Create the listing of architect added object types
		OfficeManagedObjectType[] moTypes = CompileUtil.loadTypes(this.objects,
				(object) -> object.getOfficeObjectName(), (object) -> object.loadOfficeManagedObjectType(typeContext),
				OfficeManagedObjectType[]::new);
		if (moTypes == null) {
			return null;
		}

		// Copy architect added team types into an array
		OfficeTeamType[] teamTypes = CompileUtil.loadTypes(this.teams, (team) -> team.getOfficeTeamName(),
				(team) -> team.loadOfficeTeamType(typeContext), OfficeTeamType[]::new);
		if (teamTypes == null) {
			return null;
		}

		// Create the listing of office section inputs
		OfficeAvailableSectionInputType[][] sectionInputTypesArrays = CompileUtil.loadTypes(this.sections,
				(section) -> section.getOfficeSectionName(),
				(section) -> section.loadOfficeAvailableSectionInputTypes(typeContext),
				OfficeAvailableSectionInputType[][]::new);
		if (sectionInputTypesArrays == null) {
			return null;
		}
		OfficeAvailableSectionInputType[] sectionInputTypes = Arrays.asList(sectionInputTypesArrays).stream()
				.flatMap((types) -> Arrays.asList(types).stream()).toArray(OfficeAvailableSectionInputType[]::new);

		// Create and return the type
		return new OfficeTypeImpl(inputTypes, outputTypes, teamTypes, moTypes, sectionInputTypes);
	}

	@Override
	public OfficeBindings buildOffice(OfficeFloorBuilder builder, TypeContext typeContext, Profiler profiler) {

		// Build this office
		OfficeBuilder officeBuilder = builder.addOffice(this.officeName);

		// Load the profiler (if provided)
		if (profiler != null) {
			officeBuilder.setProfiler(profiler);
		}

		// Create the bindings for the office
		OfficeBindings officeBindings = new OfficeBindingsImpl(this, officeBuilder, builder, typeContext);

		// Register the teams for the office
		this.teams.values().stream()
				.sorted((a, b) -> CompileUtil.sortCompare(a.getOfficeTeamName(), b.getOfficeTeamName()))
				.forEachOrdered((team) -> {
					// Obtain the office team name
					String officeTeamName = team.getOfficeTeamName();

					// Obtain the OfficeFloor team name
					TeamNode officeFloorTeam = LinkUtil.retrieveTarget(team, TeamNode.class,
							this.context.getCompilerIssues());
					if (officeFloorTeam == null) {
						return; // OfficeFloor team not linked
					}
					String officeFloorTeamName = officeFloorTeam.getOfficeFloorTeamName();

					// Register the team to the office
					officeBuilder.registerTeam(officeTeamName, officeFloorTeamName);
				});

		// Build the governance for the office (in deterministic order)
		this.governances.values().stream()
				.sorted((a, b) -> CompileUtil.sortCompare(a.getOfficeGovernanceName(), b.getOfficeGovernanceName()))
				.forEachOrdered((governance) -> governance.buildGovernance(officeBuilder, typeContext));

		// Load the office objects (in deterministic order)
		this.objects.values().stream()
				.sorted((a, b) -> CompileUtil.sortCompare(a.getOfficeObjectName(), b.getOfficeObjectName()))
				.forEachOrdered((objectNode) -> {

					// Obtain the managed object node
					BoundManagedObjectNode managedObjectNode = LinkUtil.retrieveTarget(objectNode,
							BoundManagedObjectNode.class, this.context.getCompilerIssues());
					if (managedObjectNode == null) {
						return;
					}

					// Load governances for linked Managed Object
					GovernanceNode[] governances = objectNode.getGovernances();
					for (GovernanceNode governance : governances) {
						managedObjectNode.addGovernance(governance, this);
					}

					// Build the managed object into the office
					officeBindings.buildManagedObjectIntoOffice(managedObjectNode);
				});

		// Build the managed object sources (in deterministic order)
		this.managedObjectSources.values().stream()
				.sorted((a, b) -> CompileUtil.sortCompare(a.getOfficeManagedObjectSourceName(),
						b.getOfficeManagedObjectSourceName()))
				.forEachOrdered((managedObjectSource) -> officeBindings
						.buildManagedObjectSourceIntoOffice(managedObjectSource));

		// Load the managed objects for office (in deterministic order)
		this.managedObjects.values().stream().sorted(
				(a, b) -> CompileUtil.sortCompare(a.getOfficeManagedObjectName(), b.getOfficeManagedObjectName()))
				.forEachOrdered((mos) -> officeBindings.buildManagedObjectIntoOffice(mos));

		// Build the sections of the office (in deterministic order)
		this.sections.values().stream()
				.sorted((a, b) -> CompileUtil.sortCompare(a.getOfficeSectionName(), b.getOfficeSectionName()))
				.forEachOrdered((section) -> {
					section.buildSection(officeBuilder, officeBindings, typeContext);
				});

		// Build the administrators for the office (in deterministic order)
		this.administrators.values().stream().sorted(
				(a, b) -> CompileUtil.sortCompare(a.getOfficeAdministratorName(), b.getOfficeAdministratorName()))
				.forEachOrdered((admin) -> {
					admin.buildAdministrator(officeBuilder);
				});

		// Build the list of escalations of the office
		List<EscalationStruct> escalationStructs = new LinkedList<OfficeNodeImpl.EscalationStruct>();
		for (EscalationNode node : this.escalations.values()) {
			// Obtain the escalation type
			String escalationTypeName = node.getOfficeEscalationType();
			Class<? extends Throwable> type = CompileUtil.obtainClass(escalationTypeName, Throwable.class, null,
					this.context.getRootSourceContext(), this, this.context.getCompilerIssues());
			if (type == null) {
				// Failed to obtain escalation type
				this.context.getCompilerIssues().addIssue(this, "Unknown escalation type " + escalationTypeName);
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
				return String.CASE_INSENSITIVE_ORDER.compare(a.type.getName(), b.type.getName());
			}
		});

		// Build the escalation handling for the office (in deterministic order)
		for (EscalationStruct escalation : escalationStructs) {

			// Obtain the target task
			TaskNode task = LinkUtil.findTarget(escalation.node, TaskNode.class, this.context.getCompilerIssues());
			if (task == null) {
				continue; // task not linked
			}

			// Build the escalation handling
			String workName = task.getWorkNode().getQualifiedWorkName();
			String taskName = task.getOfficeTaskName();
			officeBuilder.addEscalation(escalation.type, workName, taskName);
		}

		// Build the start-up triggers for the office (in deterministic order)
		this.starts.values().stream()
				.sorted((a, b) -> CompileUtil.sortCompare(a.getOfficeStartName(), b.getOfficeStartName()))
				.forEachOrdered((start) -> {
					// Obtain the target task
					TaskNode task = LinkUtil.findTarget(start, TaskNode.class, this.context.getCompilerIssues());
					if (task == null) {
						return; // task not linked
					}

					// Build the start-up trigger
					String workName = task.getWorkNode().getQualifiedWorkName();
					String taskName = task.getOfficeTaskName();
					officeBuilder.addStartupTask(workName, taskName);
				});

		// Return the office bindings
		return officeBindings;
	}

	/*
	 * ===================== OfficeArchitect ================================
	 */

	@Override
	public OfficeObject addOfficeObject(String officeManagedObjectName, String objectType) {
		return NodeUtil.getInitialisedNode(officeManagedObjectName, this.objects, this.context,
				() -> this.context.createOfficeObjectNode(officeManagedObjectName, this),
				(managedObject) -> managedObject.initialise(objectType));
	}

	@Override
	public OfficeInput addOfficeInput(String inputName, String parameterType) {
		return NodeUtil.getInitialisedNode(inputName, this.inputs, this.context,
				() -> this.context.createOfficeInputNode(inputName, this), (input) -> input.initialise(parameterType));
	}

	@Override
	public OfficeOutput addOfficeOutput(String outputName, String argumentType) {
		return NodeUtil.getInitialisedNode(outputName, this.outputs, this.context,
				() -> this.context.createOfficeOutputNode(outputName, this),
				(output) -> output.initialise(argumentType));
	}

	@Override
	public OfficeTeam addOfficeTeam(String officeTeamName) {
		return NodeUtil.getInitialisedNode(officeTeamName, this.teams, this.context,
				() -> this.context.createOfficeTeamNode(officeTeamName, this), (team) -> team.initialise());
	}

	@Override
	public OfficeSection addOfficeSection(String sectionName, String sectionSourceClassName, String sectionLocation) {
		return NodeUtil.getInitialisedNode(sectionName, this.sections, context,
				() -> this.context.createSectionNode(sectionName, this),
				(section) -> section.initialise(sectionSourceClassName, null, sectionLocation));
	}

	@Override
	public OfficeSection addOfficeSection(String sectionName, SectionSource sectionSource, String sectionLocation) {
		return NodeUtil.getInitialisedNode(sectionName, this.sections, context,
				() -> this.context.createSectionNode(sectionName, this),
				(section) -> section.initialise(sectionSource.getClass().getName(), sectionSource, sectionLocation));
	}

	@Override
	public OfficeManagedObjectSource addOfficeManagedObjectSource(String managedObjectSourceName,
			String managedObjectSourceClassName) {
		return NodeUtil.getInitialisedNode(managedObjectSourceName, this.managedObjectSources, this.context,
				() -> this.context.createManagedObjectSourceNode(managedObjectSourceName, this),
				(managedObjectSource) -> managedObjectSource.initialise(managedObjectSourceClassName, null));
	}

	@Override
	public OfficeManagedObjectSource addOfficeManagedObjectSource(String managedObjectSourceName,
			ManagedObjectSource<?, ?> managedObjectSource) {
		return NodeUtil.getInitialisedNode(managedObjectSourceName, this.managedObjectSources, this.context,
				() -> this.context.createManagedObjectSourceNode(managedObjectSourceName, this),
				(managedObjectSourceNode) -> managedObjectSourceNode
						.initialise(managedObjectSource.getClass().getName(), managedObjectSource));
	}

	@Override
	public OfficeGovernance addOfficeGovernance(String governanceName, String governanceSourceClassName) {
		return NodeUtil.getInitialisedNode(governanceName, this.governances, this.context,
				() -> this.context.createGovernanceNode(governanceName, this),
				(governance) -> governance.initialise(governanceSourceClassName, null));
	}

	@Override
	public OfficeGovernance addOfficeGovernance(String governanceName, GovernanceSource<?, ?> governanceSource) {
		return NodeUtil.getInitialisedNode(governanceName, this.governances, this.context,
				() -> this.context.createGovernanceNode(governanceName, this),
				(governance) -> governance.initialise(governanceSource.getClass().getName(), governanceSource));
	}

	@Override
	public OfficeAdministrator addOfficeAdministrator(String administratorName, String administratorSourceClassName) {
		return NodeUtil.getInitialisedNode(administratorName, this.administrators, this.context,
				() -> this.context.createAdministratorNode(administratorName, this),
				(administrator) -> administrator.initialise(administratorSourceClassName, null));
	}

	@Override
	public OfficeAdministrator addOfficeAdministrator(String administratorName,
			AdministratorSource<?, ?> administratorSource) {
		return NodeUtil.getInitialisedNode(administratorName, this.administrators, this.context,
				() -> this.context.createAdministratorNode(administratorName, this), (administrator) -> administrator
						.initialise(administratorSource.getClass().getName(), administratorSource));
	}

	@Override
	public OfficeEscalation addOfficeEscalation(String escalationTypeName) {
		return NodeUtil.getInitialisedNode(escalationTypeName, this.escalations, this.context,
				() -> this.context.createEscalationNode(escalationTypeName, this),
				(escalation) -> escalation.initialise());
	}

	@Override
	public OfficeStart addOfficeStart(String startName) {
		return NodeUtil.getInitialisedNode(startName, this.starts, this.context,
				() -> this.context.createOfficeStartNode(startName, this), (start) -> start.initialise());
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
	public void link(OfficeSectionObject sectionObject, OfficeManagedObject managedObject) {
		this.linkObject(sectionObject, managedObject);
	}

	@Override
	public void link(OfficeSectionObject sectionObject, OfficeObject managedObject) {
		this.linkObject(sectionObject, managedObject);
	}

	@Override
	public void link(ManagedObjectDependency dependency, OfficeManagedObject managedObject) {
		this.linkObject(dependency, managedObject);
	}

	@Override
	public void link(ManagedObjectDependency dependency, OfficeObject managedObject) {
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
		this.context.getCompilerIssues().addIssue(this, issueDescription, cause);
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
	public DeployedOfficeInput getDeployedOfficeInput(String sectionName, String inputName) {
		SectionNode section = NodeUtil.getNode(sectionName, this.sections,
				() -> this.context.createSectionNode(sectionName, this));
		return section.getDeployedOfficeInput(inputName);
	}

	@Override
	public OfficeObject getDeployedOfficeObject(String officeManagedObjectName) {
		return NodeUtil.getNode(officeManagedObjectName, this.objects,
				() -> this.context.createOfficeObjectNode(officeManagedObjectName, this));
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
		return LinkUtil.linkOfficeNode(this, node, this.context.getCompilerIssues(),
				(link) -> this.linkedOfficeNode = link);
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
		public EscalationStruct(Class<? extends Throwable> type, EscalationNode node) {
			this.type = type;
			this.node = node;
		}
	}

}