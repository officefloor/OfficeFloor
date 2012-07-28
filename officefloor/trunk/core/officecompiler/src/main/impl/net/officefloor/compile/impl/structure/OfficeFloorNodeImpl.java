/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.impl.util.StringExtractor;
import net.officefloor.compile.internal.structure.InputManagedObjectNode;
import net.officefloor.compile.internal.structure.ManagedObjectNode;
import net.officefloor.compile.internal.structure.ManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeFloorNode;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.internal.structure.SuppliedManagedObjectNode;
import net.officefloor.compile.internal.structure.SupplierNode;
import net.officefloor.compile.internal.structure.TeamNode;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
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
import net.officefloor.compile.spi.section.ManagedObjectDependency;
import net.officefloor.compile.spi.section.ManagedObjectFlow;
import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.profile.Profiler;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * {@link OfficeFloorNode} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorNodeImpl extends AbstractNode implements
		OfficeFloorNode {

	/**
	 * Location of the {@link OfficeFloor}.
	 */
	private final String officeFloorLocation;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

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
	 * @param officeFloorLocation
	 *            Location of the {@link OfficeFloor}.
	 * @param context
	 *            {@link NodeContext}.
	 * @param profilers
	 *            Mapping of {@link Profiler} by their {@link Office} name.
	 */
	public OfficeFloorNodeImpl(String officeFloorLocation, NodeContext context,
			Map<String, Profiler> profilers) {
		this.officeFloorLocation = officeFloorLocation;
		this.context = context;
		this.profilers = profilers;
	}

	/*
	 * =================== AbstractNode =====================================
	 */

	@Override
	protected void addIssue(String issueDescription) {
		this.context.getCompilerIssues().addIssue(LocationType.OFFICE_FLOOR,
				this.officeFloorLocation, null, null, issueDescription);
	}

	/*
	 * ===================== OfficeFloorDeployer =============================
	 */

	@Override
	public OfficeFloorManagedObjectSource addManagedObjectSource(
			String managedObjectSourceName, String managedObjectSourceClassName) {
		// Obtain and return the managed object source for the name
		ManagedObjectSourceNode mo = this.managedObjectSources
				.get(managedObjectSourceName);
		if (mo == null) {
			// Create the managed object source and have in office floor context
			mo = new ManagedObjectSourceNodeImpl(managedObjectSourceName,
					managedObjectSourceClassName, LocationType.OFFICE_FLOOR,
					this.officeFloorLocation, null, null, this.managedObjects,
					this.context);
			mo.addOfficeFloorContext(this.officeFloorLocation);

			// Add the managed object source
			this.managedObjectSources.put(managedObjectSourceName, mo);
		} else {
			// Managed object source already added
			this.addIssue("Office floor managed object source "
					+ managedObjectSourceName + " already added");
		}
		return mo;
	}

	@Override
	public OfficeFloorManagedObjectSource addManagedObjectSource(
			String managedObjectSourceName,
			ManagedObjectSource<?, ?> managedObjectSource) {
		// Obtain and return the managed object source for the name
		ManagedObjectSourceNode mo = this.managedObjectSources
				.get(managedObjectSourceName);
		if (mo == null) {
			// Create the managed object source and have in office floor context
			mo = new ManagedObjectSourceNodeImpl(managedObjectSourceName,
					managedObjectSource, LocationType.OFFICE_FLOOR,
					this.officeFloorLocation, null, null, this.managedObjects,
					this.context);
			mo.addOfficeFloorContext(this.officeFloorLocation);

			// Add the managed object source
			this.managedObjectSources.put(managedObjectSourceName, mo);
		} else {
			// Managed object source already added
			this.addIssue("Office floor managed object source "
					+ managedObjectSourceName + " already added");
		}
		return mo;
	}

	@Override
	public OfficeFloorInputManagedObject addInputManagedObject(
			String inputManagedObjectName) {
		// Obtain and return the input managed object for the name
		InputManagedObjectNode inputMo = this.inputManagedObjects
				.get(inputManagedObjectName);
		if (inputMo == null) {
			// Create the input managed object and have in office floor context
			inputMo = new InputManagedObjectNodeImpl(inputManagedObjectName,
					this.officeFloorLocation, this.context);

			// Add the input managed object
			this.inputManagedObjects.put(inputManagedObjectName, inputMo);
		} else {
			// Input managed object already added
			this.addIssue("Office floor input managed object "
					+ inputManagedObjectName + " already added");
		}
		return inputMo;
	}

	@Override
	public OfficeFloorSupplier addSupplier(String supplierName,
			String supplierSourceClassName) {
		// Obtain and return the supplier for the name
		SupplierNode supplier = this.suppliers.get(supplierName);
		if (supplier == null) {
			// Add the supplier
			supplier = new SupplierNodeImpl(supplierName,
					supplierSourceClassName, this, this.officeFloorLocation,
					this.context);
			this.suppliers.put(supplierName, supplier);
		} else {
			// Supplier already added
			this.addIssue("Office floor supplier " + supplierName
					+ " already added");
		}
		return supplier;
	}

	@Override
	public OfficeFloorTeam addTeam(String teamName, String teamSourceClassName) {
		// Obtain and return the team for the name
		TeamNode team = this.teams.get(teamName);
		if (team == null) {
			// Add the team
			team = new TeamNodeImpl(teamName, teamSourceClassName,
					this.officeFloorLocation, this.context);
			this.teams.put(teamName, team);
		} else {
			// Team already added
			this.addIssue("Office floor team " + teamName + " already added");
		}
		return team;
	}

	@Override
	public DeployedOffice addDeployedOffice(String officeName,
			OfficeSource officeSource, String officeLocation) {
		// Obtain and return the office for the name
		OfficeNode office = this.offices.get(officeName);
		if (office == null) {
			// Create the office within the office floor context
			office = new OfficeNodeImpl(officeName, officeSource,
					officeLocation, this.context);
			office.addOfficeFloorContext(this.officeFloorLocation);

			// Add the office
			this.offices.put(officeName, office);
		} else {
			// Office already added
			this.addIssue("Office " + officeName + " already deployed");
		}
		return office;
	}

	@Override
	public DeployedOffice addDeployedOffice(String officeName,
			String officeSourceClassName, String officeLocation) {
		// Obtain and return the office for the name
		OfficeNode office = this.offices.get(officeName);
		if (office == null) {
			// Create the office within the office floor context
			office = new OfficeNodeImpl(officeName, officeSourceClassName,
					officeLocation, this.context);
			office.addOfficeFloorContext(this.officeFloorLocation);

			// Add the office
			this.offices.put(officeName, office);
		} else {
			// Office already added
			this.addIssue("Office " + officeName + " already deployed");
		}
		return office;
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
	public void addIssue(String issueDescription, AssetType assetType,
			String assetName) {
		this.context.getCompilerIssues().addIssue(LocationType.OFFICE_FLOOR,
				this.officeFloorLocation, assetType, assetName,
				issueDescription);
	}

	@Override
	public void addIssue(String issueDescription, Throwable cause,
			AssetType assetType, String assetName) {
		this.context.getCompilerIssues().addIssue(LocationType.OFFICE_FLOOR,
				this.officeFloorLocation, assetType, assetName,
				issueDescription, cause);
	}

	/*
	 * ===================== OfficeFloorNode ==================================
	 */

	@Override
	public OfficeFloorManagedObjectSource addManagedObjectSource(
			String managedObjectSourceName,
			SuppliedManagedObjectNode suppliedManagedObject) {
		// Obtain and return the managed object source for the name
		ManagedObjectSourceNode mo = this.managedObjectSources
				.get(managedObjectSourceName);
		if (mo == null) {
			// Create the managed object source and have in office floor context
			mo = new ManagedObjectSourceNodeImpl(managedObjectSourceName,
					suppliedManagedObject, LocationType.OFFICE_FLOOR,
					this.officeFloorLocation, null, null, this.managedObjects,
					this.context);
			mo.addOfficeFloorContext(this.officeFloorLocation);

			// Add the managed object source
			this.managedObjectSources.put(managedObjectSourceName, mo);
		} else {
			// Managed object source already added
			this.addIssue("Office floor managed object source "
					+ managedObjectSourceName + " already added");
		}
		return mo;
	}

	@Override
	public OfficeFloor deployOfficeFloor(OfficeFrame officeFrame) {

		// Obtain the OfficeFloor builder
		OfficeFloorBuilder builder = officeFrame
				.createOfficeFloorBuilder(this.officeFloorLocation);

		// Initiate the OfficeFloor builder with compiler details
		this.context.initiateOfficeFloorBuilder(builder);

		// Load the offices (in deterministic order)
		OfficeNode[] offices = CompileUtil.toSortedArray(this.offices.values(),
				new OfficeNode[0], new StringExtractor<OfficeNode>() {
					@Override
					public String toString(OfficeNode object) {
						return object.getDeployedOfficeName();
					}
				});
		for (OfficeNode office : offices) {
			office.loadOffice();
		}

		// Load the managed object sources (in deterministic order)
		ManagedObjectSourceNode[] managedObjectSources = CompileUtil
				.toSortedArray(this.managedObjectSources.values(),
						new ManagedObjectSourceNode[0],
						new StringExtractor<ManagedObjectSourceNode>() {
							@Override
							public String toString(
									ManagedObjectSourceNode object) {
								return object
										.getOfficeFloorManagedObjectSourceName();
							}
						});
		for (ManagedObjectSourceNode managedObjectSource : managedObjectSources) {
			managedObjectSource.loadManagedObjectType();
		}

		// Build the teams (in deterministic order)
		TeamNode[] teams = CompileUtil.toSortedArray(this.teams.values(),
				new TeamNode[0], new StringExtractor<TeamNode>() {
					@Override
					public String toString(TeamNode object) {
						return object.getOfficeFloorTeamName();
					}
				});
		for (TeamNode team : teams) {
			team.buildTeam(builder);
		}

		// Build the offices (in deterministic order)
		Map<OfficeNode, OfficeBuilder> officeBuilders = new HashMap<OfficeNode, OfficeBuilder>();
		for (OfficeNode office : offices) {
			// Build the office
			OfficeBuilder officeBuilder = office.buildOffice(builder);

			// Provide profiler to office
			String officeName = office.getDeployedOfficeName();
			Profiler profiler = this.profilers.get(officeName);
			if (profiler != null) {
				officeBuilder.setProfiler(profiler);
			}

			// Keep track of the office builders
			officeBuilders.put(office, officeBuilder);
		}

		// Build the input managed objects (in deterministic order)
		InputManagedObjectNode[] inputMos = CompileUtil.toSortedArray(
				this.inputManagedObjects.values(),
				new InputManagedObjectNode[0],
				new StringExtractor<InputManagedObjectNode>() {
					@Override
					public String toString(InputManagedObjectNode object) {
						return object.getOfficeFloorInputManagedObjectName();
					}
				});

		// Build the managed object sources (in deterministic order)
		for (ManagedObjectSourceNode managedObjectSource : managedObjectSources) {

			// Obtain the managing office for the managed object source
			OfficeNode managingOffice = managedObjectSource
					.getManagingOfficeNode();
			OfficeBuilder officeBuilder = officeBuilders.get(managingOffice);
			if (officeBuilder == null) {
				continue; // must have managing office
			}

			// Build the managed object source
			managedObjectSource.buildManagedObject(builder, managingOffice,
					officeBuilder);

			// Bind the input managed objects (for this managed object source)
			for (InputManagedObjectNode inputMo : inputMos) {
				if (managedObjectSource == inputMo
						.getBoundManagedObjectSourceNode()) {
					// Bind managed object source for the input managed object
					inputMo.buildOfficeManagedObject(managingOffice,
							officeBuilder);
				}
			}
		}

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
					LocationType.OFFICE_FLOOR,
					OfficeFloorNodeImpl.this.officeFloorLocation, assetType,
					assetName, issueDescription);
		}

		@Override
		public void addIssue(AssetType assetType, String assetName,
				String issueDescription, Throwable cause) {
			OfficeFloorNodeImpl.this.context.getCompilerIssues().addIssue(
					LocationType.OFFICE_FLOOR,
					OfficeFloorNodeImpl.this.officeFloorLocation, assetType,
					assetName, issueDescription, cause);
		}
	}

}