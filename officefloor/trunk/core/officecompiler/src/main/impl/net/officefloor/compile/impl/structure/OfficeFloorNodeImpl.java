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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import net.officefloor.compile.impl.officefloor.OfficeFloorSourceContextImpl;
import net.officefloor.compile.impl.officefloor.OfficeFloorTypeImpl;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.impl.util.LoadTypeError;
import net.officefloor.compile.internal.structure.InputManagedObjectNode;
import net.officefloor.compile.internal.structure.ManagedObjectNode;
import net.officefloor.compile.internal.structure.ManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeBindings;
import net.officefloor.compile.internal.structure.OfficeFloorNode;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.internal.structure.SuppliedManagedObjectNode;
import net.officefloor.compile.internal.structure.SupplierNode;
import net.officefloor.compile.internal.structure.TeamNode;
import net.officefloor.compile.officefloor.OfficeFloorLoader;
import net.officefloor.compile.officefloor.OfficeFloorManagedObjectSourceType;
import net.officefloor.compile.officefloor.OfficeFloorPropertyType;
import net.officefloor.compile.officefloor.OfficeFloorTeamSourceType;
import net.officefloor.compile.officefloor.OfficeFloorType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.ManagedObjectTeam;
import net.officefloor.compile.spi.office.OfficeObject;
import net.officefloor.compile.spi.office.OfficeTeam;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.officefloor.ManagingOffice;
import net.officefloor.compile.spi.officefloor.OfficeFloorInputManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.spi.officefloor.OfficeFloorSupplier;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.compile.spi.section.ManagedObjectDependency;
import net.officefloor.compile.spi.section.ManagedObjectFlow;
import net.officefloor.compile.type.TypeContext;
import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.profile.Profiler;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.source.UnknownClassError;
import net.officefloor.frame.spi.source.UnknownPropertyError;
import net.officefloor.frame.spi.source.UnknownResourceError;

