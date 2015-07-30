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
package net.officefloor.autowire.impl.supplier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireObject;
import net.officefloor.autowire.AutoWireTeam;
import net.officefloor.autowire.ManagedObjectSourceWirer;
import net.officefloor.autowire.ManagedObjectSourceWirerContext;
import net.officefloor.autowire.impl.AutoWirePropertiesImpl;
import net.officefloor.autowire.spi.supplier.source.SupplierSource;
import net.officefloor.autowire.spi.supplier.source.SupplierSourceContext;
import net.officefloor.autowire.supplier.SuppliedManagedObject;
import net.officefloor.autowire.supplier.SuppliedManagedObjectDependencyType;
import net.officefloor.autowire.supplier.SuppliedManagedObjectFlowType;
import net.officefloor.autowire.supplier.SuppliedManagedObjectTeam;
import net.officefloor.autowire.supplier.SuppliedManagedObjectTeamType;
import net.officefloor.autowire.supplier.SuppliedManagedObjectType;
import net.officefloor.autowire.supplier.SupplierType;
import net.officefloor.autowire.supplier.SupplyOrder;
import net.officefloor.compile.impl.properties.PropertyListSourceProperties;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectFlowType;
import net.officefloor.compile.managedobject.ManagedObjectLoader;
import net.officefloor.compile.managedobject.ManagedObjectTeamType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.officefloor.OfficeFloorSupplier;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.source.UnknownClassError;
import net.officefloor.frame.spi.source.UnknownPropertyError;
import net.officefloor.frame.spi.source.UnknownResourceError;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.source.TeamSource;

