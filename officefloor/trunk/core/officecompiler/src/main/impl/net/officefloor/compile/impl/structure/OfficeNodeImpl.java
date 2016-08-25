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
import net.officefloor.compile.impl.properties.PropertyListImpl;
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
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeInputNode;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.internal.structure.OfficeObjectNode;
import net.officefloor.compile.internal.structure.OfficeOutputNode;
import net.officefloor.compile.internal.structure.OfficeStartNode;
import net.officefloor.compile.internal.structure.TaskTeamNode;
import net.officefloor.compile.internal.structure.SectionNode;
import net.officefloor.compile.internal.structure.TaskNode;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.office.OfficeInputType;
import net.officefloor.compile.office.OfficeManagedObjectType;
import net.officefloor.compile.office.OfficeOutputType;
import net.officefloor.compile.office.OfficeSectionInputType;
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
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.compile.spi.section.ManagedObjectDependency;
import net.officefloor.compile.spi.section.ManagedObjectFlow;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
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
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * {@link OfficeObjectNode} instances by their {@link OfficeObject} name.
	 */
	private final Map<String, OfficeObjectStruct> objects = new HashMap<String, OfficeObjectStruct>();

	/**
	 * {@link TaskTeamNode} instances by their {@link OfficeTeam} name.
	 */
	private final Map<String, TeamStruct> teams = new HashMap<String, TeamStruct>();

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
	 * Flag indicating if in the {@link OfficeFloor} context.
	 */
	private boolean isInOfficeFloorContext = false;

	/**
	 * Location of the {@link OfficeFloor}.
	 */
	private String officeFloorLocation;

	/**
	 * Initialise with all parameters.
	 * 
	 * @param officeName
	 *            Name of the {@link DeployedOffice}.
	 * @param officeSource
	 *            {@link OfficeSource} instance.
	 * @param officeSourceClassName
	 *            {@link OfficeSource} class name.
	 * @param properties
	 *            {@link PropertyList}.
	 * @param officeLocation
	 *            Location of the {@link Office}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	private OfficeNodeImpl(String officeName, OfficeSource officeSource,
			String officeSourceClassName, PropertyList properties,
			String officeLocation, NodeContext context) {
		this.officeName = officeName;
		this.officeSource = officeSource;
		this.officeSourceClassName = (officeSource == null ? officeSourceClassName
				: null);
		this.properties = (properties != null ? properties
				: new PropertyListImpl());
		this.officeLocation = officeLocation;
		this.context = context;
	}

	/**
	 * Allows loading the {@link OfficeType}.
	 * 
	 * @param officeSource
	 *            {@link OfficeSource} instance.
	 * @param properties
	 *            {@link PropertyList} to configure the {@link OfficeSource}.
	 * @param officeLocation
	 *            Location of the {@link Office}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public OfficeNodeImpl(OfficeSource officeSource, PropertyList properties,
			String officeLocation, NodeContext context) {
		this(null, officeSource, null, properties, officeLocation, context);
	}

	/**
	 * Allow adding the {@link DeployedOffice}.
	 * 
	 * @param officeName
	 *            Name of this {@link DeployedOffice}.
	 * @param officeSourceClassName
	 *            Class name of the {@link OfficeSource}.
	 * @param officeLocation
	 *            Location of the {@link Office}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public OfficeNodeImpl(String officeName, String officeSourceClassName,
			String officeLocation, NodeContext context) {
		this(officeName, null, officeSourceClassName, null, officeLocation,
				context);
	}

	/**
	 * Allow adding the {@link DeployedOffice}.
	 * 
	 * @param officeName
	 *            Name of this {@link DeployedOffice}.
	 * @param officeSource
	 *            {@link OfficeSource} instance.
	 * @param officeLocation
	 *            Location of the {@link Office}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public OfficeNodeImpl(String officeName, OfficeSource officeSource,
			String officeLocation, NodeContext context) {
		this(officeName, officeSource, null, null, officeLocation, context);
	}

	/**
	 * Adds an issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 * @param cause
	 *            Cause of the issue.
	 */
	protected void addIssue(String issueDescription, Throwable cause) {
		this.context.getCompilerIssues().addIssue(LocationType.OFFICE,
				this.officeLocation, null, null, issueDescription, cause);
	}

	/*
	 * =================== AbstractNode ================================
	 */

	@Override
	protected void addIssue(String issueDescription) {
		this.context.getCompilerIssues().addIssue(LocationType.OFFICE,
				this.officeLocation, null, null, issueDescription);
	}

	/*
	 * ================== OfficeNode ===================================
	 */

	@Override
	public boolean sourceOffice() {

		// Determine if must instantiate
		OfficeSource source = this.officeSource;
		if (source == null) {

			// Obtain the office source class
			Class<? extends OfficeSource> officeSourceClass = this.context
					.getOfficeSourceClass(this.officeSourceClassName,
							this.officeLocation, this.officeName);
			if (officeSourceClass == null) {
				return false; // must have office source class
			}

			// Obtain the office source
			source = CompileUtil.newInstance(officeSourceClass,
					OfficeSource.class, LocationType.OFFICE,
					this.officeLocation, AssetType.OFFICE, this.officeName,
					this.context.getCompilerIssues());
			if (source == null) {
				return false; // must have office source
			}
		}

		// Create the office source context
		OfficeSourceContext context = new OfficeSourceContextImpl(false,
				this.officeLocation, properties, this.context);

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
			this.addIssue("Failure loading " + ex.getType().getSimpleName()
					+ " from source " + ex.getSourceClassName());
			return false; // must not fail in loading types

		} catch (Throwable ex) {
			this.addIssue(
					"Failed to source " + OfficeType.class.getSimpleName()
							+ " definition from "
							+ OfficeSource.class.getSimpleName() + " "
							+ source.getClass().getName(), ex);
			return false; // must be successful
		}

		// As here, successfully loaded the office
		return true;
	}

	@Override
	public OfficeType loadOfficeType() {

		// Copy the inputs into an array (in deterministic order)
		OfficeInputNode[] inputs = CompileUtil.toSortedArray(
				this.inputs.values(), new OfficeInputNode[0],
				new StringExtractor<OfficeInputNode>() {
					@Override
					public String toString(OfficeInputNode object) {
						return object.getOfficeInputName();
					}
				});
		OfficeInputNode[] originalInputs = new OfficeInputNode[inputs.length];
		System.arraycopy(inputs, 0, originalInputs, 0, inputs.length);

		// Copy the outputs into an array (in deterministic order)
		OfficeOutputNode[] outputs = CompileUtil.toSortedArray(
				this.outputs.values(), new OfficeOutputNode[0],
				new StringExtractor<OfficeOutputNode>() {
					@Override
					public String toString(OfficeOutputNode object) {
						return object.getOfficeOutputName();
					}
				});
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
		List<OfficeInputType> inputTypeList = new LinkedList<OfficeInputType>();
		for (OfficeInputNode input : inputs) {
			if (input == null) {
				continue; // used to handle response
			}
			OfficeInputType inputType = input.getOfficeInputType();
			if (inputType != null) {
				inputTypeList.add(inputType);
			}
		}
		OfficeInputType[] inputTypes = inputTypeList
				.toArray(new OfficeInputType[0]);

		// Create the listing of output types
		List<OfficeOutputType> outputTypeList = new LinkedList<OfficeOutputType>();
		for (OfficeOutputNode output : outputs) {
			if (output == null) {
				continue; // used as response provider
			}
			OfficeOutputType outputType = output.getOfficeOutputType();
			if (outputType != null) {
				outputTypeList.add(outputType);
			}
		}
		OfficeOutputType[] outputTypes = outputTypeList
				.toArray(new OfficeOutputType[0]);

		// Copy architect added object types into an array
		OfficeObjectStruct[] objectStructs = this.objects.values().toArray(
				new OfficeObjectStruct[0]);
		List<OfficeManagedObjectType> managedObjectTypes = new LinkedList<OfficeManagedObjectType>();
		for (int i = 0; i < objectStructs.length; i++) {
			if (objectStructs[i].isAdded) {
				managedObjectTypes.add(objectStructs[i].officeObject);
			}
		}
		OfficeManagedObjectType[] moTypes = managedObjectTypes
				.toArray(new OfficeManagedObjectType[0]);

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
		TeamStruct[] teamStructs = this.teams.values().toArray(
				new TeamStruct[0]);
		List<OfficeTeamType> teamTypeList = new LinkedList<OfficeTeamType>();
		for (int i = 0; i < teamStructs.length; i++) {
			if (teamStructs[i].isAdded) {
				teamTypeList.add(teamStructs[i].team);
			}
		}
		OfficeTeamType[] teamTypes = teamTypeList
				.toArray(new OfficeTeamType[0]);

		// Ensure all teams have names
		for (int i = 0; i < teamTypes.length; i++) {
			OfficeTeamType teamType = teamTypes[i];
			if (CompileUtil.isBlank(teamType.getOfficeTeamName())) {
				this.addIssue("Null name for team " + i);
				return null; // must have name
			}
		}

		// Create the listing of office section inputs
		List<OfficeSectionInputType> sectionInputTypeList = new LinkedList<OfficeSectionInputType>();
		for (SectionNode section : this.sections.values()) {

			// TODO implement
			throw new UnsupportedOperationException(
					"TODO implement creating list of OfficeSectionInputTypes");

			// sectionInputTypeList.addAll(Arrays.asList(section
			// .getOfficeInputTypes()));
		}
		OfficeSectionInputType[] sectionInputTypes = sectionInputTypeList
				.toArray(new OfficeSectionInputType[0]);

		// Load the type
		OfficeType officeType = new OfficeTypeImpl(inputTypes, outputTypes,
				teamTypes, moTypes, sectionInputTypes);

		// Type loaded successfully
		return officeType;
	}

	@Override
	public void addOfficeFloorContext(String officeFloorLocation) {
		this.officeFloorLocation = officeFloorLocation;

		// Flag all the teams within office floor context
		for (TeamStruct struct : this.teams.values()) {
			struct.team.addOfficeFloorContext(this.officeFloorLocation);
		}

		this.isInOfficeFloorContext = true;
	}

	@Override
	public OfficeBuilder buildOffice(OfficeFloorBuilder builder) {

		// Build this office
		OfficeBuilder officeBuilder = builder.addOffice(this.officeName);

		// Register the teams for the office
		for (TeamStruct struct : this.teams.values()) {

			// Obtain the office team name
			String officeTeamName = struct.team.getOfficeTeamName();

			// Obtain the office floor team name
			OfficeFloorTeam officeFloorTeam = LinkUtil.retrieveTarget(
					struct.team, OfficeFloorTeam.class, "Office team "
							+ officeTeamName, LocationType.OFFICE,
					this.officeLocation, AssetType.TEAM, officeTeamName,
					this.context.getCompilerIssues());
			if (officeFloorTeam == null) {
				continue; // office floor team not linked
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
			governance.buildGovernance(officeBuilder);
		}

		// Load the office objects (in deterministic order)
		OfficeObjectStruct[] officeObjects = CompileUtil.toSortedArray(
				this.objects.values(), new OfficeObjectStruct[0],
				new StringExtractor<OfficeObjectStruct>() {
					@Override
					public String toString(OfficeObjectStruct object) {
						return object.officeObject.getOfficeManagedObjectName();
					}
				});

		// Configure governance for the linked office floor managed objects.
		// Must be before managed objects as building them builds dependencies.
		Map<OfficeObjectStruct, BoundManagedObjectNode> officeObjectToBoundMo = new HashMap<OfficeNodeImpl.OfficeObjectStruct, BoundManagedObjectNode>();
		for (OfficeObjectStruct struct : officeObjects) {

			// Obtain the office object name
			OfficeObjectNode objectNode = struct.officeObject;
			String officeObjectName = objectNode.getOfficeObjectName();

			// Obtain the office floor managed object node
			BoundManagedObjectNode managedObjectNode = LinkUtil.retrieveTarget(
					objectNode, BoundManagedObjectNode.class, "Office object "
							+ officeObjectName, LocationType.OFFICE,
					this.officeLocation, AssetType.MANAGED_OBJECT,
					officeObjectName, this.context.getCompilerIssues());
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
		for (OfficeObjectStruct struct : officeObjects) {

			// Obtain the office floor managed object node
			BoundManagedObjectNode managedObjectNode = officeObjectToBoundMo
					.get(struct);
			if (managedObjectNode == null) {
				continue; // not linked and reported in adding governance
			}

			// Determine if already built the managed object
			if (builtManagedObjects.contains(managedObjectNode)) {
				continue; // already built into office (reusing)
			}

			// Have the managed object build itself into this office
			managedObjectNode.buildOfficeManagedObject(this, officeBuilder);
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
			mos.loadManagedObjectType();
		}

		// Build the managed object sources for office (in deterministic order)
		for (ManagedObjectSourceNode mos : managedObjectSources) {
			mos.buildManagedObject(builder, this, officeBuilder);
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
			section.buildSection(builder, this, officeBuilder);
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
					this.context.getSourceContext(), LocationType.OFFICE,
					this.officeLocation, null, null,
					this.context.getCompilerIssues());
			if (type == null) {
				// Failed to obtain escalation type
				this.context.getCompilerIssues().addIssue(LocationType.OFFICE,
						this.officeLocation, null, null,
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
					TaskNode.class, "Escalation " + escalation.type.getName(),
					LocationType.OFFICE, this.officeLocation, null, null,
					this.context.getCompilerIssues());
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
					"Office start-up trigger " + start.getOfficeStartName(),
					LocationType.OFFICE, this.officeLocation, null, null,
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
		// Obtain and return the required object for the name
		OfficeObjectStruct struct = this.objects.get(officeManagedObjectName);
		if (struct == null) {

			// Add the object
			OfficeObjectNode object = new OfficeObjectNodeImpl(
					officeManagedObjectName, objectType, this.officeLocation,
					this.context);
			struct = new OfficeObjectStruct(object, true);
			this.objects.put(officeManagedObjectName, struct);

		} else {

			// Flag the office object added by office architect
			struct.isAdded = true;

			// Added but determine if requires initialising
			if (!struct.officeObject.isInitialised()) {
				// Initialise as not yet initialised
				struct.officeObject.initialise(objectType);
			} else {
				// Object already added and initialised
				this.addIssue("Object " + officeManagedObjectName
						+ " already added");
			}
		}
		return struct.officeObject;
	}

	@Override
	public OfficeInput addInput(String inputName, String parameterType) {
		// Obtain and return the input for the name
		OfficeInputNode input = this.inputs.get(inputName);
		if (input == null) {
			// Create the input
			input = new OfficeInputNodeImpl(inputName, parameterType,
					this.officeLocation, this.context);

			// Add the input
			this.inputs.put(inputName, input);
		} else {
			// Input already added
			this.addIssue("Office input " + inputName + " already added");
		}
		return input;
	}

	@Override
	public OfficeOutput addOutput(String outputName, String argumentType) {
		// Obtain and return the output for the name
		OfficeOutputNode output = this.outputs.get(outputName);
		if (output == null) {
			// Create the output
			output = new OfficeOutputNodeImpl(outputName, argumentType,
					this.officeLocation, this.context);

			// Add the output
			this.outputs.put(outputName, output);
		} else {
			// Output already added
			this.addIssue("Office output " + outputName + " already added");
		}
		return output;
	}

	@Override
	public OfficeTeam addOfficeTeam(String officeTeamName) {
		// Obtain and return the team for the name
		TeamStruct struct = this.teams.get(officeTeamName);
		if (struct == null) {
			// Create the team
			TaskTeamNode team = new TaskTeamNodeImpl(officeTeamName,
					this.officeLocation, this.context);
			struct = new TeamStruct(team, true); // added by architect
			if (this.isInOfficeFloorContext) {
				// Add office floor context as within office floor context
				struct.team.addOfficeFloorContext(this.officeFloorLocation);
			}

			// Add the team
			this.teams.put(officeTeamName, struct);
		} else {
			// Determine if added by architect
			if (!struct.isAdded) {
				// Now added by architect
				struct.isAdded = true;
			} else {
				// Team already added by architect
				this.addIssue("Team " + officeTeamName + " already added");
			}
		}
		return struct.team;
	}

	@Override
	public OfficeSection addOfficeSection(String sectionName,
			String sectionSourceClassName, String sectionLocation,
			PropertyList properties) {
		// Obtain and return the section for the name
		SectionNode section = this.sections.get(sectionName);
		if (section == null) {
			// Create the section and have it loaded
			section = this.context.createSectionNode(sectionName, this)
					.initialise(null, sectionSourceClassName, sectionLocation,
							properties, null);
			// Add the section
			this.sections.put(sectionName, section);
		} else {
			// Section already added
			this.addIssue("Section " + sectionName + " already added");
		}
		return section;
	}

	@Override
	public OfficeSection addOfficeSection(String sectionName,
			SectionSource sectionSource, String sectionLocation,
			PropertyList properties) {
		// Obtain and return the section for the name
		SectionNode section = this.sections.get(sectionName);
		if (section == null) {
			// Create the section and have it loaded
			section = this.context.createSectionNode(sectionName, this)
					.initialise(sectionSource, null, sectionLocation,
							properties, null);
			// Add the section
			this.sections.put(sectionName, section);
		} else {
			// Section already added and initialised
			this.addIssue("Section " + sectionName + " already added");
		}
		return section;
	}

	@Override
	public OfficeManagedObjectSource addOfficeManagedObjectSource(
			String managedObjectSourceName, String managedObjectSourceClassName) {
		// Obtain and return the section managed object source for the name
		ManagedObjectSourceNode managedObjectSource = this.managedObjectSources
				.get(managedObjectSourceName);
		if (managedObjectSource == null) {

			// Create the office managed object source (within office context)
			managedObjectSource = new ManagedObjectSourceNodeImpl(
					managedObjectSourceName, managedObjectSourceClassName,
					LocationType.OFFICE, this.officeLocation, null, this,
					this.managedObjects, this.context);
			managedObjectSource.addOfficeContext(this.officeLocation);

			// Add the office managed object source
			this.managedObjectSources.put(managedObjectSourceName,
					managedObjectSource);

		} else {
			// Managed object source already added
			this.addIssue("Managed object source " + managedObjectSourceName
					+ " already added");
		}
		return managedObjectSource;
	}

	@Override
	public OfficeManagedObjectSource addOfficeManagedObjectSource(
			String managedObjectSourceName,
			ManagedObjectSource<?, ?> managedObjectSource) {
		// Obtain and return the section managed object source for the name
		ManagedObjectSourceNode managedObjectSourceNode = this.managedObjectSources
				.get(managedObjectSourceName);
		if (managedObjectSourceNode == null) {

			// Create the office managed object source (within office context)
			managedObjectSourceNode = new ManagedObjectSourceNodeImpl(
					managedObjectSourceName, managedObjectSource,
					LocationType.OFFICE, this.officeLocation, null, this,
					this.managedObjects, this.context);
			managedObjectSourceNode.addOfficeContext(this.officeLocation);

			// Add the office managed object source
			this.managedObjectSources.put(managedObjectSourceName,
					managedObjectSourceNode);

		} else {
			// Managed object source already added
			this.addIssue("Managed object source " + managedObjectSourceName
					+ " already added");
		}
		return managedObjectSourceNode;
	}

	@Override
	public OfficeGovernance addOfficeGovernance(String governanceName,
			String governanceSourceClassName) {
		// Obtain and return the governance for the name
		GovernanceNode governance = this.governances.get(governanceName);
		if (governance == null) {
			// Add the governance
			governance = new GovernanceNodeImpl(governanceName,
					governanceSourceClassName, this.officeLocation, this,
					this.context);
			this.governances.put(governanceName, governance);
		} else {
			// Governance already added and initialised
			this.addIssue("Governance " + governanceName + " already added");
		}
		return governance;
	}

	@Override
	public OfficeGovernance addOfficeGovernance(String governanceName,
			GovernanceSource<?, ?> governanceSource) {
		// Obtain and return the governance for the name
		GovernanceNode governance = this.governances.get(governanceName);
		if (governance == null) {
			// Add the governance
			governance = new GovernanceNodeImpl(governanceName,
					governanceSource, this.officeLocation, this, this.context);
			this.governances.put(governanceName, governance);
		} else {
			// Governance already added and initialised
			this.addIssue("Governance " + governanceName + " already added");
		}
		return governance;
	}

	@Override
	public OfficeAdministrator addOfficeAdministrator(String administratorName,
			String administratorSourceClassName) {
		// Obtain and return the administrator for the name
		AdministratorNode administrator = this.administrators
				.get(administratorName);
		if (administrator == null) {
			// Add the administrator
			administrator = new AdministratorNodeImpl(administratorName,
					administratorSourceClassName, this.officeLocation,
					this.context);
			this.administrators.put(administratorName, administrator);
		} else {
			// Administrator already added and initialised
			this.addIssue("Administrator " + administratorName
					+ " already added");
		}
		return administrator;
	}

	@Override
	public OfficeAdministrator addOfficeAdministrator(String administratorName,
			AdministratorSource<?, ?> administratorSource) {
		// Obtain and return the administrator for the name
		AdministratorNode administrator = this.administrators
				.get(administratorName);
		if (administrator == null) {
			// Add the administrator
			administrator = new AdministratorNodeImpl(administratorName,
					administratorSource, this.officeLocation, this.context);
			this.administrators.put(administratorName, administrator);
		} else {
			// Administrator already added and initialised
			this.addIssue("Administrator " + administratorName
					+ " already added");
		}
		return administrator;
	}

	@Override
	public OfficeEscalation addOfficeEscalation(String escalationTypeName) {
		// Obtain and return the escalation for type
		EscalationNode escalation = this.escalations.get(escalationTypeName);
		if (escalation == null) {
			// Add the escalation
			escalation = new EscalationNodeImpl(escalationTypeName,
					this.officeLocation, this.context);
			this.escalations.put(escalationTypeName, escalation);
		} else {
			// Escalation already added
			this.addIssue("Escalation " + escalationTypeName + " already added");
		}
		return escalation;
	}

	@Override
	public OfficeStart addOfficeStart(String startName) {
		// Obtain and return the start-up trigger
		OfficeStartNode start = this.starts.get(startName);
		if (start == null) {
			// Add the start
			start = new OfficeStartNodeImpl(startName, this.officeLocation,
					this.context);
			this.starts.put(startName, start);
		} else {
			// Start already added
			this.addIssue("Office start-up trigger " + startName
					+ " already added");
		}
		return start;
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
	public void addIssue(String issueDescription, AssetType assetType,
			String assetName) {
		this.context.getCompilerIssues().addIssue(LocationType.OFFICE,
				this.officeLocation, assetType, assetName, issueDescription);
	}

	@Override
	public void addIssue(String issueDescription, Throwable cause,
			AssetType assetType, String assetName) {
		this.context.getCompilerIssues().addIssue(LocationType.OFFICE,
				this.officeLocation, assetType, assetName, issueDescription,
				cause);
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

		// Ensure in office floor context
		if (!this.isInOfficeFloorContext) {
			throw new IllegalStateException(
					"Must be in office floor context to obtain office input");
		}

		// Obtain the section
		SectionNode section = this.sections.get(sectionName);
		if (section == null) {
			// Add the section
			section = new SectionNodeImpl(sectionName, this, this.context);
			this.sections.put(sectionName, section);
		}

		// Obtain and return the section input
		return section.getDeployedOfficeInput(inputName);
	}

	@Override
	public OfficeObject getDeployedOfficeObject(String officeManagedObjectName) {

		// Ensure in office floor context
		if (!this.isInOfficeFloorContext) {
			throw new IllegalStateException(
					"Must be in office floor context to obtain required managed object");
		}

		// Obtain and return the office object
		OfficeObjectStruct struct = this.objects.get(officeManagedObjectName);
		if (struct == null) {
			// Create the object within the office floor context
			OfficeObjectNode object = new OfficeObjectNodeImpl(
					officeManagedObjectName, this.officeLocation, this.context);
			object.addOfficeFloorContext(this.officeFloorLocation);

			// Add the object
			struct = new OfficeObjectStruct(object, false);
			this.objects.put(officeManagedObjectName, struct);
		}
		return struct.officeObject;
	}

	@Override
	public OfficeTeam getDeployedOfficeTeam(String officeTeamName) {

		// Ensure in office floor context
		if (!this.isInOfficeFloorContext) {
			throw new IllegalStateException(
					"Must be in office floor context to obtain team");
		}

		// Obtain and return the office team
		TeamStruct struct = this.teams.get(officeTeamName);
		if (struct == null) {
			// Create the team within the office floor context
			TaskTeamNode team = new TaskTeamNodeImpl(officeTeamName,
					this.officeLocation, this.context);
			struct = new TeamStruct(team, false); // not added by architect
			team.addOfficeFloorContext(this.officeFloorLocation);

			// Add the office team
			this.teams.put(officeTeamName, struct);
		}
		return struct.team;
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
	 * Structure containing details of an {@link OfficeObjectNode}.
	 */
	private class OfficeObjectStruct {

		/**
		 * {@link OfficeObjectNode}.
		 */
		public final OfficeObjectNode officeObject;

		/**
		 * Flag indicating if has been added by {@link OfficeArchitect}.
		 */
		public boolean isAdded = false;

		/**
		 * Initiate.
		 * 
		 * @param officeObject
		 *            {@link OfficeObjectNode}.
		 * @param isAdded
		 *            <code>true</code> if has been added by
		 *            {@link OfficeArchitect}.
		 */
		public OfficeObjectStruct(OfficeObjectNode officeObject, boolean isAdded) {
			this.officeObject = officeObject;
			this.isAdded = isAdded;
		}
	}

	/**
	 * Structure containing details of an {@link TaskTeamNode}.
	 */
	private class TeamStruct {

		/**
		 * {@link TaskTeamNode}.
		 */
		public final TaskTeamNode team;

		/**
		 * Flag indicating if has been added by {@link OfficeArchitect}.
		 */
		public boolean isAdded = false;

		/**
		 * Initiate.
		 * 
		 * @param team
		 *            {@link TaskTeamNode}.
		 * @param isAdded
		 *            <code>true</code> if has been added by
		 *            {@link OfficeArchitect}.
		 */
		public TeamStruct(TaskTeamNode team, boolean isAdded) {
			this.team = team;
			this.isAdded = isAdded;
		}
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