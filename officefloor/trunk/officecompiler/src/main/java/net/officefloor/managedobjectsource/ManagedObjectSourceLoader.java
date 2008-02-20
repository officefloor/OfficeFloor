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
import net.officefloor.frame.internal.configuration.TaskManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.TaskNodeReference;
import net.officefloor.frame.internal.configuration.WorkConfiguration;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ResourceLocator;
import net.officefloor.model.officefloor.ManagedObjectSourceModel;
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

		// TODO handle managed object additional configuration
		List<String> handlerFlows = new LinkedList<String>();
		ManagedObjectSourceConfiguration mosConfig = (ManagedObjectSourceConfiguration) managedObjectBuilder;
		HandlerConfiguration<?, ?>[] handlerConfigs = mosConfig
				.getHandlerConfiguration();
		for (HandlerConfiguration<?, ?> handlerConfig : handlerConfigs) {
			System.out.println("Handler " + handlerConfig.getHandlerKey());
			for (HandlerFlowConfiguration<?> handlerFlow : handlerConfig
					.getLinkedProcessConfiguration()) {
				TaskNodeReference flowTask = handlerFlow.getTaskNodeReference();
				System.out.println("  flow -> "
						+ handlerFlow.getFlowName()
						+ " "
						+ (flowTask == null ? "[no task]" : flowTask
								.getWorkName()
								+ ":" + flowTask.getTaskName()));

				// Add to handler flows if no task node referenced
				if (flowTask == null) {
					handlerFlows.add(handlerConfig.getHandlerKey().name() + "."
							+ handlerFlow.getFlowName());
				}
			}
		}

		// TODO handle office additional configuration
		List<String> flows = new LinkedList<String>();
		List<String> tasks = new LinkedList<String>();
		Set<String> teamNames = new HashSet<String>();
		OfficeConfiguration officeConfig = (OfficeConfiguration) officeBuilder;
		WorkConfiguration<? extends Work>[] workConfigs = officeConfig
				.getWorkConfiguration();
		for (WorkConfiguration<? extends Work> workConfig : workConfigs) {
			String workName = workConfig.getWorkName();
			for (TaskConfiguration<?, ? extends Work, ?, ?> taskConfig : workConfig
					.getTaskConfiguration()) {
				String taskName = taskConfig.getTaskName();
				String teamName = taskConfig.getTeamId();
				System.out.println(workName + ":" + taskName + " (team="
						+ teamName + ")");
				if (teamName != null) {
					teamNames.add(teamName);
				}
				for (FlowConfiguration flowConfig : taskConfig
						.getFlowConfiguration()) {
					String flowName = flowConfig.getFlowName();
					TaskNodeReference initialTask = flowConfig.getInitialTask();
					System.out.println("  flow "
							+ flowName
							+ " ["
							+ flowConfig.getInstigationStrategy()
							+ "] "
							+ (initialTask == null ? "[no task]" : initialTask
									.getWorkName()
									+ ":" + initialTask.getTaskName()));

					// Add to flows if no task node referenced
					if (initialTask == null) {
						flows.add(workName + "." + taskName + "." + flowName);
					}
				}
				TaskManagedObjectConfiguration[] moConfigs = taskConfig
						.getManagedObjectConfiguration();
				for (TaskManagedObjectConfiguration moConfig : moConfigs) {
					System.out.println("  mo->"
							+ moConfig.getWorkManagedObjectName());
				}

				// Add task if no team designated
				if (teamName == null) {
					tasks.add(workName + "." + taskName);
				}
			}
		}

		// TODO handle office frame additional configuration
		for (String officeFloorName : officeFrame.officeFloors.keySet()) {
			System.out.println("office floor: '" + officeFloorName + "'");
		}

		// TODO remove
		System.out.println();
		System.out.println("HANDLERS:");
		for (String handlerFlow : handlerFlows) {
			System.out.println("  " + handlerFlow);
		}
		System.out.println("FLOWS:");
		for (String flow : flows) {
			System.out.println("  " + flow);
		}
		System.out.println("TASKS:");
		for (String task : tasks) {
			System.out.println("  " + task);
		}
		System.out.println("TEAMS:");
		for (String teamName : teamNames) {
			System.out.println("  " + teamName);
		}

		// Create the managed object source model
		ManagedObjectSourceModel managedObjectSourceModel = new ManagedObjectSourceModel(
				managedObjectSourceName, managedObjectSource.getClass()
						.getName(), null, propertyModels
						.toArray(new PropertyModel[0]), null);

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
