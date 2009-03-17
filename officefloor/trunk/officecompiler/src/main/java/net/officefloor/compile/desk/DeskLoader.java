/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.compile.desk;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.officefloor.compile.LoaderContext;
import net.officefloor.compile.impl.work.source.WorkLoaderContextImpl;
import net.officefloor.compile.spi.work.source.WorkLoader;
import net.officefloor.compile.spi.work.source.WorkLoaderContext;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.DeskTaskModel;
import net.officefloor.model.desk.DeskTaskObjectModel;
import net.officefloor.model.desk.DeskTaskObjectToExternalManagedObjectModel;
import net.officefloor.model.desk.DeskTaskToFlowItemModel;
import net.officefloor.model.desk.DeskWorkModel;
import net.officefloor.model.desk.DeskWorkToFlowItemModel;
import net.officefloor.model.desk.ExternalEscalationModel;
import net.officefloor.model.desk.ExternalFlowModel;
import net.officefloor.model.desk.ExternalManagedObjectModel;
import net.officefloor.model.desk.FlowItemEscalationModel;
import net.officefloor.model.desk.FlowItemEscalationToExternalEscalationModel;
import net.officefloor.model.desk.FlowItemEscalationToFlowItemModel;
import net.officefloor.model.desk.FlowItemModel;
import net.officefloor.model.desk.FlowItemOutputModel;
import net.officefloor.model.desk.FlowItemOutputToExternalFlowModel;
import net.officefloor.model.desk.FlowItemOutputToFlowItemModel;
import net.officefloor.model.desk.FlowItemToNextExternalFlowModel;
import net.officefloor.model.desk.FlowItemToNextFlowItemModel;
import net.officefloor.model.desk.PropertyModel;
import net.officefloor.model.work.TaskFlowModel;
import net.officefloor.model.work.WorkModel;
import net.officefloor.repository.ConfigurationContext;
import net.officefloor.repository.ConfigurationItem;
import net.officefloor.repository.ModelRepository;
import net.officefloor.util.DoubleKeyMap;

/**
 * Loads the {@link net.officefloor.model.desk.DeskModel}.
 * 
 * @author Daniel
 */
public class DeskLoader {

	/**
	 * Sequential link type.
	 */
	public static final String SEQUENTIAL_LINK_TYPE = "Sequential";

	/**
	 * Parallel link type.
	 */
	public static final String PARALLEL_LINK_TYPE = "Parallel";

	/**
	 * Asynchronous link type.
	 */
	public static final String ASYNCHRONOUS_LINK_TYPE = "Asynchronous";

	/**
	 * {@link LoaderContext}.
	 */
	private final LoaderContext loaderContext;

	/**
	 * {@link ModelRepository}.
	 */
	private final ModelRepository modelRepository;

	/**
	 * Obtains the {@link FlowInstigationStrategyEnum} for the input link type.
	 * 
	 * @param linkType
	 *            Link type.
	 * @return {@link FlowInstigationStrategyEnum}.
	 * @throws Exception
	 *             If unknown link type.
	 */
	public static FlowInstigationStrategyEnum getFlowInstigationStrategyEnum(
			String linkType) throws Exception {
		if (SEQUENTIAL_LINK_TYPE.equals(linkType)) {
			return FlowInstigationStrategyEnum.SEQUENTIAL;
		} else if (PARALLEL_LINK_TYPE.equals(linkType)) {
			return FlowInstigationStrategyEnum.PARALLEL;
		} else if (ASYNCHRONOUS_LINK_TYPE.equals(linkType)) {
			return FlowInstigationStrategyEnum.ASYNCHRONOUS;
		} else {
			// Unknown strategy
			throw new Exception(
					"Unknown flow instigation strategy for link type '"
							+ linkType + "'");
		}
	}

	/**
	 * Obtains the Id for the input {@link FlowItemModel}.
	 * 
	 * @param flowItem
	 *            {@link FlowItemModel}.
	 * @return Id of the input {@link FlowItemModel}.
	 */
	public static String getFlowItemOutputId(TaskFlowModel<?> taskFlow) {
		// Enumeration takes priority over index
		Enum<?> enumValue = taskFlow.getFlowKey();
		if (enumValue != null) {
			return enumValue.toString();
		}

		// Fall back to the index
		int index = taskFlow.getFlowIndex();
		return String.valueOf(index);
	}

	/**
	 * Initiate.
	 * 
	 * @param loaderContext
	 *            {@link LoaderContext} for loading classes of {@link WorkModel}
	 *            .
	 * @param modelRepository
	 *            {@link ModelRepository}.
	 */
	public DeskLoader(LoaderContext loaderContext,
			ModelRepository modelRepository) {
		this.loaderContext = loaderContext;
		this.modelRepository = modelRepository;
	}