/**
 * {@link SupplierSourceContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class SupplierSourceContextImpl extends SourceContextImpl implements
		SupplierSourceContext {

	/**
	 * {@link OfficeFloorSupplier} name.
	 */
	private final String supplierName;

	/**
	 * {@link OfficeFloor} location.
	 */
	private final String officeFloorLocation;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * {@link SuppliedAutoWireObject} instances.
	 */
	private final List<SuppliedAutoWireObject> suppliedObjects = new LinkedList<SuppliedAutoWireObject>();

	/**
	 * Initiate.
	 * 
	 * @param isLoadingType
	 *            Indicates if loading type.
	 * @param supplierName
	 *            {@link OfficeFloorSupplier} name.
	 * @param officeFloorLocation
	 *            {@link OfficeFloor} location.
	 * @param propertyList
	 *            {@link PropertyList}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public SupplierSourceContextImpl(boolean isLoadingType,
			String supplierName, String officeFloorLocation,
			PropertyList propertyList, NodeContext context) {
		super(isLoadingType, context.getSourceContext(),
				new PropertyListSourceProperties(propertyList));
		this.supplierName = supplierName;
		this.officeFloorLocation = officeFloorLocation;
		this.context = context;
	}

	/**
	 * Loads the {@link SupplierType} from the added {@link ManagedObject}
	 * instances, along with filling {@link SupplyOrder} instances.
	 * 
	 * @param <S>
	 *            {@link SupplierSource} type.
	 * @param supplierSourceClass
	 *            {@link SupplierSource} class.
	 * @param propertyList
	 *            {@link PropertyList}.
	 * @param supplyOrders
	 *            {@link SupplyOrder} instances.
	 * @return {@link SupplierType}.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public <S extends SupplierSource> SupplierType loadSupplier(
			Class<S> supplierSourceClass, PropertyList propertyList,
			SupplyOrder... supplyOrders) {

		// Ensure all supply orders have auto-wire specified
		List<SupplyOrder> filteredSupplyOrders = new ArrayList<SupplyOrder>(
				supplyOrders.length);
		for (int i = 0; i < supplyOrders.length; i++) {
			SupplyOrder supplyOrder = supplyOrders[i];

			// Ensure has auto-wire
			if (supplyOrder.getAutoWire() == null) {
				this.addIssue(SupplyOrder.class.getSimpleName() + " " + i
						+ " must have an " + AutoWire.class.getSimpleName());
				continue; // do not include
			}

			// As here valid, so include
			filteredSupplyOrders.add(supplyOrder);
		}
		supplyOrders = filteredSupplyOrders
				.toArray(new SupplyOrder[filteredSupplyOrders.size()]);

		// Instantiate the supplier source
		S supplierSource = CompileUtil.newInstance(supplierSourceClass,
				SupplierSource.class, LocationType.OFFICE_FLOOR,
				this.officeFloorLocation, null, null,
				this.context.getCompilerIssues());
		if (supplierSource == null) {
			return null; // failed to instantiate
		}

		try {
			// Source the supplier
			supplierSource.supply(this);

		} catch (UnknownPropertyError ex) {
			this.addIssue("Missing property '" + ex.getUnknownPropertyName()
					+ "' for " + SupplierSource.class.getSimpleName() + " "
					+ supplierSourceClass.getName());
			return null; // must have property

		} catch (UnknownClassError ex) {
			this.addIssue("Can not load class '" + ex.getUnknownClassName()
					+ "' for " + SupplierSource.class.getSimpleName() + " "
					+ supplierSourceClass.getName());
			return null; // must have class

		} catch (UnknownResourceError ex) {
			this.addIssue("Can not obtain resource at location '"
					+ ex.getUnknownResourceLocation() + "' for "
					+ SupplierSource.class.getSimpleName() + " "
					+ supplierSourceClass.getName());
			return null; // must have resource

		} catch (Throwable ex) {
			this.addIssue(
					"Failed to source " + SupplierType.class.getSimpleName()
							+ " definition from "
							+ SupplierSource.class.getSimpleName() + " "
							+ supplierSourceClass.getName(), ex);
			return null; // must be successful
		}

		// Load the supplied managed object types
		int index = 0;
		List<SuppliedManagedObjectType> managedObjects = new ArrayList<SuppliedManagedObjectType>(
				this.suppliedObjects.size());
		for (SuppliedAutoWireObject object : this.suppliedObjects) {

			// Increment to identify the managed object
			index++;

			// Ensure have auto-wiring
			if ((object.autoWiring == null) || (object.autoWiring.length == 0)) {
				this.addIssue("Must provide auto-wiring for "
						+ ManagedObject.class.getSimpleName() + " " + index);
				continue; // can not load supplied managed object
			}

			// Obtain the name of the managed object
			String managedObjectName = object.autoWiring[0].getQualifiedType();
			String issueSuffix = " for " + ManagedObject.class.getSimpleName()
					+ " " + managedObjectName;

			// Ensure have managed object source
			if (object.managedObjectSource == null) {
				this.addIssue("Must provide a "
						+ ManagedObjectSource.class.getSimpleName()
						+ issueSuffix);
				continue; // can not load supplied managed object
			}

			// Obtain the managed object loader
			ManagedObjectLoader managedObjectLoader = this.context
					.getManagedObjectLoader(LocationType.OFFICE_FLOOR,
							this.officeFloorLocation, managedObjectName);

			// Load the managed object type
			ManagedObjectType moType = managedObjectLoader
					.loadManagedObjectType(object.managedObjectSource,
							object.properties);
			if (moType == null) {
				this.addIssue("Could not load "
						+ ManagedObjectType.class.getSimpleName() + issueSuffix);
				continue; // can not load supplied managed object
			}

			// Determine if an input managed object
			boolean isInput = managedObjectLoader.isInputManagedObject(moType);

			// Wire in the configuration for the managed object
			if (object.wirer != null) {
				object.wirer.wire(object);
			}

			// Obtain the dependency types
			List<SuppliedManagedObjectDependencyType> dependencies = new LinkedList<SuppliedManagedObjectDependencyType>();
			for (ManagedObjectDependencyType<?> dependencyType : moType
					.getDependencyTypes()) {

				// Obtain the dependency name
				String dependencyName = dependencyType.getDependencyName();

				// Obtain the base supplied managed object dependency details
				String type = dependencyType.getDependencyType().getName();
				String qualifier = dependencyType.getTypeQualifier();

				// Determine if overridden details
				AutoWire overriddenAutoWire = object.dependencies
						.remove(dependencyName);
				if (overriddenAutoWire != null) {
					type = overriddenAutoWire.getType();
					qualifier = overriddenAutoWire.getQualifier();
				}

				// Add the supplied managed object dependency
				dependencies.add(new SuppliedManagedObjectDependencyTypeImpl(
						dependencyName, type, qualifier));
			}
			for (String dependencyName : object.dependencies.keySet()) {
				// Unknown mapped dependency (as should be removed above)
				this.addIssue("Wired dependency '" + dependencyName
						+ "' not specified on "
						+ ManagedObjectType.class.getSimpleName() + issueSuffix);
			}

			// Obtain the flow types
			List<SuppliedManagedObjectFlowType> flows = new LinkedList<SuppliedManagedObjectFlowType>();
			for (ManagedObjectFlowType<?> flowType : moType.getFlowTypes()) {

				// Obtain the flow name
				String flowName = flowType.getFlowName();

				// Obtain the details of the flow
				Class<?> argumentType = flowType.getArgumentType();

				// Obtain mapping for flow
				MappedFlow mappedFlow = object.flows.remove(flowName);
				if (mappedFlow == null) {
					this.addIssue("Flow '" + flowName + "' must be wired"
							+ issueSuffix);
					continue; // must have mapped flow
				}
				String sectionName = mappedFlow.sectionName;
				String sectionInputName = mappedFlow.sectionInputName;

				// Add the supplied managed object flow
				flows.add(new SuppliedManagedObjectFlowTypeImpl(flowName,
						sectionName, sectionInputName, argumentType));
			}
			for (String flowName : object.flows.keySet()) {
				// Unknown mapped flow (as should be removed above)
				this.addIssue("Wired flow '" + flowName + "' not specified on "
						+ ManagedObjectType.class.getSimpleName() + issueSuffix);
			}

			// Obtain the supplied teams and required team types
			List<SuppliedManagedObjectTeam> suppliedTeams = new LinkedList<SuppliedManagedObjectTeam>();
			List<SuppliedManagedObjectTeamType> requiredTeams = new LinkedList<SuppliedManagedObjectTeamType>();
			for (ManagedObjectTeamType teamType : moType.getTeamTypes()) {

				// Obtain the team name
				String teamName = teamType.getTeamName();

				// Obtain the team mapping
				AutoWire teamAutoWire = object.teams.remove(teamName);
				if (teamAutoWire == null) {
					// Not mapped, so should be supplied
					SuppliedAutoWireTeam suppliedTeam = object.suppliedTeams
							.remove(teamName);

					// Ensure have wiring for team
					if (suppliedTeam == null) {
						this.addIssue("Team '" + teamName + "' must be wired"
								+ issueSuffix);
						continue; // must have wiring to include
					}

					// Add the supplied team
					suppliedTeams.add(new SuppliedManagedObjectTeamImpl(
							teamName, suppliedTeam.getTeamSourceClassName(),
							suppliedTeam.getProperties()));

					// Not include as already supplied
					continue;
				}

				// Add the required team type
				requiredTeams.add(new SuppliedManagedObjectTeamTypeImpl(
						teamName, teamAutoWire));
			}
			Set<String> unknownTeamNames = new HashSet<String>();
			unknownTeamNames.addAll(object.teams.keySet());
			unknownTeamNames.addAll(object.suppliedTeams.keySet());
			for (String teamName : unknownTeamNames) {
				// Unknown mapped teams (as should be removed above)
				this.addIssue("Wired team '" + teamName + "' not specified on "
						+ ManagedObjectType.class.getSimpleName() + issueSuffix);
			}

			// Add the supplied managed object type
			SuppliedManagedObjectDependencyType[] dependencyTypes = dependencies
					.toArray(new SuppliedManagedObjectDependencyType[dependencies
							.size()]);
			SuppliedManagedObjectFlowType[] flowTypes = flows
					.toArray(new SuppliedManagedObjectFlowType[flows.size()]);
			SuppliedManagedObjectTeamType[] teamTypes = requiredTeams
					.toArray(new SuppliedManagedObjectTeamType[requiredTeams
							.size()]);
			Class<?>[] extensionInterfaces = moType.getExtensionInterfaces();
			managedObjects.add(new SuppliedManagedObjectTypeImpl(
					object.autoWiring, isInput, dependencyTypes, flowTypes,
					teamTypes, extensionInterfaces));

			// Create the supplied managed object
			SuppliedManagedObjectTeam[] teams = suppliedTeams
					.toArray(new SuppliedManagedObjectTeam[suppliedTeams.size()]);
			SuppliedManagedObject<?, ?> suppliedManagedObject = new SuppliedManagedObjectImpl(
					moType, object.managedObjectSource, object.properties,
					object.timeout, teams);

			// Fill supply orders
			NEXT_SUPPLY_ORDER: for (SupplyOrder supplyOrder : supplyOrders) {
				AutoWire supplyAutoWire = supplyOrder.getAutoWire();
				for (AutoWire objectAutoWire : object.getAutoWiring()) {
					if (supplyAutoWire.equals(objectAutoWire)) {

						// Matching auto-wire, so fill supply order
						supplyOrder.fillOrder(suppliedManagedObject);

						// Supply order filled, so move on to next supply order
						continue NEXT_SUPPLY_ORDER;
					}
				}
			}
		}

		// Return the supplier type
		return new SupplierTypeImpl(
				managedObjects
						.toArray(new SuppliedManagedObjectType[managedObjects
								.size()]));
	}

	/**
	 * Adds an issue.
	 * 
	 * @param issueDescription
	 *            Issue description.
	 */
	private void addIssue(String issueDescription) {
		this.context.getCompilerIssues().addIssue(LocationType.OFFICE_FLOOR,
				this.officeFloorLocation, null, this.supplierName,
				issueDescription);
	}

	/**
	 * Adds an issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 * @param cause
	 *            Cause of issue.
	 */
	private void addIssue(String issueDescription, Throwable cause) {
		this.context.getCompilerIssues().addIssue(LocationType.OFFICE_FLOOR,
				this.officeFloorLocation, null, this.supplierName,
				issueDescription, cause);
	}

	/*
	 * ====================== SupplierSourceContext =====================
	 */

	@Override
	public <D extends Enum<D>, F extends Enum<F>> AutoWireObject addManagedObject(
			ManagedObjectSource<D, F> managedObjectSource,
			ManagedObjectSourceWirer wirer, AutoWire... autoWiring) {

		// Create the auto-wire object
		PropertyList properties = this.context.createPropertyList();
		SuppliedAutoWireObject object = new SuppliedAutoWireObject(
				managedObjectSource, properties, wirer, autoWiring,
				this.context);

		// Add the auto-wire object
		this.suppliedObjects.add(object);

		// Return the auto-wire object
		return object;
	}

	/**
	 * Supplied {@link AutoWireObject}.
	 */
	private static class SuppliedAutoWireObject extends AutoWirePropertiesImpl
			implements AutoWireObject, ManagedObjectSourceWirerContext {

		/**
		 * {@link ManagedObjectSource}.
		 */
		private final ManagedObjectSource<?, ?> managedObjectSource;

		/**
		 * {@link PropertyList}.
		 */
		private final PropertyList properties;

		/**
		 * {@link ManagedObjectSourceWirer}.
		 */
		private final ManagedObjectSourceWirer wirer;

		/**
		 * {@link AutoWire} instances.
		 */
		private final AutoWire[] autoWiring;

		/**
		 * Timeout.
		 */
		private long timeout = 0;

		/**
		 * Mapped dependencies.
		 */
		private final Map<String, AutoWire> dependencies = new HashMap<String, AutoWire>();

		/**
		 * Mapped flows.
		 */
		private final Map<String, MappedFlow> flows = new HashMap<String, MappedFlow>();

		/**
		 * Mapped {@link Team} instances.
		 */
		private final Map<String, AutoWire> teams = new HashMap<String, AutoWire>();

		/**
		 * {@link SuppliedAutoWireTeam} instances.
		 */
		private final Map<String, SuppliedAutoWireTeam> suppliedTeams = new HashMap<String, SuppliedAutoWireTeam>();

		/**
		 * {@link NodeContext}.
		 */
		private final NodeContext context;

		/**
		 * Initiate.
		 * 
		 * @param managedObjectSource
		 *            {@link ManagedObjectSource}.
		 * @param properties
		 *            {@link PropertyList}.
		 * @param wirer
		 *            {@link ManagedObjectSourceWirer}.
		 * @param autoWiring
		 *            {@link AutoWire} instances.
		 * @param classLoader
		 *            {@link ClassLoader}.
		 */
		public SuppliedAutoWireObject(
				ManagedObjectSource<?, ?> managedObjectSource,
				PropertyList properties, ManagedObjectSourceWirer wirer,
				AutoWire[] autoWiring, NodeContext context) {
			super(context.getSourceContext().getClassLoader(), properties);
			this.managedObjectSource = managedObjectSource;
			this.properties = properties;
			this.wirer = wirer;
			this.autoWiring = autoWiring;
			this.context = context;
		}

		/*
		 * ============================ AutoWireObject =======================
		 */

		@Override
		public String getManagedObjectSourceClassName() {
			return (this.managedObjectSource == null ? null
					: this.managedObjectSource.getClass().getName());
		}

		@Override
		public ManagedObjectSourceWirer getManagedObjectSourceWirer() {
			return this.wirer;
		}

		@Override
		public AutoWire[] getAutoWiring() {
			return this.autoWiring;
		}

		@Override
		public long getTimeout() {
			return this.timeout;
		}

		@Override
		public void setTimeout(long timeout) {
			this.timeout = timeout;
		}

		/*
		 * ======================= ManagedObjectSourceWirer ====================
		 */

		@Override
		public void setManagedObjectScope(ManagedObjectScope managedobjectScope) {
			// Scope not necessary to determine managed object type
		}

		@Override
		public void mapDependency(String dependencyName, AutoWire autoWire) {
			this.dependencies.put(dependencyName, autoWire);
		}

		@Override
		public void mapFlow(String managedObjectSourceFlowName,
				String sectionName, String sectionInputName) {
			this.flows.put(managedObjectSourceFlowName, new MappedFlow(
					sectionName, sectionInputName));
		}

		@Override
		public <S extends TeamSource> AutoWireTeam mapTeam(
				String managedObjectSourceTeamName, String teamSourceClassName) {

			// Create the supplied auto-wire team
			PropertyList properties = this.context.createPropertyList();
			SuppliedAutoWireTeam suppliedTeam = new SuppliedAutoWireTeam(
					managedObjectSourceTeamName, teamSourceClassName,
					properties, this.context);

			// Register and return the supplied auto-wire team
			this.suppliedTeams.put(managedObjectSourceTeamName, suppliedTeam);
			return suppliedTeam;
		}

		@Override
		public void mapTeam(String managedObjectSourceTeamName,
				AutoWire autoWire) {
			this.teams.put(managedObjectSourceTeamName, autoWire);
		}
	}

	/**
	 * Supplied {@link AutoWireTeam}.
	 */
	private static class SuppliedAutoWireTeam extends AutoWirePropertiesImpl
			implements AutoWireTeam {

		/**
		 * {@link Team} name.
		 */
		private final String teamName;

		/**
		 * Name of the {@link TeamSource} {@link Class}.
		 */
		private final String teamSourceClassName;

		/**
		 * Initiate.
		 * 
		 * @param teamName
		 *            {@link Team} name.
		 * @param teamSourceClassName
		 *            Name of the {@link TeamSource} {@link Class}.
		 * @param properties
		 *            {@link PropertyList}.
		 * @param context
		 *            {@link NodeContext}.
		 */
		public SuppliedAutoWireTeam(String teamName,
				String teamSourceClassName, PropertyList properties,
				NodeContext context) {
			super(context.getSourceContext().getClassLoader(), properties);
			this.teamName = teamName;
			this.teamSourceClassName = teamSourceClassName;
		}

		/*
		 * ====================== AutoWireTeam =============================
		 */

		@Override
		public String getTeamName() {
			return this.teamName;
		}

		@Override
		public String getTeamSourceClassName() {
			return this.teamSourceClassName;
		}

		@Override
		public AutoWire[] getAutoWiring() {
			// Only auto-wiring for supplied managed object team
			return new AutoWire[0];
		}
	}

	/**
	 * Mapped flow.
	 */
	private static class MappedFlow {

		/**
		 * {@link OfficeSection} name.
		 */
		public final String sectionName;

		/**
		 * {@link OfficeSectionInput} name.
		 */
		public final String sectionInputName;

		/**
		 * Initiate.
		 * 
		 * @param sectionName
		 *            {@link OfficeSection} name.
		 * @param sectionInputName
		 *            {@link OfficeSectionInput} name.
		 */
		public MappedFlow(String sectionName, String sectionInputName) {
			this.sectionName = sectionName;
			this.sectionInputName = sectionInputName;
		}
	}

}