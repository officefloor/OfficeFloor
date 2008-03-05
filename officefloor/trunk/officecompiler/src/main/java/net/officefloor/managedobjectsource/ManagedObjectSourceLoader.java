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
package net.officefloor.managedobjectsource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.build.BuilderFactory;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.build.issue.OfficeIssuesListener;
import net.officefloor.frame.api.execute.Handler;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.ClassLoaderResourceLocator;
import net.officefloor.frame.impl.ManagedObjectSourceContextImpl;
import net.officefloor.frame.impl.construct.BuilderFactoryImpl;
import net.officefloor.frame.internal.configuration.FlowConfiguration;
import net.officefloor.frame.internal.configuration.HandlerConfiguration;
import net.officefloor.frame.internal.configuration.HandlerFlowConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectSourceConfiguration;
import net.officefloor.frame.internal.configuration.OfficeConfiguration;
import net.officefloor.frame.internal.configuration.OfficeFloorConfiguration;
import net.officefloor.frame.internal.configuration.TaskConfiguration;
import net.officefloor.frame.internal.configuration.TaskNodeReference;
import net.officefloor.frame.internal.configuration.WorkConfiguration;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ResourceLocator;
import net.officefloor.model.officefloor.ManagedObjectHandlerInstanceModel;
import net.officefloor.model.officefloor.ManagedObjectHandlerLinkProcessModel;
import net.officefloor.model.officefloor.ManagedObjectHandlerModel;
import net.officefloor.model.officefloor.ManagedObjectSourceModel;
import net.officefloor.model.officefloor.ManagedObjectTaskFlowModel;
import net.officefloor.model.officefloor.ManagedObjectTaskModel;
import net.officefloor.model.officefloor.ManagedObjectTeamModel;
import net.officefloor.model.officefloor.PropertyModel;

/**
 * Loads the {@link ManagedObjectSourceModel}.
 * 
 * @author Daniel
 */
public class ManagedObjectSourceLoader {