	/**
	 * Initiate.
	 * 
	 * @param loaderContext
	 *            {@link LoaderContext} for loading classes of {@link WorkModel}
	 *            .
	 */
	public DeskLoader(LoaderContext loaderContext) {
		this(loaderContext, new ModelRepository());
	}

	/**
	 * Convenience constructor.
	 * 
	 * @param classLoader
	 *            {@link java.lang.ClassLoader}.
	 */
	public DeskLoader(ClassLoader classLoader) {
		this(new LoaderContext(classLoader));
	}

	/**
	 * Loads the {@link DeskModel} without attaching the synchronisers.
	 * 
	 * @param configuration
	 *            {@link ConfigurationItem}.
	 * @return Configured {@link DeskModel}.
	 * @throws Exception
	 *             If fails.
	 */
	public DeskModel loadDesk(ConfigurationItem configuration) throws Exception {

		// Load the desk from the configuration
		DeskModel desk = this.modelRepository.retrieve(new DeskModel(),
				configuration);

		// Create the set of external managed objects
		Map<String, ExternalManagedObjectModel> externalManagedObjects = new HashMap<String, ExternalManagedObjectModel>();
		for (ExternalManagedObjectModel mo : desk.getExternalManagedObjects()) {
			externalManagedObjects.put(mo.getName(), mo);
		}

		// Connect the task objects to external managed objects
		for (DeskWorkModel work : desk.getWorks()) {
			for (DeskTaskModel task : work.getTasks()) {
				for (DeskTaskObjectModel taskObject : task.getObjects()) {
					// Obtain the connection
					DeskTaskObjectToExternalManagedObjectModel conn = taskObject
							.getManagedObject();
					if (conn != null) {
						// Obtain the external managed object
						ExternalManagedObjectModel extMo = externalManagedObjects
								.get(conn.getName());
						if (extMo != null) {
							// Connect
							conn.setTaskObject(taskObject);
							conn.setManagedObject(extMo);
							conn.connect();
						}
					}
				}
			}
		}

		// Create the set of external flows
		Map<String, ExternalFlowModel> externalFlows = new HashMap<String, ExternalFlowModel>();
		for (ExternalFlowModel flow : desk.getExternalFlows()) {
			externalFlows.put(flow.getName(), flow);
		}

		// Connect the flow item outputs to external flow
		for (FlowItemModel flow : desk.getFlowItems()) {
			for (FlowItemOutputModel output : flow.getOutputs()) {
				// Obtain the connection
				FlowItemOutputToExternalFlowModel conn = output
						.getExternalFlow();
				if (conn != null) {
					// Obtain the external flow
					ExternalFlowModel extFlow = externalFlows.get(conn
							.getName());
					if (extFlow != null) {
						// Connect
						conn.setOutput(output);
						conn.setExternalFlow(extFlow);
						conn.connect();
					}
				}
			}
		}

		// Connect the flow item to next external flow
		for (FlowItemModel flow : desk.getFlowItems()) {
			// Obtain the connection
			FlowItemToNextExternalFlowModel conn = flow.getNextExternalFlow();
			if (conn != null) {
				// Obtain the external flow
				ExternalFlowModel extFlow = externalFlows.get(conn
						.getExternalFlowName());
				if (extFlow != null) {
					// Connect
					conn.setPreviousFlowItem(flow);
					conn.setNextExternalFlow(extFlow);
					conn.connect();
				}
			}
		}

		// Create the set of flows
		Map<String, FlowItemModel> flowItems = new HashMap<String, FlowItemModel>();
		for (FlowItemModel flowItem : desk.getFlowItems()) {
			flowItems.put(flowItem.getId(), flowItem);
		}

		// Connect the flow item outputs to flow item
		for (FlowItemModel flow : desk.getFlowItems()) {
			for (FlowItemOutputModel output : flow.getOutputs()) {
				// Obtain the connection
				FlowItemOutputToFlowItemModel conn = output.getFlowItem();
				if (conn != null) {
					// Obtain the flow item
					FlowItemModel flowItem = flowItems.get(conn.getId());
					if (flowItem != null) {
						// Connect
						conn.setOutput(output);
						conn.setFlowItem(flowItem);
						conn.connect();
					}
				}
			}
		}

		// Connect the escalation to flow item
		for (FlowItemModel flow : desk.getFlowItems()) {
			for (FlowItemEscalationModel escalation : flow.getEscalations()) {
				// Obtain the connection
				FlowItemEscalationToFlowItemModel conn = escalation
						.getEscalationHandler();
				if (conn != null) {
					// Obtain the handling flow
					FlowItemModel flowItem = flowItems.get(conn.getId());
					if (flowItem != null) {
						// Connect
						conn.setEscalation(escalation);
						conn.setHandler(flowItem);
						conn.connect();
					}
				}
			}
		}

		// Create the set of external escalations
		Map<String, ExternalEscalationModel> externalEscalations = new HashMap<String, ExternalEscalationModel>();
		for (ExternalEscalationModel externalEscalation : desk
				.getExternalEscalations()) {
			externalEscalations.put(externalEscalation.getName(),
					externalEscalation);
		}

		// Connect the escalation to external escalation
		for (FlowItemModel flow : desk.getFlowItems()) {
			for (FlowItemEscalationModel escalation : flow.getEscalations()) {
				// Obtain the connection
				FlowItemEscalationToExternalEscalationModel conn = escalation
						.getExternalEscalation();
				if (conn != null) {
					// Obtain the external escalation
					ExternalEscalationModel externalEscalation = externalEscalations
							.get(conn.getName());
					if (externalEscalation != null) {
						// Connect
						conn.setEscalation(escalation);
						conn.setExternalEscalation(externalEscalation);
						conn.connect();
					}
				}
			}
		}

		// Connect the work to initial flow item
		for (DeskWorkModel deskWork : desk.getWorks()) {
			// Obtain the connection
			DeskWorkToFlowItemModel conn = deskWork.getInitialFlowItem();
			if (conn != null) {
				// Obtain the initial flow item
				FlowItemModel flowItem = flowItems.get(conn.getFlowItemId());
				if (flowItem != null) {
					// Connect
					conn.setDeskWork(deskWork);
					conn.setInitialFlowItem(flowItem);
					conn.connect();
				}
			}
		}

		// Connect the flow item to its next flow item
		for (FlowItemModel previous : desk.getFlowItems()) {
			// Obtain the connection
			FlowItemToNextFlowItemModel conn = previous.getNextFlowItem();
			if (conn != null) {
				// Obtain the flow item
				FlowItemModel next = flowItems.get(conn.getId());
				if (next != null) {
					// Connect
					conn.setPreviousFlowItem(previous);
					conn.setNextFlowItem(next);
					conn.connect();
				}
			}
		}

		// Create the set of tasks
		DoubleKeyMap<String, String, DeskTaskModel> taskRegistry = new DoubleKeyMap<String, String, DeskTaskModel>();
		for (DeskWorkModel deskWork : desk.getWorks()) {
			for (DeskTaskModel deskTask : deskWork.getTasks()) {
				taskRegistry
						.put(deskWork.getId(), deskTask.getName(), deskTask);
			}
		}

		// Connect the flows to their task
		for (FlowItemModel flowItem : desk.getFlowItems()) {
			// Obtain the task
			DeskTaskModel deskTask = taskRegistry.get(flowItem.getWorkName(),
					flowItem.getTaskName());
			if (deskTask != null) {
				// Connect
				new DeskTaskToFlowItemModel(flowItem, deskTask).connect();
			}
		}

		// Return the desk
		return desk;
	}

