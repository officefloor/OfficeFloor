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
package net.officefloor.compile.impl.desk;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.compile.LoaderContext;
import net.officefloor.compile.spi.work.WorkType;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.ExternalFlowModel;
import net.officefloor.model.desk.ExternalManagedObjectModel;
import net.officefloor.model.desk.TaskEscalationModel;
import net.officefloor.model.desk.TaskEscalationToExternalFlowModel;
import net.officefloor.model.desk.TaskEscalationToTaskModel;
import net.officefloor.model.desk.TaskFlowModel;
import net.officefloor.model.desk.TaskFlowToExternalFlowModel;
import net.officefloor.model.desk.TaskFlowToTaskModel;
import net.officefloor.model.desk.TaskModel;
import net.officefloor.model.desk.TaskToNextExternalFlowModel;
import net.officefloor.model.desk.TaskToNextTaskModel;
import net.officefloor.model.desk.WorkModel;
import net.officefloor.model.desk.WorkTaskModel;
import net.officefloor.model.desk.WorkTaskObjectModel;
import net.officefloor.model.desk.WorkTaskObjectToExternalManagedObjectModel;
import net.officefloor.model.desk.WorkTaskToTaskModel;
import net.officefloor.model.desk.WorkToInitialTaskModel;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.util.DoubleKeyMap;

/**
 * Loads the {@link net.officefloor.model.desk.DeskModel}.
 * 
 * @author Daniel
 */
public class DeskLoaderImpl {

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
	 * {@link ModelRepositoryImpl}.
	 */
	private final ModelRepositoryImpl modelRepository;

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
	 * Initiate.
	 * 
	 * @param loaderContext
	 *            {@link LoaderContext} for loading classes of {@link WorkType}
	 *            .
	 * @param modelRepository
	 *            {@link ModelRepositoryImpl}.
	 */
	public DeskLoaderImpl(LoaderContext loaderContext,
			ModelRepositoryImpl modelRepository) {
		this.loaderContext = loaderContext;
		this.modelRepository = modelRepository;
	}

	/**
	 * Initiate.
	 * 
	 * @param loaderContext
	 *            {@link LoaderContext} for loading classes of {@link WorkType}
	 *            .
	 */
	public DeskLoaderImpl(LoaderContext loaderContext) {
		this(loaderContext, new ModelRepositoryImpl());
	}

	/**
	 * Convenience constructor.
	 * 
	 * @param classLoader
	 *            {@link java.lang.ClassLoader}.
	 */
	public DeskLoaderImpl(ClassLoader classLoader) {
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
			externalManagedObjects.put(mo.getExternalManagedObjectName(), mo);
		}

		// Connect the task objects to external managed objects
		for (WorkModel work : desk.getWorks()) {
			for (WorkTaskModel task : work.getWorkTasks()) {
				for (WorkTaskObjectModel taskObject : task.getTaskObjects()) {
					// Obtain the connection
					WorkTaskObjectToExternalManagedObjectModel conn = taskObject
							.getExternalManagedObject();
					if (conn != null) {
						// Obtain the external managed object
						ExternalManagedObjectModel extMo = externalManagedObjects
								.get(conn.getExternalManagedObjectName());
						if (extMo != null) {
							// Connect
							conn.setTaskObject(taskObject);
							conn.setExternalManagedObject(extMo);
							conn.connect();
						}
					}
				}
			}
		}

		// Create the set of external flows
		Map<String, ExternalFlowModel> externalFlows = new HashMap<String, ExternalFlowModel>();
		for (ExternalFlowModel flow : desk.getExternalFlows()) {
			externalFlows.put(flow.getExternalFlowName(), flow);
		}

		// Connect the flow item outputs to external flow
		for (TaskModel flow : desk.getTasks()) {
			for (TaskFlowModel output : flow.getTaskFlows()) {
				// Obtain the connection
				TaskFlowToExternalFlowModel conn = output.getExternalFlow();
				if (conn != null) {
					// Obtain the external flow
					ExternalFlowModel extFlow = externalFlows.get(conn
							.getExternalFlowName());
					if (extFlow != null) {
						// Connect
						conn.setTaskFlow(output);
						conn.setExternalFlow(extFlow);
						conn.connect();
					}
				}
			}
		}

		// Connect the flow item to next external flow
		for (TaskModel flow : desk.getTasks()) {
			// Obtain the connection
			TaskToNextExternalFlowModel conn = flow.getNextExternalFlow();
			if (conn != null) {
				// Obtain the external flow
				ExternalFlowModel extFlow = externalFlows.get(conn
						.getExternalFlowName());
				if (extFlow != null) {
					// Connect
					conn.setPreviousTask(flow);
					conn.setNextExternalFlow(extFlow);
					conn.connect();
				}
			}
		}

		// Create the set of flows
		Map<String, TaskModel> flowItems = new HashMap<String, TaskModel>();
		for (TaskModel flowItem : desk.getTasks()) {
			flowItems.put(flowItem.getTaskName(), flowItem);
		}

