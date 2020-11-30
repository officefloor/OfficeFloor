/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.model.impl.officefloor;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.impl.util.TripleKeyMap;
import net.officefloor.compile.spi.office.OfficeObject;
import net.officefloor.compile.spi.office.OfficeTeam;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorExecutionStrategy;
import net.officefloor.compile.spi.officefloor.OfficeFloorExecutive;
import net.officefloor.compile.spi.officefloor.OfficeFloorInputManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectDependency;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectExecutionStrategy;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectFlow;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectFunctionDependency;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectPool;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectTeam;
import net.officefloor.compile.spi.officefloor.OfficeFloorSupplier;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.compile.spi.officefloor.source.RequiredProperties;
import net.officefloor.compile.spi.officefloor.source.impl.AbstractOfficeFloorSource;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.officefloor.DeployedOfficeInputModel;
import net.officefloor.model.officefloor.DeployedOfficeModel;
import net.officefloor.model.officefloor.DeployedOfficeObjectModel;
import net.officefloor.model.officefloor.DeployedOfficeObjectToOfficeFloorInputManagedObjectModel;
import net.officefloor.model.officefloor.DeployedOfficeObjectToOfficeFloorManagedObjectModel;
import net.officefloor.model.officefloor.DeployedOfficeTeamModel;
import net.officefloor.model.officefloor.DeployedOfficeTeamToOfficeFloorTeamModel;
import net.officefloor.model.officefloor.OfficeFloorChanges;
import net.officefloor.model.officefloor.OfficeFloorExecutionStrategyModel;
import net.officefloor.model.officefloor.OfficeFloorExecutiveModel;
import net.officefloor.model.officefloor.OfficeFloorInputManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorInputManagedObjectToBoundOfficeFloorManagedObjectSourceModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectDependencyModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectDependencyToOfficeFloorInputManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectDependencyToOfficeFloorManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectPoolModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceExecutionStrategyModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceExecutionStrategyToOfficeFloorExecutionStrategyModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceFlowModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceFunctionDependencyModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceFunctionDependencyToOfficeFloorManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceInputDependencyModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceInputDependencyToOfficeFloorManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceStartAfterOfficeFloorManagedObjectSourceModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceStartBeforeOfficeFloorManagedObjectSourceModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceTeamModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceToDeployedOfficeModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceToOfficeFloorInputManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceToOfficeFloorManagedObjectPoolModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceToOfficeFloorSupplierModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectToOfficeFloorManagedObjectSourceModel;
import net.officefloor.model.officefloor.OfficeFloorModel;
import net.officefloor.model.officefloor.OfficeFloorSupplierModel;
import net.officefloor.model.officefloor.OfficeFloorTeamModel;
import net.officefloor.model.officefloor.PropertyModel;
import net.officefloor.model.officefloor.TypeQualificationModel;