	/**
	 * Loads the {@link DeskModel} from the configuration attaching the
	 * synchronisers.
	 * 
	 * @param configuration
	 *            {@link ConfigurationItem}.
	 * @return Configured {@link DeskModel}.
	 * @throws Exception
	 *             If fails.
	 */
	public DeskModel loadDeskAndSynchronise(ConfigurationItem configuration)
			throws Exception {

		// Load the desk model
		DeskModel desk = this.loadDesk(configuration);

		// Obtain the context
		ConfigurationContext context = configuration.getContext();

		// Load the work for the desk
		for (DeskWorkModel work : desk.getWorks()) {
			this.loadWork(work, context);
		}

		// Load the flow for the desk
		List<FlowItemModel> removeFlowItems = new LinkedList<FlowItemModel>();
		for (FlowItemModel flowItem : desk.getFlowItems()) {
			if (!this.loadFlowItem(flowItem, desk)) {
				// Add flow item to remove
				removeFlowItems.add(flowItem);
			}
		}

		// Remove no longer existing flows
		for (FlowItemModel flowItem : removeFlowItems) {
			// Remove connection and flow
			flowItem.removeConnections();
			desk.removeFlowItem(flowItem);
		}

		// Return the desk model
		return desk;
	}

	/**
	 * Stores the {@link DeskModel} in the input {@link ConfigurationItem}.
	 * 
	 * @param desk
	 *            {@link DeskModel}.
	 * @param configuration
	 *            {@link ConfigurationItem}.
	 * @throws Exception
	 *             If fails to store the {@link DeskModel}.
	 */
	public void storeDesk(DeskModel desk, ConfigurationItem configuration)
			throws Exception {

		// Specify next flows
		for (FlowItemModel previous : desk.getFlowItems()) {
			FlowItemToNextFlowItemModel conn = previous.getNextFlowItem();
			if (conn != null) {
				conn.setId(conn.getNextFlowItem().getId());
			}
		}

		// Specify next external flow
		for (FlowItemModel previous : desk.getFlowItems()) {
			FlowItemToNextExternalFlowModel conn = previous
					.getNextExternalFlow();
			if (conn != null) {
				conn.setExternalFlowName(conn.getNextExternalFlow().getName());
			}
		}

		// Specify initial flow for work
		for (DeskWorkModel work : desk.getWorks()) {
			DeskWorkToFlowItemModel conn = work.getInitialFlowItem();
			if (conn != null) {
				conn.setFlowItemId(conn.getInitialFlowItem().getId());
			}
		}

		// Specify flow links
		for (FlowItemModel flowItem : desk.getFlowItems()) {
			for (FlowItemOutputModel flowItemOutput : flowItem.getOutputs()) {
				FlowItemOutputToFlowItemModel conn = flowItemOutput
						.getFlowItem();
				if (conn != null) {
					conn.setId(conn.getFlowItem().getId());
				}
			}
		}

		// Specify flow external links
		for (FlowItemModel flowItem : desk.getFlowItems()) {
			for (FlowItemOutputModel flowItemOutput : flowItem.getOutputs()) {
				FlowItemOutputToExternalFlowModel conn = flowItemOutput
						.getExternalFlow();
				if (conn != null) {
					conn.setName(conn.getExternalFlow().getName());
				}
			}
		}

		// Specify escalation handlers
		for (FlowItemModel flowItem : desk.getFlowItems()) {
			for (FlowItemEscalationModel flowItemEscalation : flowItem
					.getEscalations()) {
				FlowItemEscalationToFlowItemModel conn = flowItemEscalation
						.getEscalationHandler();
				if (conn != null) {
					conn.setId(conn.getHandler().getId());
				}
			}
		}

		// Specify external escalations
		for (FlowItemModel flowItem : desk.getFlowItems()) {
			for (FlowItemEscalationModel flowItemEscalation : flowItem
					.getEscalations()) {
				FlowItemEscalationToExternalEscalationModel conn = flowItemEscalation
						.getExternalEscalation();
				if (conn != null) {
					conn.setName(conn.getExternalEscalation().getName());
				}
			}
		}

		// Stores the desk
		this.modelRepository.store(desk, configuration);
	}