		// Connect the flow item outputs to flow item
		for (TaskModel flow : desk.getTasks()) {
			for (TaskFlowModel output : flow.getTaskFlows()) {
				// Obtain the connection
				TaskFlowToTaskModel conn = output.getTask();
				if (conn != null) {
					// Obtain the flow item
					TaskModel flowItem = flowItems.get(conn.getTaskName());
					if (flowItem != null) {
						// Connect
						conn.setTaskFlow(output);
						conn.setTask(flowItem);
						conn.connect();
					}
				}
			}
		}

		// Connect the escalation to flow item
		for (TaskModel flow : desk.getTasks()) {
			for (TaskEscalationModel escalation : flow.getTaskEscalations()) {
				// Obtain the connection
				TaskEscalationToTaskModel conn = escalation.getTask();
				if (conn != null) {
					// Obtain the handling flow
					TaskModel flowItem = flowItems.get(conn.getTaskName());
					if (flowItem != null) {
						// Connect
						conn.setEscalation(escalation);
						conn.setTask(flowItem);
						conn.connect();
					}
				}
			}
		}

		// Connect the escalation to external flows
		for (TaskModel flow : desk.getTasks()) {
			for (TaskEscalationModel escalation : flow.getTaskEscalations()) {
				// Obtain the connection
				TaskEscalationToExternalFlowModel conn = escalation
						.getExternalFlow();
				if (conn != null) {
					// Obtain the external escalation
					ExternalFlowModel externalEscalation = externalFlows
							.get(conn.getExternalFlowName());
					if (externalEscalation != null) {
						// Connect
						conn.setTaskEscalation(escalation);
						conn.setExternalFlow(externalEscalation);
						conn.connect();
					}
				}
			}
		}

		// Connect the work to initial flow item
		for (WorkModel deskWork : desk.getWorks()) {
			// Obtain the connection
			WorkToInitialTaskModel conn = deskWork.getInitialTask();
			if (conn != null) {
				// Obtain the initial flow item
				TaskModel flowItem = flowItems.get(conn.getInitialTaskName());
				if (flowItem != null) {
					// Connect
					conn.setWork(deskWork);
					conn.setInitialTask(flowItem);
					conn.connect();
				}
			}
		}

		// Connect the flow item to its next flow item
		for (TaskModel previous : desk.getTasks()) {
			// Obtain the connection
			TaskToNextTaskModel conn = previous.getNextTask();
			if (conn != null) {
				// Obtain the flow item
				TaskModel next = flowItems.get(conn.getNextTaskName());
				if (next != null) {
					// Connect
					conn.setPreviousTask(previous);
					conn.setNextTask(next);
					conn.connect();
				}
			}
		}

		// Create the set of tasks
		DoubleKeyMap<String, String, WorkTaskModel> taskRegistry = new DoubleKeyMap<String, String, WorkTaskModel>();
		for (WorkModel deskWork : desk.getWorks()) {
			for (WorkTaskModel deskTask : deskWork.getWorkTasks()) {
				taskRegistry.put(deskWork.getWorkName(), deskTask
						.getWorkTaskName(), deskTask);
			}
		}