/**
 * {@link OfficeFloorNode} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorNodeImpl extends AbstractNode implements
		OfficeFloorNode {

	/**
	 * {@link Class} name of the {@link OfficeFloorSource}.
	 */
	private final String officeFloorSourceClassName;

	/**
	 * Optionally provided instantiated {@link OfficeFloorSource}.
	 */
	private final OfficeFloorSource officeFloorSource;

	/**
	 * Location of the {@link OfficeFloor}.
	 */
	private final String officeFloorLocation;

	/**
	 * {@link PropertyList} to configure the {@link OfficeFloor}.
	 */
	private final PropertyList properties;

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
	}

	/**
	 * Mapping of {@link Profiler} by their {@link Office} name.
	 */
	private final Map<String, Profiler> profilers;

	/**
	 * {@link ManagedObjectSourceNode} instances by their
	 * {@link OfficeFloorManagedObjectSource} name.
	 */
	private final Map<String, ManagedObjectSourceNode> managedObjectSources = new HashMap<String, ManagedObjectSourceNode>();

	/**
	 * {@link InputManagedObjectNode} instances by their
	 * {@link OfficeFloorInputManagedObject} name.
	 */
	private final Map<String, InputManagedObjectNode> inputManagedObjects = new HashMap<String, InputManagedObjectNode>();

	/**
	 * {@link ManagedObjectNode} instances by their
	 * {@link OfficeFloorManagedObject} name.
	 */
	private final Map<String, ManagedObjectNode> managedObjects = new HashMap<String, ManagedObjectNode>();

	/**
	 * {@link SupplierNode} instances by their {@link OfficeFloorSupplier} name.
	 */
	private final Map<String, SupplierNode> suppliers = new HashMap<String, SupplierNode>();

	/**
	 * {@link TeamNode} instances by their {@link OfficeFloorTeam} name.
	 */
	private final Map<String, TeamNode> teams = new HashMap<String, TeamNode>();

	/**
	 * {@link OfficeNode} instances by their {@link DeployedOffice} name.
	 */
	private final Map<String, OfficeNode> offices = new HashMap<String, OfficeNode>();

	/**
	 * Initiate.
	 * 
	 * @param officeFloorSourceClassName
	 *            {@link OfficeFloorSource} class name.
	 * @param officeFloorSource
	 *            Optional instantiated {@link OfficeFloorSource}. May be
	 *            <code>null</code>.
	 * @param officeFloorLocation
	 *            Location of the {@link OfficeFloor}.
	 * @param context
	 *            {@link NodeContext}.
	 * @param profilers
	 *            Mapping of {@link Profiler} by their {@link Office} name.
	 */
	public OfficeFloorNodeImpl(String officeFloorSourceClassName,
			OfficeFloorSource officeFloorSource, String officeFloorLocation,
			NodeContext context, Map<String, Profiler> profilers) {
		this.officeFloorSourceClassName = officeFloorSourceClassName;
		this.officeFloorSource = officeFloorSource;
		this.officeFloorLocation = officeFloorLocation;
		this.context = context;
		this.profilers = profilers;

		// Create the additional objects
		this.properties = this.context.createPropertyList();
	}

	/*
	 * =========================== Node =====================================
	 */

	@Override
	public String getNodeName() {
		return OFFICE_FLOOR_NAME;
	}

	@Override
	public String getNodeType() {
		return TYPE;
	}

	@Override
	public String getLocation() {
		return this.officeFloorLocation;
	}

	@Override
	public Node getParentNode() {
		return null;
	}

	@Override
	public boolean isInitialised() {
		return (this.state != null);
	}

	@Override
	public void initialise() {
		this.state = NodeUtil.initialise(this, this.context, this.state,
				() -> new InitialisedState());
	}

	/*
	 * ===================== ManagedObjectRegistry =============================
	 */

	@Override
	public ManagedObjectNode getManagedObjectNode(String managedObjectName) {
		return NodeUtil.getNode(managedObjectName, this.managedObjects,
				() -> this.context.createManagedObjectNode(managedObjectName));
	}

	@Override
	public ManagedObjectNode addManagedObjectNode(String managedObjectName,
			ManagedObjectScope managedObjectScope,
			ManagedObjectSourceNode managedObjectSourceNode) {
		return NodeUtil.getInitialisedNode(managedObjectName,
				this.managedObjects, this.context, () -> this.context
						.createManagedObjectNode(managedObjectName), (
						managedObject) -> managedObject.initialise(
						managedObjectScope, managedObjectSourceNode));
	}

	/*
	 * ===================== OfficeFloorDeployer =============================
	 */

	@Override
	public OfficeFloorManagedObjectSource addManagedObjectSource(
			String managedObjectSourceName, String managedObjectSourceClassName) {
		return NodeUtil.getInitialisedNode(managedObjectSourceName,
				this.managedObjectSources, this.context, () -> this.context
						.createManagedObjectSourceNode(managedObjectSourceName,
								this),
				(managedObjectSource) -> managedObjectSource.initialise(
						managedObjectSourceClassName, null));
	}

	@Override
	public OfficeFloorManagedObjectSource addManagedObjectSource(
			String managedObjectSourceName,
			ManagedObjectSource<?, ?> managedObjectSource) {
		return NodeUtil.getInitialisedNode(managedObjectSourceName,
				this.managedObjectSources, this.context, () -> this.context
						.createManagedObjectSourceNode(managedObjectSourceName,
								this),
				(managedObjectSourceNode) -> managedObjectSourceNode
						.initialise(managedObjectSource.getClass().getName(),
								managedObjectSource));
	}

	@Override
	public OfficeFloorInputManagedObject addInputManagedObject(
			String inputManagedObjectName) {
		return NodeUtil.getInitialisedNode(inputManagedObjectName,
				this.inputManagedObjects, this.context, () -> this.context
						.createInputManagedNode(inputManagedObjectName, this),
				(inputManagedObject) -> inputManagedObject.initialise());
	}

	@Override
	public OfficeFloorSupplier addSupplier(String supplierName,
			String supplierSourceClassName) {
		return NodeUtil.getInitialisedNode(supplierName, this.suppliers,
				this.context, () -> this.context.createSupplierNode(
						supplierName, supplierSourceClassName, this),
				(supplier) -> supplier.initialise());
	}

	@Override
	public OfficeFloorTeam addTeam(String teamName, String teamSourceClassName) {
		return NodeUtil.getInitialisedNode(teamName, this.teams, this.context,
				() -> this.context.createTeamNode(teamName, this),
				(team) -> team.initialise(teamSourceClassName));
	}

	@Override
	public DeployedOffice addDeployedOffice(String officeName,
			OfficeSource officeSource, String officeLocation) {
		return NodeUtil.getInitialisedNode(officeName, this.offices,
				this.context, () -> this.context.createOfficeNode(officeName,
						this), (office) -> office.initialise(officeSource
						.getClass().getName(), officeSource, officeLocation));
	}

	@Override
	public DeployedOffice addDeployedOffice(String officeName,
			String officeSourceClassName, String officeLocation) {
		return NodeUtil.getInitialisedNode(officeName, this.offices,
				this.context, () -> this.context.createOfficeNode(officeName,
						this), (office) -> office.initialise(
						officeSourceClassName, null, officeLocation));
	}

	@Override
	public void link(OfficeFloorManagedObjectSource managedObjectSource,
			OfficeFloorInputManagedObject inputManagedObject) {
		this.linkManagedObjectSourceInput(managedObjectSource,
				inputManagedObject);
	}

	@Override
	public void link(ManagedObjectTeam team, OfficeFloorTeam officeFloorTeam) {
		this.linkTeam(team, officeFloorTeam);
	}

	@Override
	public void link(ManagedObjectDependency dependency,
			OfficeFloorManagedObject managedObject) {
		this.linkObject(dependency, managedObject);
	}

	@Override
	public void link(ManagedObjectDependency dependency,
			OfficeFloorInputManagedObject inputManagedObject) {
		this.linkObject(dependency, inputManagedObject);
	}

	@Override
	public void link(ManagedObjectFlow flow, DeployedOfficeInput input) {
		this.linkFlow(flow, input);
	}

	@Override
	public void link(ManagingOffice managingOffice, DeployedOffice office) {
		this.linkOffice(managingOffice, office);
	}

	@Override
	public void link(OfficeTeam team, OfficeFloorTeam officeFloorTeam) {
		this.linkTeam(team, officeFloorTeam);
	}

	@Override
	public void link(OfficeObject requiredManagedObject,
			OfficeFloorManagedObject officeFloorManagedObject) {
		this.linkObject(requiredManagedObject, officeFloorManagedObject);
	}

	@Override
	public void link(OfficeObject officeObject,
			OfficeFloorInputManagedObject inputManagedObject) {
		this.linkObject(officeObject, inputManagedObject);
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
	 * ===================== PropertyConfigurable =============================
	 */

	@Override
	public void addProperty(String name, String value) {
		this.properties.addProperty(name).setValue(value);
	}

	/*
	 * ===================== OfficeFloorNode ==================================
	 */

	@Override
	public void addProfiler(String officeName, Profiler profiler) {
		// TODO implement OfficeFloorNode.addProfiler
		throw new UnsupportedOperationException(
				"TODO implement OfficeFloorNode.addProfiler");

	}

	@Override
	public OfficeFloorManagedObjectSource addManagedObjectSource(
			String managedObjectSourceName,
			SuppliedManagedObjectNode suppliedManagedObject) {
		return NodeUtil.getInitialisedNode(managedObjectSourceName,
				this.managedObjectSources, this.context, () -> this.context
						.createManagedObjectSourceNode(managedObjectSourceName,
								suppliedManagedObject),
				(managedObjectSource) -> managedObjectSource.initialise(null,
						null));
	}

	@Override
	public boolean sourceOfficeFloor() {

		// Determine if must instantiate
		OfficeFloorSource source = this.officeFloorSource;
		if (source == null) {

			// Obtain the OfficeFloor source class
			Class<? extends OfficeFloorSource> officeFloorSourceClass = this.context
					.getOfficeFloorSourceClass(this.officeFloorSourceClassName,
							this);
			if (officeFloorSourceClass == null) {
				return false; // must have OfficeFloor source class
			}

			// Instantiate the office floor source
			source = CompileUtil.newInstance(officeFloorSourceClass,
					OfficeFloorSource.class, this,
					this.context.getCompilerIssues());
			if (officeFloorSource == null) {
				return false; // failed to instantiate
			}
		}

		// Create the OfficeFloor source context
		OfficeFloorSourceContext sourceContext = new OfficeFloorSourceContextImpl(
				false, this.officeFloorLocation, this.properties, this,
				this.context);

		try {
			// Source the OfficeFloor
			source.sourceOfficeFloor(this, sourceContext);

		} catch (UnknownPropertyError ex) {
			this.addIssue("Missing property '" + ex.getUnknownPropertyName()
					+ "' for " + OfficeFloorSource.class.getSimpleName() + " "
					+ source.getClass().getName());
			return false; // must have property

		} catch (UnknownClassError ex) {
			this.addIssue("Can not load class '" + ex.getUnknownClassName()
					+ "' for " + OfficeFloorSource.class.getSimpleName() + " "
					+ source.getClass().getName());
			return false; // must have class

		} catch (UnknownResourceError ex) {
			this.addIssue("Can not obtain resource at location '"
					+ ex.getUnknownResourceLocation() + "' for "
					+ OfficeFloorSource.class.getSimpleName() + " "
					+ source.getClass().getName());
			return false; // must have resource

		} catch (LoadTypeError ex) {
			ex.addLoadTypeIssue(this, this.context.getCompilerIssues());
			return false; // must not fail in loading types

		} catch (Throwable ex) {
			this.addIssue(
					"Failed to source " + OfficeFloor.class.getSimpleName()
							+ " from "
							+ OfficeFloorSource.class.getSimpleName()
							+ " (source=" + source.getClass().getName()
							+ ", location=" + officeFloorLocation + ")", ex);
			return false; // must be successful
		}

		// As here, successful
		return true;
	}

	@Override
	public boolean sourceOfficeFloorTree() {

		// Source the OfficeFloor
		boolean isSourced = this.sourceOfficeFloor();
		if (!isSourced) {
			return false;
		}

		// Source all the offices
		isSourced = this.offices
				.values()
				.stream()
				.sorted((a, b) -> CompileUtil.sortCompare(
						a.getDeployedOfficeName(), b.getDeployedOfficeName()))
				.allMatch((office) -> office.sourceOfficeTree());
		if (!isSourced) {
			return false;
		}

		// As here, successfully sourced
		return true;
	}

	@Override
	public OfficeFloorType loadOfficeFloorType(TypeContext typeContext) {

		// Obtain the OfficeFloor source class
		Class<? extends OfficeFloorSource> officeFloorSourceClass = this.context
				.getOfficeFloorSourceClass(this.officeFloorSourceClassName,
						this);

		// Obtain the loader to load properties
		OfficeFloorLoader loader = this.context.getOfficeFloorLoader(this);

		// Load the specification properties
		PropertyList properties = loader
				.loadSpecification(officeFloorSourceClass);

		// Load the required properties
		PropertyList requiredProperties = loader.loadRequiredProperties(
				officeFloorSourceClass, officeFloorLocation, this.properties);
		for (Property property : requiredProperties) {
			String propertyName = property.getName();
			if (properties.getProperty(propertyName) == null) {
				properties.addProperty(propertyName, property.getLabel());
			}
		}

		// Load optional properties
		for (Property property : this.properties) {
			properties.getOrAddProperty(property.getName()).setValue(
					property.getValue());
		}

		// Load the properties
		OfficeFloorPropertyType[] propertyTypes = PropertyNode
				.constructPropertyNodes(properties);

		// Load the managed object source types (in deterministic order)
		OfficeFloorManagedObjectSourceType[] managedObjectSourceTypes = CompileUtil
				.loadTypes(
						this.managedObjectSources,
						(managedObjectSource) -> managedObjectSource
								.getOfficeFloorManagedObjectSourceName(),
						(managedObjectSource) -> managedObjectSource
								.loadOfficeFloorManagedObjectSourceType(typeContext),
						OfficeFloorManagedObjectSourceType[]::new);
		if (managedObjectSourceTypes == null) {
			return null;
		}

		// Load the team sources (in deterministic order)
		OfficeFloorTeamSourceType[] teamTypes = CompileUtil.loadTypes(
				this.teams, (team) -> team.getOfficeFloorTeamName(),
				(team) -> team.loadOfficeFloorTeamSourceType(typeContext),
				OfficeFloorTeamSourceType[]::new);
		if (teamTypes == null) {
			return null;
		}

		// Load and return the type
		return new OfficeFloorTypeImpl(propertyTypes, managedObjectSourceTypes,
				teamTypes);
	}

	@Override
	public OfficeFloor deployOfficeFloor(OfficeFrame officeFrame,
			TypeContext typeContext) {

		// Obtain the OfficeFloor builder
		OfficeFloorBuilder builder = officeFrame
				.createOfficeFloorBuilder(this.officeFloorLocation);

		// Initiate the OfficeFloor builder with compiler details
		this.context.initiateOfficeFloorBuilder(builder);

		// Build the teams (in deterministic order)
		this.teams
				.values()
				.stream()
				.sorted((a, b) -> CompileUtil.sortCompare(
						a.getOfficeFloorTeamName(), b.getOfficeFloorTeamName()))
				.forEachOrdered((team) -> team.buildTeam(builder));

		// Build the offices (in deterministic order)
		Map<OfficeNode, OfficeBindings> officeBindings = new HashMap<OfficeNode, OfficeBindings>();
		this.offices
				.values()
				.stream()
				.sorted((a, b) -> CompileUtil.sortCompare(
						a.getDeployedOfficeName(), b.getDeployedOfficeName()))
				.forEachOrdered((office) -> {

					// Obtain possible profiler to office
						String officeName = office.getDeployedOfficeName();
						Profiler profiler = this.profilers.get(officeName);

						// Build the office
						OfficeBindings bindings = office.buildOffice(builder,
								typeContext, profiler);

						// Keep track of the offices
						officeBindings.put(office, bindings);
					});

		// Obtains the Office bindings for the managed object source
		Function<ManagedObjectSourceNode, OfficeBindings> getOfficeBindings = (
				managedObjectSource) -> {
			OfficeNode managingOffice = managedObjectSource
					.getManagingOfficeNode();
			return officeBindings.get(managingOffice);
		};

		// Build the managed object sources (in deterministic order)
		this.managedObjectSources
				.values()
				.stream()
				.sorted((a, b) -> CompileUtil.sortCompare(
						a.getOfficeManagedObjectSourceName(),
						b.getOfficeManagedObjectSourceName()))
				.forEachOrdered((managedObjectSource) -> {

					// Obtain the managing office for managed object
						OfficeBindings bindings = getOfficeBindings
								.apply(managedObjectSource);
						if (bindings == null) {
							return; // must have office
						}

						// Build the managed object source into the office
						bindings.buildManagedObjectSourceIntoOffice(managedObjectSource);
					});

		// Build the managed objects (in deterministic order)
		this.managedObjects
				.values()
				.stream()
				.sorted((a, b) -> CompileUtil.sortCompare(
						a.getOfficeFloorManagedObjectName(),
						b.getOfficeFloorManagedObjectName()))
				.forEachOrdered((managedObject) -> {
					// Obtain the managed object source
						ManagedObjectSourceNode managedObjectSource = managedObject
								.getManagedObjectSourceNode();

						// Obtain the managing office for managed object
						OfficeBindings bindings = getOfficeBindings
								.apply(managedObjectSource);
						if (bindings == null) {
							return; // must have office
						}

						// Build the managed object into the office
						bindings.buildManagedObjectIntoOffice(managedObject);
					});

		// Build the input managed objects (in deterministic order)
		this.inputManagedObjects
				.values()
				.stream()
				.sorted((a, b) -> CompileUtil.sortCompare(
						a.getOfficeFloorInputManagedObjectName(),
						b.getOfficeFloorInputManagedObjectName()))
				.forEachOrdered((inputManagedObject) -> {
					// Obtain the managed object source
						ManagedObjectSourceNode managedObjectSource = inputManagedObject
								.getBoundManagedObjectSourceNode();
						if (managedObjectSource == null) {
							return; // must have managed object source
						}

						// Obtain the managing office for managed object
						OfficeBindings bindings = getOfficeBindings
								.apply(managedObjectSource);
						if (bindings == null) {
							return; // must have office
						}

						// Build the input managed object into the office
						bindings.buildInputManagedObjectIntoOffice(inputManagedObject);
					});

		// Return the built office floor
		return builder.buildOfficeFloor(new CompilerOfficeFloorIssues());
	}

	/**
	 * Compiler {@link OfficeFloorIssues}.
	 */
	private class CompilerOfficeFloorIssues implements OfficeFloorIssues {

		/*
		 * ================ OfficeFloorIssues ==============================
		 */

		@Override
		public void addIssue(AssetType assetType, String assetName,
				String issueDescription) {
			OfficeFloorNodeImpl.this.context.getCompilerIssues().addIssue(
					OfficeFloorNodeImpl.this, issueDescription);
		}

		@Override
		public void addIssue(AssetType assetType, String assetName,
				String issueDescription, Throwable cause) {
			OfficeFloorNodeImpl.this.context.getCompilerIssues().addIssue(
					OfficeFloorNodeImpl.this, issueDescription, cause);
		}
	}

}