	/**
	 * Loads and returns the {@link ManagedObjectSourceModel}.
	 * 
	 * @param managedObjectSourceName
	 *            Name of the {@link ManagedObjectSourceModel}.
	 * @param managedObjectSource
	 *            {@link ManagedObjectSource} instance.
	 * @param properties
	 *            {@link Properties} for the {@link ManagedObjectSource}.
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @return {@link ManagedObjectSourceModel}.
	 * @throws Throwable
	 *             If fails to load the {@link ManagedObjectSourceModel}. It is
	 *             {@link Throwable} as {@link Error} instances may be thrown
	 *             from
	 *             {@link ManagedObjectSource#init(net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext)}.
	 */
	public ManagedObjectSourceModel loadManagedObjectSource(
			String managedObjectSourceName,
			ManagedObjectSource managedObjectSource, Properties properties,
			ClassLoader classLoader) throws Throwable {

		// Create the resource loader
		ResourceLocator projectResourceLoader = new ClassLoaderResourceLocator(
				classLoader);

		// Create an office frame to capture additional configuration
		LoaderOfficeFrame officeFrame = new LoaderOfficeFrame();

		// Create the managed object builder
		ManagedObjectBuilder managedObjectBuilder = officeFrame
				.getBuilderFactory().createManagedObjectBuilder();

		// Create the office builder
		OfficeBuilder officeBuilder = officeFrame.getBuilderFactory()
				.createOfficeBuilder();

		// Initialise managed object to obtain addition configuration
		managedObjectSource.init(new ManagedObjectSourceContextImpl(
				managedObjectSourceName, properties, projectResourceLoader,
				managedObjectBuilder, officeBuilder, officeFrame));

		// Create the properties for the managed object source
		List<PropertyModel> propertyModels = new LinkedList<PropertyModel>();
		for (String propertyName : properties.stringPropertyNames()) {
			String propertyValue = properties.getProperty(propertyName);
			propertyModels.add(new PropertyModel(propertyName, propertyValue));
		}

		// Translate to configuration
		ManagedObjectSourceConfiguration mosConfig = (ManagedObjectSourceConfiguration) managedObjectBuilder;

		// Obtain the handler configuration
		List<ManagedObjectHandlerModel> handlerModels = new LinkedList<ManagedObjectHandlerModel>();
		for (HandlerConfiguration<?, ?> handlerConfig : mosConfig
				.getHandlerConfiguration()) {

			// Obtain the handler key name
			String handlerKeyName = handlerConfig.getHandlerKey().name();

			// Determine if handler is provided by managed object source
			if (handlerConfig.getHandlerFactory() == null) {
				// Handler to be specified
				handlerModels.add(new ManagedObjectHandlerModel(handlerKeyName,
						handlerConfig.getHandlerType().getName(), null));

			} else {
				// Hander factory specified only linking to be provided

				// Create the listing of flows for the handler instance
				List<ManagedObjectHandlerLinkProcessModel> handlerFlows = new LinkedList<ManagedObjectHandlerLinkProcessModel>();
				for (HandlerFlowConfiguration<?> handlerFlow : handlerConfig
						.getLinkedProcessConfiguration()) {
					// Obtain the details of the handler flow
					String flowName = handlerFlow.getFlowName();
					TaskNodeReference taskFlow = handlerFlow
							.getTaskNodeReference();
					String workName = (taskFlow == null ? null : taskFlow
							.getWorkName());
					String taskName = (taskFlow == null ? null : taskFlow
							.getTaskName());

					// Create and register the flow for the handler
					handlerFlows.add(new ManagedObjectHandlerLinkProcessModel(
							flowName, workName, taskName));
				}

				// Create the handler instance
				ManagedObjectHandlerInstanceModel handlerInstance = new ManagedObjectHandlerInstanceModel(
						new Boolean(true),
						null,
						null,
						handlerFlows
								.toArray(new ManagedObjectHandlerLinkProcessModel[0]));

				// Create and register the handler model
				ManagedObjectHandlerModel handlerModel = new ManagedObjectHandlerModel(
						handlerKeyName, Handler.class.getName(),
						handlerInstance);
				handlerModels.add(handlerModel);
			}
		}

		// Obtain the task configuration (including the teams)
		OfficeConfiguration officeConfig = (OfficeConfiguration) officeBuilder;
		List<ManagedObjectTaskModel> taskModels = new LinkedList<ManagedObjectTaskModel>();
		Set<String> teamNames = new HashSet<String>();
		for (WorkConfiguration<? extends Work> workConfig : officeConfig
				.getWorkConfiguration()) {

			// Obtain the work name
			String workName = workConfig.getWorkName();
			for (TaskConfiguration<?, ? extends Work, ?, ?> taskConfig : workConfig
					.getTaskConfiguration()) {

				// Obtain details of the task
				String taskName = taskConfig.getTaskName();
				String teamName = taskConfig.getTeamId();

				// Add team if specified
				if (teamName != null) {
					teamNames.add(teamName);
				}

				// Obtain the flows of the task
				List<ManagedObjectTaskFlowModel> taskFlows = new LinkedList<ManagedObjectTaskFlowModel>();
				for (FlowConfiguration flowConfig : taskConfig
						.getFlowConfiguration()) {

					// Obtain the flow details
					String flowName = flowConfig.getFlowName();
					TaskNodeReference initialTask = flowConfig.getInitialTask();
					String flowWorkName = (initialTask == null ? null
							: initialTask.getWorkName());
					String flowTaskName = (initialTask == null ? null
							: initialTask.getTaskName());

					// Add the flow
					taskFlows.add(new ManagedObjectTaskFlowModel(flowName,
							flowWorkName, flowTaskName));
				}

				// Add the task
				taskModels.add(new ManagedObjectTaskModel(workName, taskName,
						teamName, taskFlows
								.toArray(new ManagedObjectTaskFlowModel[0])));
			}
		}

		// Create the listing of teams
		String[] orderedTeamNames = teamNames.toArray(new String[0]);
		Arrays.sort(orderedTeamNames);
		ManagedObjectTeamModel[] teams = new ManagedObjectTeamModel[orderedTeamNames.length];
		for (int i = 0; i < teams.length; i++) {
			teams[i] = new ManagedObjectTeamModel(orderedTeamNames[i]);
		}

		// Create the managed object source model
		ManagedObjectSourceModel managedObjectSourceModel = new ManagedObjectSourceModel(
				managedObjectSourceName, managedObjectSource.getClass()
						.getName(), null, propertyModels
						.toArray(new PropertyModel[0]), handlerModels
						.toArray(new ManagedObjectHandlerModel[0]), taskModels
						.toArray(new ManagedObjectTaskModel[0]), teams, null);

		// Return the managed object source model
		return managedObjectSourceModel;
	}

	/**
	 * {@link OfficeFrame} for loading the {@link ManagedObjectSourceModel}.
	 */
	protected class LoaderOfficeFrame extends OfficeFrame {

		/**
		 * {@link BuilderFactory}.
		 */
		private final BuilderFactory builderFactory = new BuilderFactoryImpl();

		/**
		 * Registry of {@link OfficeFloorConfiguration} by office floor name.
		 */
		public final Map<String, OfficeFloorConfiguration> officeFloors = new HashMap<String, OfficeFloorConfiguration>();

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.api.OfficeFrame#getBuilderFactory()
		 */
		@Override
		public BuilderFactory getBuilderFactory() {
			return this.builderFactory;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.api.OfficeFrame#registerOfficeFloor(java.lang.String,
		 *      net.officefloor.frame.api.build.OfficeFloorBuilder,
		 *      net.officefloor.frame.api.build.issue.OfficeIssuesListener)
		 */
		@Override
		protected OfficeFloor registerOfficeFloor(String name,
				OfficeFloorBuilder officeFloorBuilder,
				OfficeIssuesListener issuesListener) throws Exception {

			// Transform Office Floor Builder into Configuration
			OfficeFloorConfiguration officeFloorConfiguration = (OfficeFloorConfiguration) officeFloorBuilder;

			// Register the office floor configuration
			this.officeFloors.put(name, officeFloorConfiguration);

			// No office floor to be returned
			return null;
		}
	}

}