		// Connect the flows to their task
		for (TaskModel flowItem : desk.getTasks()) {
			// Obtain the task
			WorkTaskModel deskTask = taskRegistry.get(flowItem.getWorkName(),
					flowItem.getWorkTaskName());
			if (deskTask != null) {
				// Connect
				new WorkTaskToTaskModel(flowItem, deskTask).connect();
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
	// TODO synchronise should create do/undo object
	@Deprecated
	public DeskModel loadDeskAndSynchronise(ConfigurationItem configuration)
			throws Exception {

		// Load the desk model
		DeskModel desk = this.loadDesk(configuration);

		// TODO synchronise should create do/undo object
		// // Obtain the context
		// ConfigurationContext context = configuration.getContext();
		//
		// // Load the work for the desk
		// for (DeskWorkModel work : desk.getWorks()) {
		// this.loadWork(work, context);
		// }
		//
		// // Load the flow for the desk
		// List<FlowItemModel> removeFlowItems = new
		// LinkedList<FlowItemModel>();
		// for (FlowItemModel flowItem : desk.getFlowItems()) {
		// if (!this.loadFlowItem(flowItem, desk)) {
		// // Add flow item to remove
		// removeFlowItems.add(flowItem);
		// }
		// }
		//
		// // Remove no longer existing flows
		// for (FlowItemModel flowItem : removeFlowItems) {
		// // Remove connection and flow
		// flowItem.removeConnections();
		// desk.removeFlowItem(flowItem);
		// }

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
		for (TaskModel previous : desk.getTasks()) {
			TaskToNextTaskModel conn = previous.getNextTask();
			if (conn != null) {
				conn.setNextTaskName(conn.getNextTask().getTaskName());
			}
		}

		// Specify next external flow
		for (TaskModel previous : desk.getTasks()) {
			TaskToNextExternalFlowModel conn = previous.getNextExternalFlow();
			if (conn != null) {
				conn.setExternalFlowName(conn.getNextExternalFlow()
						.getExternalFlowName());
			}
		}

		// Specify initial flow for work
		for (WorkModel work : desk.getWorks()) {
			WorkToInitialTaskModel conn = work.getInitialTask();
			if (conn != null) {
				conn.setInitialTaskName(conn.getInitialTask().getTaskName());
			}
		}

		// Specify flow links
		for (TaskModel flowItem : desk.getTasks()) {
			for (TaskFlowModel flowItemOutput : flowItem.getTaskFlows()) {
				TaskFlowToTaskModel conn = flowItemOutput.getTask();
				if (conn != null) {
					conn.setTaskName(conn.getTask().getTaskName());
				}
			}
		}

		// Specify flow external links
		for (TaskModel flowItem : desk.getTasks()) {
			for (TaskFlowModel flowItemOutput : flowItem.getTaskFlows()) {
				TaskFlowToExternalFlowModel conn = flowItemOutput
						.getExternalFlow();
				if (conn != null) {
					conn.setExternalFlowName(conn.getExternalFlow()
							.getExternalFlowName());
				}
			}
		}

		// Specify escalation handlers
		for (TaskModel flowItem : desk.getTasks()) {
			for (TaskEscalationModel flowItemEscalation : flowItem
					.getTaskEscalations()) {
				TaskEscalationToTaskModel conn = flowItemEscalation.getTask();
				if (conn != null) {
					conn.setTaskName(conn.getTask().getTaskName());
				}
			}
		}

		// Specify external escalations
		for (TaskModel flowItem : desk.getTasks()) {
			for (TaskEscalationModel flowItemEscalation : flowItem
					.getTaskEscalations()) {
				TaskEscalationToExternalFlowModel conn = flowItemEscalation
						.getExternalFlow();
				if (conn != null) {
					conn.setExternalFlowName(conn.getExternalFlow().getExternalFlowName());
				}
			}
		}

		// Stores the desk
		this.modelRepository.store(desk, configuration);
	}

	// TODO use WorkType (and remove below)
	// /**
	// * Loads the {@link DeskWorkModel}.
	// *
	// * @param work
	// * {@link DeskWorkModel}.
	// * @param context
	// * {@link ConfigurationContext}.
	// */
	// public void loadWork(final DeskWorkModel work, ConfigurationContext
	// context)
	// throws Exception {
	//
	// // Obtain the name of the loader
	// String loaderClassName = work.getLoader();
	// if (loaderClassName != null) {
	//
	// // Create the work loader
	// WorkSource workLoader = this.loaderContext.createInstance(
	// WorkSource.class, loaderClassName);
	//
	// // Create the listing of properties and their names
	// List<PropertyModel> propertyModels = work.getProperties();
	// String[] propertyNames = new String[propertyModels.size()];
	// Properties properties = new Properties();
	// int index = 0;
	// for (PropertyModel property : work.getProperties()) {
	// String name = property.getName();
	// String value = property.getValue();
	// propertyNames[index++] = name;
	// properties.setProperty(name, value);
	// }
	//
	// // Obtain the class loader
	// ClassLoader classLoader = this.loaderContext.getClassLoader();
	//
	// // Create the work loader context
	// WorkSourceContext workLoaderContext = new WorkSourceContextImpl(
	// propertyNames, properties, classLoader);
	//
	// // Load the work model
	// WorkType<?> workModel = workLoader.loadWork(workLoaderContext);
	//
	// // Synchronise the work
	// WorkToDeskWorkSynchroniser.synchroniseWorkOntoDeskWork(workModel,
	// work);
	// }
	// }
	//
	// /**
	// * Loads the {@link FlowItemModel}.
	// *
	// * @param flowItem
	// * {@link FlowItemModel}.
	// * @param desk
	// * {@link DeskModel}.
	// * @return <code>true</code> if {@link FlowItemModel} should exist on
	// work.
	// */
	// private boolean loadFlowItem(FlowItemModel flowItem, DeskModel desk)
	// throws Exception {
	//
	// // Obtain the work for the flow item
	// DeskWorkModel work = null;
	// for (DeskWorkModel model : desk.getWorks()) {
	// String workName = flowItem.getWorkName();
	// if ((workName != null) && (workName.equals(model.getId()))) {
	// work = model;
	// }
	// }
	// if (work == null) {
	// // Work not found therefore do not load
	// return false;
	// }
	//
	// // Obtain the task for the flow item
	// DeskTaskModel task = null;
	// for (DeskTaskModel model : work.getTasks()) {
	// if (flowItem.getTaskName().equals(model.getName())) {
	// task = model;
	// }
	// }
	// if (task == null) {
	// // Task not found therefore do not load
	// return false;
	// }
	//
	// // Synchronise task to flow item
	// TaskToFlowItemSynchroniser.synchroniseTaskOntoFlowItem(task.getTask(),
	// flowItem);
	//
	// // Should keep flow item
	// return true;
	// }

}