/**
 * {@link OfficeFloorModel} {@link OfficeFloorSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorModelOfficeFloorSource extends AbstractOfficeFloorSource {

	/*
	 * =================== AbstractOfficeFloorSource ==========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	public void specifyConfigurationProperties(RequiredProperties requiredProperties, OfficeFloorSourceContext context)
			throws Exception {
	}

	@Override
	public void sourceOfficeFloor(OfficeFloorDeployer deployer, OfficeFloorSourceContext context) throws Exception {

		// Retrieve the OfficeFloor model
		ConfigurationItem configuration = context.getConfigurationItem(context.getOfficeFloorLocation(), null);
		OfficeFloorModel officeFloor = new OfficeFloorModel();
		new OfficeFloorRepositoryImpl(new ModelRepositoryImpl()).retrieveOfficeFloor(officeFloor, configuration);

		// Determine if auto-wire
		if (officeFloor.getIsAutoWireObjects()) {
			deployer.enableAutoWireObjects();
		}
		if (officeFloor.getIsAutoWireTeams()) {
			deployer.enableAutoWireTeams();
		}

		// Add the OfficeFloor suppliers, keeping registry of them
		Map<String, OfficeFloorSupplier> suppliers = new HashMap<String, OfficeFloorSupplier>();
		for (OfficeFloorSupplierModel supplierModel : officeFloor.getOfficeFloorSuppliers()) {

			// Add the OfficeFloor supplier
			String supplierName = supplierModel.getOfficeFloorSupplierName();
			OfficeFloorSupplier supplier = deployer.addSupplier(supplierName,
					supplierModel.getSupplierSourceClassName());
			for (PropertyModel property : supplierModel.getProperties()) {
				supplier.addProperty(property.getName(), property.getValue());
			}

			// Register the supplier
			suppliers.put(supplierName, supplier);
		}

		// Add the OfficeFloor managed object pools, keeping registry of them
		Map<String, OfficeFloorManagedObjectPool> managedObjectPools = new HashMap<>();
		for (OfficeFloorManagedObjectPoolModel poolModel : officeFloor.getOfficeFloorManagedObjectPools()) {

			// Add the managed object pool
			String managedObjectPoolName = poolModel.getOfficeFloorManagedObjectPoolName();
			String managedObjectPoolSourceClassName = poolModel.getManagedObjectPoolSourceClassName();
			OfficeFloorManagedObjectPool pool = deployer.addManagedObjectPool(managedObjectPoolName,
					managedObjectPoolSourceClassName);
			managedObjectPools.put(managedObjectPoolName, pool);

			// Add properties for the managed object source
			for (PropertyModel property : poolModel.getProperties()) {
				pool.addProperty(property.getName(), property.getValue());
			}
		}

		// Add the OfficeFloor managed object sources, keeping registry of them
		Map<String, OfficeFloorManagedObjectSource> managedObjectSources = new HashMap<String, OfficeFloorManagedObjectSource>();
		for (OfficeFloorManagedObjectSourceModel managedObjectSourceModel : officeFloor
				.getOfficeFloorManagedObjectSources()) {

			// Add the OfficeFloor managed object source
			String managedObjectSourceName = managedObjectSourceModel.getOfficeFloorManagedObjectSourceName();

			// Determine if supplied managed object source
			OfficeFloorManagedObjectSource managedObjectSource;
			OfficeFloorManagedObjectSourceToOfficeFloorSupplierModel mosToSupplier = managedObjectSourceModel
					.getOfficeFloorSupplier();
			if (mosToSupplier != null) {
				// Supplied managed object source, so obtain its supplier
				String supplierName = mosToSupplier.getOfficeFloorSupplierName();
				OfficeFloorSupplier supplier = suppliers.get(supplierName);
				if (supplier == null) {
					// Must have supplier
					deployer.addIssue(
							"No supplier '" + supplierName + "' for managed object source " + managedObjectSourceName);
					continue; // must have supplier to add managed object source
				}

				// Supply the managed object source
				String qualifier = mosToSupplier.getQualifier();
				qualifier = (CompileUtil.isBlank(qualifier) ? null : qualifier);
				String type = mosToSupplier.getType();
				managedObjectSource = supplier.getOfficeFloorManagedObjectSource(managedObjectSourceName, qualifier,
						type);

			} else {
				// Source the managed object source
				managedObjectSource = deployer.addManagedObjectSource(managedObjectSourceName,
						managedObjectSourceModel.getManagedObjectSourceClassName());
			}

			// Add properties for the managed object source
			for (PropertyModel property : managedObjectSourceModel.getProperties()) {
				managedObjectSource.addProperty(property.getName(), property.getValue());
			}

			// Provide timeout
			String timeoutValue = managedObjectSourceModel.getTimeout();
			if (!CompileUtil.isBlank(timeoutValue)) {
				try {
					managedObjectSource.setTimeout(Long.valueOf(timeoutValue));
				} catch (NumberFormatException ex) {
					deployer.addIssue("Invalid timeout value: " + timeoutValue + " for managed object source "
							+ managedObjectSourceName);
				}
			}

			// Register the managed object source
			managedObjectSources.put(managedObjectSourceName, managedObjectSource);

			// Determine if pool the managed object
			OfficeFloorManagedObjectSourceToOfficeFloorManagedObjectPoolModel mosToPool = managedObjectSourceModel
					.getOfficeFloorManagedObjectPool();
			if (mosToPool != null) {
				OfficeFloorManagedObjectPoolModel poolModel = mosToPool.getOfficeFloorManagedObjectPool();
				if (poolModel != null) {
					OfficeFloorManagedObjectPool pool = managedObjectPools
							.get(poolModel.getOfficeFloorManagedObjectPoolName());
					deployer.link(managedObjectSource, pool);
				}
			}
		}

		// Add the OfficeFloor managed objects, keeping registry of them
		Map<String, OfficeFloorManagedObject> managedObjects = new HashMap<String, OfficeFloorManagedObject>();
		for (OfficeFloorManagedObjectModel managedObjectModel : officeFloor.getOfficeFloorManagedObjects()) {

			// Obtain the managed object details
			String managedObjectName = managedObjectModel.getOfficeFloorManagedObjectName();
			ManagedObjectScope managedObjectScope = this
					.getManagedObjectScope(managedObjectModel.getManagedObjectScope(), deployer, managedObjectName);

			// Obtain the managed object source for the managed object
			OfficeFloorManagedObjectSource moSource = null;
			OfficeFloorManagedObjectToOfficeFloorManagedObjectSourceModel moToSource = managedObjectModel
					.getOfficeFloorManagedObjectSource();
			if (moToSource != null) {
				OfficeFloorManagedObjectSourceModel moSourceModel = moToSource.getOfficeFloorManagedObjectSource();
				if (moSourceModel != null) {
					moSource = managedObjectSources.get(moSourceModel.getOfficeFloorManagedObjectSourceName());
				}
			}
			if (moSource == null) {
				continue; // must have managed object source
			}

			// Add the managed object and also register it
			OfficeFloorManagedObject managedObject = moSource.addOfficeFloorManagedObject(managedObjectName,
					managedObjectScope);
			managedObjects.put(managedObjectName, managedObject);

			// Load type qualifications
			for (TypeQualificationModel qualification : managedObjectModel.getTypeQualifications()) {
				managedObject.addTypeQualification(qualification.getQualifier(), qualification.getType());
			}
		}

		// Add the OfficeFloor input managed objects, keeping registry of them
		Map<String, OfficeFloorInputManagedObject> inputManagedObjects = new HashMap<String, OfficeFloorInputManagedObject>();
		for (OfficeFloorInputManagedObjectModel inputManagedObjectModel : officeFloor
				.getOfficeFloorInputManagedObjects()) {

			// Add the input managed object and also register it
			String inputManagedObjectName = inputManagedObjectModel.getOfficeFloorInputManagedObjectName();
			String inputObjectType = inputManagedObjectModel.getObjectType();
			OfficeFloorInputManagedObject inputManagedObject = deployer.addInputManagedObject(inputManagedObjectName,
					inputObjectType);
			inputManagedObjects.put(inputManagedObjectName, inputManagedObject);

			// Load type qualifications
			for (TypeQualificationModel qualification : inputManagedObjectModel.getTypeQualifications()) {
				inputManagedObject.addTypeQualification(qualification.getQualifier(), qualification.getType());
			}

			// Provide the binding to managed object source (if available)
			OfficeFloorManagedObjectSource boundManagedObjectSource = null;
			OfficeFloorInputManagedObjectToBoundOfficeFloorManagedObjectSourceModel conn = inputManagedObjectModel
					.getBoundOfficeFloorManagedObjectSource();
			if (conn != null) {
				OfficeFloorManagedObjectSourceModel boundMoSourceModel = conn.getBoundOfficeFloorManagedObjectSource();
				if (boundMoSourceModel != null) {
					boundManagedObjectSource = managedObjectSources
							.get(boundMoSourceModel.getOfficeFloorManagedObjectSourceName());
				}
			}
			if (boundManagedObjectSource != null) {
				// Have bound managed object source so bind it
				inputManagedObject.setBoundOfficeFloorManagedObjectSource(boundManagedObjectSource);
			}

			// Link dependencies for the input managed object
			for (OfficeFloorManagedObjectSourceToOfficeFloorInputManagedObjectModel mosToInputMoModel : inputManagedObjectModel
					.getOfficeFloorManagedObjectSources()) {

				// Obtain the inputting managed object source
				OfficeFloorManagedObjectSource inputMos = null;
				OfficeFloorManagedObjectSourceModel inputMosModel = mosToInputMoModel
						.getOfficeFloorManagedObjectSource();
				if (inputMosModel != null) {
					inputMos = managedObjectSources.get(inputMosModel.getOfficeFloorManagedObjectSourceName());
				}
				if (inputMos != null) {
					// Have managed object source so link input dependencies
					for (OfficeFloorManagedObjectSourceInputDependencyModel inputDependencyModel : inputMosModel
							.getOfficeFloorManagedObjectSourceInputDependencies()) {

						// Add the dependency
						String dependencyName = inputDependencyModel
								.getOfficeFloorManagedObjectSourceInputDependencyName();
						OfficeFloorManagedObjectDependency inputDependency = inputMos
								.getInputOfficeFloorManagedObjectDependency(dependencyName);

						// Obtain the dependent managed object
						OfficeFloorManagedObject dependentManagedObject = null;
						OfficeFloorManagedObjectSourceInputDependencyToOfficeFloorManagedObjectModel dependencyToMo = inputDependencyModel
								.getOfficeFloorManagedObject();
						if (dependencyToMo != null) {
							OfficeFloorManagedObjectModel dependentMoModel = dependencyToMo
									.getOfficeFloorManagedObject();
							if (dependentMoModel != null) {
								dependentManagedObject = managedObjects
										.get(dependentMoModel.getOfficeFloorManagedObjectName());
							}
						}
						if (dependentManagedObject == null) {
							continue; // must have dependent managed object
						}

						// Link the input dependency to managed object
						deployer.link(inputDependency, dependentManagedObject);
					}
				}
			}
		}

		// Link the start orderings for managed object sources
		for (OfficeFloorManagedObjectSourceModel mosModel : officeFloor.getOfficeFloorManagedObjectSources()) {

			// Obtain the managed object source
			OfficeFloorManagedObjectSource mos = managedObjectSources
					.get(mosModel.getOfficeFloorManagedObjectSourceName());

			// Link the direct start befores
			for (OfficeFloorManagedObjectSourceStartBeforeOfficeFloorManagedObjectSourceModel startLaterModel : mosModel
					.getStartBeforeEarliers()) {

				// Obtain the start before
				String startLaterName = startLaterModel.getOfficeFloorManagedObjectSourceName();
				if (CompileUtil.isBlank(startLaterName)) {

					// Link start before by type
					String startLaterType = startLaterModel.getManagedObjectType();
					deployer.startBefore(mos, startLaterType);

				} else {
					// Use direct name of managed object source
					OfficeFloorManagedObjectSource startLater = managedObjectSources.get(startLaterName);
					if (startLater != null) {
						// Link start before directly
						deployer.startBefore(mos, startLater);
					}
				}
			}

			// Link the direct start afters
			for (OfficeFloorManagedObjectSourceStartAfterOfficeFloorManagedObjectSourceModel startEarlierModel : mosModel
					.getStartAfterLaters()) {

				// Obtain the start after
				String startEarlierName = startEarlierModel.getOfficeFloorManagedObjectSourceName();
				if (CompileUtil.isBlank(startEarlierName)) {

					// Link start after by type
					String startEarlierType = startEarlierModel.getManagedObjectType();
					deployer.startAfter(mos, startEarlierType);

				} else {
					// Use direct name of managed object source
					OfficeFloorManagedObjectSource startEarlier = managedObjectSources.get(startEarlierName);
					if (startEarlier != null) {
						// Link start after directly
						deployer.startAfter(mos, startEarlier);
					}
				}
			}
		}

		// Link the function dependencies for the managed object sources
		for (OfficeFloorManagedObjectSourceModel mosModel : officeFloor.getOfficeFloorManagedObjectSources()) {

			// Obtain the managed object source
			OfficeFloorManagedObjectSource mos = managedObjectSources
					.get(mosModel.getOfficeFloorManagedObjectSourceName());

			// Link each function dependency for the managed object source
			for (OfficeFloorManagedObjectSourceFunctionDependencyModel dependencyModel : mosModel
					.getOfficeFloorManagedObjectSourceFunctionDependencies()) {

				// Add the dependency
				String dependencyName = dependencyModel.getOfficeFloorManagedObjectSourceFunctionDependencyName();
				OfficeFloorManagedObjectFunctionDependency dependency = mos
						.getOfficeFloorManagedObjectFunctionDependency(dependencyName);

				// Link the dependent managed object
				OfficeFloorManagedObject dependentManagedObject = null;
				OfficeFloorManagedObjectSourceFunctionDependencyToOfficeFloorManagedObjectModel dependencyToMo = dependencyModel
						.getOfficeFloorManagedObject();
				if (dependencyToMo != null) {
					OfficeFloorManagedObjectModel dependentMoModel = dependencyToMo.getOfficeFloorManagedObject();
					if (dependentMoModel != null) {
						dependentManagedObject = managedObjects.get(dependentMoModel.getOfficeFloorManagedObjectName());
					}
				}
				if (dependentManagedObject != null) {
					// Link the function dependency to the managed object
					deployer.link(dependency, dependentManagedObject);
				}
			}
		}

		// Link the dependencies for the managed objects
		for (OfficeFloorManagedObjectModel managedObjectModel : officeFloor.getOfficeFloorManagedObjects()) {

			// Obtain the managed object
			OfficeFloorManagedObject managedObject = managedObjects
					.get(managedObjectModel.getOfficeFloorManagedObjectName());

			// Link each dependency for the managed object
			for (OfficeFloorManagedObjectDependencyModel dependencyModel : managedObjectModel
					.getOfficeFloorManagedObjectDependencies()) {

				// Add the dependency
				String dependencyName = dependencyModel.getOfficeFloorManagedObjectDependencyName();
				OfficeFloorManagedObjectDependency dependency = managedObject
						.getOfficeFloorManagedObjectDependency(dependencyName);

				// Link the dependent managed object
				OfficeFloorManagedObject dependentManagedObject = null;
				OfficeFloorManagedObjectDependencyToOfficeFloorManagedObjectModel dependencyToMo = dependencyModel
						.getOfficeFloorManagedObject();
				if (dependencyToMo != null) {
					OfficeFloorManagedObjectModel dependentMoModel = dependencyToMo.getOfficeFloorManagedObject();
					if (dependentMoModel != null) {
						dependentManagedObject = managedObjects.get(dependentMoModel.getOfficeFloorManagedObjectName());
					}
				}
				if (dependentManagedObject != null) {
					// Link the dependency to the managed object
					deployer.link(dependency, dependentManagedObject);
				}

				// Link the dependent input managed object
				OfficeFloorInputManagedObject inputManagedObject = null;
				OfficeFloorManagedObjectDependencyToOfficeFloorInputManagedObjectModel dependencyToInput = dependencyModel
						.getOfficeFloorInputManagedObject();
				if (dependencyToInput != null) {
					OfficeFloorInputManagedObjectModel inputMoModel = dependencyToInput
							.getOfficeFloorInputManagedObject();
					if (inputMoModel != null) {
						inputManagedObject = inputManagedObjects
								.get(inputMoModel.getOfficeFloorInputManagedObjectName());
					}
				}
				if (inputManagedObject != null) {
					// Link the dependency to the input managed object
					deployer.link(dependency, inputManagedObject);
				}
			}
		}

		// Set the OfficeFloor executive
		OfficeFloorExecutiveModel executiveModel = officeFloor.getOfficeFloorExecutive();
		OfficeFloorExecutive executive = null;
		Map<String, OfficeFloorExecutionStrategy> executionStrategies = new HashMap<>();
		if (executiveModel != null) {

			// Specify the executive
			executive = deployer.setExecutive(executiveModel.getExecutiveSourceClassName());

			// Add properties for the executive
			for (PropertyModel property : executiveModel.getProperties()) {
				executive.addProperty(property.getName(), property.getValue());
			}

			// Load the execution strategies
			for (OfficeFloorExecutionStrategyModel strategyModel : executiveModel.getExecutionStrategies()) {

				// Add the OfficeFloor execution strategy
				String strategyName = strategyModel.getExecutionStrategyName();
				OfficeFloorExecutionStrategy strategy = executive.getOfficeFloorExecutionStrategy(strategyName);

				// Register the execution strategy
				executionStrategies.put(strategyName, strategy);
			}
		}

		// Add the OfficeFloor teams, keeping registry of teams
		Map<String, OfficeFloorTeam> teams = new HashMap<String, OfficeFloorTeam>();
		for (OfficeFloorTeamModel teamModel : officeFloor.getOfficeFloorTeams()) {

			// Add the OfficeFloor team
			String teamName = teamModel.getOfficeFloorTeamName();
			OfficeFloorTeam team = deployer.addTeam(teamName, teamModel.getTeamSourceClassName());
			int teamSize = teamModel.getTeamSize();
			if (teamSize != 0) {
				team.setTeamSize(teamSize);
			}
			for (PropertyModel property : teamModel.getProperties()) {
				team.addProperty(property.getName(), property.getValue());
			}

			// Load the type qualifications
			for (TypeQualificationModel qualification : teamModel.getTypeQualifications()) {
				team.addTypeQualification(qualification.getQualifier(), qualification.getType());
			}

			// Register the team
			teams.put(teamName, team);

			// Flag request for no team oversight
			if (teamModel.getRequestNoTeamOversight()) {
				team.requestNoTeamOversight();
			}
		}

		// Add the offices, keeping registry of the offices and their inputs
		Map<String, DeployedOffice> offices = new HashMap<String, DeployedOffice>();
		TripleKeyMap<String, String, String, DeployedOfficeInput> officeInputs = new TripleKeyMap<String, String, String, DeployedOfficeInput>();
		for (DeployedOfficeModel officeModel : officeFloor.getDeployedOffices()) {

			// Add the office, registering them
			String officeName = officeModel.getDeployedOfficeName();
			DeployedOffice office = deployer.addDeployedOffice(officeName, officeModel.getOfficeSourceClassName(),
					officeModel.getOfficeLocation());
			offices.put(officeName, office);
			for (PropertyModel property : officeModel.getProperties()) {
				office.addProperty(property.getName(), property.getValue());
			}

			// Add the office inputs, registering them
			for (DeployedOfficeInputModel inputModel : officeModel.getDeployedOfficeInputs()) {
				String sectionName = inputModel.getSectionName();
				String sectionInputName = inputModel.getSectionInputName();
				DeployedOfficeInput officeInput = office.getDeployedOfficeInput(sectionName, sectionInputName);
				officeInputs.put(officeName, sectionName, sectionInputName, officeInput);
			}

			// Add the office objects
			for (DeployedOfficeObjectModel objectModel : officeModel.getDeployedOfficeObjects()) {

				// Add the office object
				OfficeObject officeObject = office.getDeployedOfficeObject(objectModel.getDeployedOfficeObjectName());

				// Link the OfficeFloor managed object
				OfficeFloorManagedObject managedObject = null;
				DeployedOfficeObjectToOfficeFloorManagedObjectModel connToMo = objectModel
						.getOfficeFloorManagedObject();
				if (connToMo != null) {
					OfficeFloorManagedObjectModel managedObjectModel = connToMo.getOfficeFloorManagedObject();
					if (managedObjectModel != null) {
						managedObject = managedObjects.get(managedObjectModel.getOfficeFloorManagedObjectName());
					}
				}
				if (managedObject != null) {
					// Have the office object be the managed object
					deployer.link(officeObject, managedObject);
				}

				// Link the OfficeFloor input managed object
				OfficeFloorInputManagedObject inputManagedObject = null;
				DeployedOfficeObjectToOfficeFloorInputManagedObjectModel connToInputMo = objectModel
						.getOfficeFloorInputManagedObject();
				if (connToInputMo != null) {
					OfficeFloorInputManagedObjectModel inputMoModel = connToInputMo.getOfficeFloorInputManagedObject();
					if (inputMoModel != null) {
						inputManagedObject = inputManagedObjects
								.get(inputMoModel.getOfficeFloorInputManagedObjectName());
					}
				}
				if (inputManagedObject != null) {
					// Have the office object by the input managed object
					deployer.link(officeObject, inputManagedObject);
				}
			}

			// Add the office teams
			for (DeployedOfficeTeamModel teamModel : officeModel.getDeployedOfficeTeams()) {

				// Add the office team
				OfficeTeam officeTeam = office.getDeployedOfficeTeam(teamModel.getDeployedOfficeTeamName());

				// Obtain the OfficeFloor team
				OfficeFloorTeam officeFloorTeam = null;
				DeployedOfficeTeamToOfficeFloorTeamModel conn = teamModel.getOfficeFloorTeam();
				if (conn != null) {
					OfficeFloorTeamModel officeFloorTeamModel = conn.getOfficeFloorTeam();
					if (officeFloorTeamModel != null) {
						officeFloorTeam = teams.get(officeFloorTeamModel.getOfficeFloorTeamName());
					}
				}
				if (officeFloorTeam == null) {
					continue; // must have undertaking OfficeFloor team
				}

				// Have the office team be the OfficeFloor team
				deployer.link(officeTeam, officeFloorTeam);
			}
		}

		// Link details for the managed object sources
		for (OfficeFloorManagedObjectSourceModel managedObjectSourceModel : officeFloor
				.getOfficeFloorManagedObjectSources()) {

			// Obtain the managed object source
			OfficeFloorManagedObjectSource managedObjectSource = managedObjectSources
					.get(managedObjectSourceModel.getOfficeFloorManagedObjectSourceName());
			if (managedObjectSource == null) {
				continue; // must have managed object source
			}

			// Obtain the managing office
			DeployedOffice managingOffice = null;
			OfficeFloorManagedObjectSourceToDeployedOfficeModel moToOffice = managedObjectSourceModel
					.getManagingOffice();
			if (moToOffice != null) {
				DeployedOfficeModel officeModel = moToOffice.getManagingOffice();
				if (officeModel != null) {
					managingOffice = offices.get(officeModel.getDeployedOfficeName());
				}
			}
			if (managingOffice != null) {
				// Have the office manage the managed object
				deployer.link(managedObjectSource.getManagingOffice(), managingOffice);
			}

			// Obtain the input managed object
			OfficeFloorInputManagedObject inputManagedObject = null;
			OfficeFloorManagedObjectSourceToOfficeFloorInputManagedObjectModel mosToInput = managedObjectSourceModel
					.getOfficeFloorInputManagedObject();
			if (mosToInput != null) {
				OfficeFloorInputManagedObjectModel inputMo = mosToInput.getOfficeFloorInputManagedObject();
				if (inputMo != null) {
					String inputManagedObjectName = inputMo.getOfficeFloorInputManagedObjectName();
					if (!CompileUtil.isBlank(inputManagedObjectName)) {
						inputManagedObject = inputManagedObjects.get(inputManagedObjectName);
					}
				}
			}
			if (inputManagedObject != null) {
				// Have input managed object for managed object source
				deployer.link(managedObjectSource, inputManagedObject);
			}

			// Add the OfficeFloor managed object source flows
			for (OfficeFloorManagedObjectSourceFlowModel flowModel : managedObjectSourceModel
					.getOfficeFloorManagedObjectSourceFlows()) {

				// Add the OfficeFloor managed object source flow
				String flowName = flowModel.getOfficeFloorManagedObjectSourceFlowName();
				OfficeFloorManagedObjectFlow flow = managedObjectSource.getOfficeFloorManagedObjectFlow(flowName);

				// Obtain the office input
				DeployedOfficeInput officeInput = null;
				OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel flowToInput = flowModel
						.getDeployedOfficeInput();
				if (flowToInput != null) {
					DeployedOfficeInputModel officeInputModel = flowToInput.getDeployedOfficeInput();
					if (officeInputModel != null) {
						DeployedOfficeModel officeModel = this.getOfficeForInput(officeInputModel, officeFloor);
						officeInput = officeInputs.get(officeModel.getDeployedOfficeName(),
								officeInputModel.getSectionName(), officeInputModel.getSectionInputName());
					}
				}
				if (officeInput != null) {
					// Have the office input for the flow
					deployer.link(flow, officeInput);
				}
			}

			// Add the OfficeFloor managed object source teams
			for (OfficeFloorManagedObjectSourceTeamModel mosTeamModel : managedObjectSourceModel
					.getOfficeFloorManagedObjectSourceTeams()) {

				// Add the OfficeFloor managed object source team
				String mosTeamName = mosTeamModel.getOfficeFloorManagedObjectSourceTeamName();
				OfficeFloorManagedObjectTeam mosTeam = managedObjectSource.getOfficeFloorManagedObjectTeam(mosTeamName);

				// Obtain the OfficeFloor team
				OfficeFloorTeam team = null;
				OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel mosTeamToTeam = mosTeamModel
						.getOfficeFloorTeam();
				if (mosTeamToTeam != null) {
					OfficeFloorTeamModel teamModel = mosTeamToTeam.getOfficeFloorTeam();
					if (teamModel != null) {
						team = teams.get(teamModel.getOfficeFloorTeamName());
					}
				}
				if (team != null) {
					// Have the team for the managed object source team
					deployer.link(mosTeam, team);
				}
			}

			// Add the OfficeFloor managed object source execution strategies
			for (OfficeFloorManagedObjectSourceExecutionStrategyModel mosExecutionStrategyModel : managedObjectSourceModel
					.getOfficeFloorManagedObjectSourceExecutionStrategies()) {

				// Add the OfficeFloor managed object source execution strategy
				String mosExecutionStrategyName = mosExecutionStrategyModel
						.getOfficeFloorManagedObjectSourceExecutionStrategyName();
				OfficeFloorManagedObjectExecutionStrategy mosExecutionStrategy = managedObjectSource
						.getOfficeFloorManagedObjectExecutionStrategy(mosExecutionStrategyName);

				// Obtain the OfficeFloor execution strategy
				OfficeFloorExecutionStrategy executionStrategy = null;
				OfficeFloorManagedObjectSourceExecutionStrategyToOfficeFloorExecutionStrategyModel mosExecutionStrategyToExecutionStrategy = mosExecutionStrategyModel
						.getOfficeFloorExecutionStrategy();
				if (mosExecutionStrategyToExecutionStrategy != null) {
					OfficeFloorExecutionStrategyModel executionStrategyModel = mosExecutionStrategyToExecutionStrategy
							.getOfficeFloorExecutionStrategy();
					if (executionStrategyModel != null) {
						executionStrategy = executionStrategies.get(executionStrategyModel.getExecutionStrategyName());
					}
				}
				if (executionStrategy != null) {
					// Have the execution strategy for the managed object source execution strategy
					deployer.link(mosExecutionStrategy, executionStrategy);
				}
			}
		}
	}

	/**
	 * Obtains {@link DeployedOfficeModel} for the {@link DeployedOfficeInputModel}.
	 * 
	 * @param officeInputModel {@link DeployedOfficeInputModel}.
	 * @param officeFloor      {@link OfficeFloorModel}.
	 * @return {@link DeployedOfficeModel}.
	 */
	private DeployedOfficeModel getOfficeForInput(DeployedOfficeInputModel officeInputModel,
			OfficeFloorModel officeFloor) {

		// Find the office for the input
		for (DeployedOfficeModel office : officeFloor.getDeployedOffices()) {
			for (DeployedOfficeInputModel input : office.getDeployedOfficeInputs()) {
				if (input == officeInputModel) {
					// Found input, return containing office
					return office;
				}
			}
		}

		// No office if at this point
		return null;
	}

	/**
	 * Obtains the {@link ManagedObjectScope} from the managed object scope name.
	 * 
	 * @param managedObjectScope Name of the {@link ManagedObjectScope}.
	 * @param deployer           {@link OfficeFloorDeployer}.
	 * @param managedObjectName  Name of the {@link OfficeFloorManagedObjectModel}.
	 * @return {@link ManagedObjectScope} or <code>null</code> with issue reported
	 *         to the {@link OfficeFloorDeployer}.
	 */
	private ManagedObjectScope getManagedObjectScope(String managedObjectScope, OfficeFloorDeployer deployer,
			String managedObjectName) {

		// Obtain the managed object scope
		if (OfficeFloorChanges.PROCESS_MANAGED_OBJECT_SCOPE.equals(managedObjectScope)) {
			return ManagedObjectScope.PROCESS;
		} else if (OfficeFloorChanges.THREAD_MANAGED_OBJECT_SCOPE.equals(managedObjectScope)) {
			return ManagedObjectScope.THREAD;
		} else if (OfficeFloorChanges.FUNCTION_MANAGED_OBJECT_SCOPE.equals(managedObjectScope)) {
			return ManagedObjectScope.FUNCTION;
		}

		// Unknown scope if at this point
		deployer.addIssue(
				"Unknown managed object scope " + managedObjectScope + " for managed object " + managedObjectName);
		return null;
	}

}