	/**
	 * Loads the {@link DeskWorkModel}.
	 * 
	 * @param work
	 *            {@link DeskWorkModel}.
	 * @param context
	 *            {@link ConfigurationContext}.
	 */
	public void loadWork(final DeskWorkModel work, ConfigurationContext context)
			throws Exception {

		// Obtain the name of the loader
		String loaderClassName = work.getLoader();
		if (loaderClassName != null) {

			// Create the work loader
			WorkLoader workLoader = this.loaderContext.createInstance(
					WorkLoader.class, loaderClassName);

			// Create the listing of properties and their names
			List<PropertyModel> propertyModels = work.getProperties();
			String[] propertyNames = new String[propertyModels.size()];
			Properties properties = new Properties();
			int index = 0;
			for (PropertyModel property : work.getProperties()) {
				String name = property.getName();
				String value = property.getValue();
				propertyNames[index++] = name;
				properties.setProperty(name, value);
			}

			// Obtain the class loader
			ClassLoader classLoader = this.loaderContext.getClassLoader();

			// Create the work loader context
			WorkLoaderContext workLoaderContext = new WorkLoaderContextImpl(
					propertyNames, properties, classLoader);

			// Load the work model
			WorkModel<?> workModel = workLoader.loadWork(workLoaderContext);

			// Synchronise the work
			WorkToDeskWorkSynchroniser.synchroniseWorkOntoDeskWork(workModel,
					work);
		}
	}

	/**
	 * Loads the {@link FlowItemModel}.
	 * 
	 * @param flowItem
	 *            {@link FlowItemModel}.
	 * @param desk
	 *            {@link DeskModel}.
	 * @return <code>true</code> if {@link FlowItemModel} should exist on work.
	 */
	private boolean loadFlowItem(FlowItemModel flowItem, DeskModel desk)
			throws Exception {

		// Obtain the work for the flow item
		DeskWorkModel work = null;
		for (DeskWorkModel model : desk.getWorks()) {
			String workName = flowItem.getWorkName();
			if ((workName != null) && (workName.equals(model.getId()))) {
				work = model;
			}
		}
		if (work == null) {
			// Work not found therefore do not load
			return false;
		}

		// Obtain the task for the flow item
		DeskTaskModel task = null;
		for (DeskTaskModel model : work.getTasks()) {
			if (flowItem.getTaskName().equals(model.getName())) {
				task = model;
			}
		}
		if (task == null) {
			// Task not found therefore do not load
			return false;
		}

		// Synchronise task to flow item
		TaskToFlowItemSynchroniser.synchroniseTaskOntoFlowItem(task.getTask(),
				flowItem);

		// Should keep flow item
		return true;
	}

}
