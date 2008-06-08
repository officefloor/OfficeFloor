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
package net.officefloor.compile;

import net.officefloor.LoaderContext;
import net.officefloor.frame.api.build.BuildException;
import net.officefloor.frame.api.build.FlowNodeBuilder;
import net.officefloor.frame.api.build.HandlerBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.ManagedObjectHandlerBuilder;
import net.officefloor.frame.api.build.OfficeEnhancer;
import net.officefloor.frame.api.build.OfficeEnhancerContext;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.construct.OfficeBuilderImpl;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.model.officefloor.FlowTaskToOfficeTaskModel;
import net.officefloor.model.officefloor.LinkProcessToOfficeTaskModel;
import net.officefloor.model.officefloor.ManagedObjectHandlerInstanceModel;
import net.officefloor.model.officefloor.ManagedObjectHandlerLinkProcessModel;
import net.officefloor.model.officefloor.ManagedObjectHandlerModel;
import net.officefloor.model.officefloor.ManagedObjectSourceModel;
import net.officefloor.model.officefloor.ManagedObjectTaskFlowModel;
import net.officefloor.model.officefloor.ManagedObjectTaskModel;
import net.officefloor.model.officefloor.ManagedObjectTeamModel;
import net.officefloor.model.officefloor.OfficeFloorOfficeModel;
import net.officefloor.model.officefloor.PropertyModel;
import net.officefloor.model.officefloor.TeamModel;
import net.officefloor.util.OFCU;

/**
 * {@link ManagedObjectSource} entry for the {@link OfficeFloor}.
 * 
 * @author Daniel
 */
public class ManagedObjectSourceEntry extends
		AbstractEntry<ManagedObjectBuilder<?>, ManagedObjectSourceModel> {

	/**
	 * Loads the {@link ManagedObjectSourceEntry}.
	 * 
	 * @param configuration
	 *            {@link ManagedObjectSourceModel}.
	 * @param officeFloorEntry
	 *            {@link OfficeFloorEntry} containing this
	 *            {@link ManagedObjectSourceEntry}.
	 * @param context
	 *            {@link OfficeFloorCompilerContext}.
	 * @return {@link ManagedObjectSourceEntry}.
	 * @throws Exception
	 *             If fails.
	 */
	@SuppressWarnings("unchecked")
	public static ManagedObjectSourceEntry loadManagedObjectSource(
			ManagedObjectSourceModel configuration,
			OfficeFloorEntry officeFloorEntry,
			OfficeFloorCompilerContext context) throws Exception {

		// Obtain the managed object source class
		Class managedObjectSourceClass = context.getLoaderContext()
				.obtainClass(configuration.getSource());

		// Create the builder
		ManagedObjectBuilder<?> builder = context.getBuilderFactory()
				.createManagedObjectBuilder(managedObjectSourceClass);

		// Create the entry
		ManagedObjectSourceEntry mosEntry = new ManagedObjectSourceEntry(
				configuration.getId(), builder, configuration, officeFloorEntry);

		// Return the entry
		return mosEntry;
	}

	/**
	 * {@link OfficeFloorEntry} containing this {@link ManagedObjectSourceEntry}.
	 */
	private final OfficeFloorEntry officeFloorEntry;

	/**
	 * Initiate.
	 * 
	 * @param id
	 *            Id of the
	 *            {@link net.officefloor.frame.spi.managedobject.source.ManagedObjectSource}.
	 * @param builder
	 *            {@link ManagedObjectBuilder}.
	 * @param model
	 *            {@link ManagedObjectSourceModel}.
	 */
	public ManagedObjectSourceEntry(String id, ManagedObjectBuilder<?> builder,
			ManagedObjectSourceModel model, OfficeFloorEntry officeFloorEntry) {
		super(id, builder, model);
		this.officeFloorEntry = officeFloorEntry;
	}

	/**
	 * Obtains the {@link OfficeFloorEntry}.
	 * 
	 * @return {@link OfficeFloorEntry}.
	 */
	public OfficeFloorEntry getOfficeFloorEntry() {
		return this.officeFloorEntry;
	}

	/**
	 * Builds the {@link ManagedObjectSource}.
	 */
	@SuppressWarnings("unchecked")
	public void build(final LoaderContext builderUtil) throws Exception {

		// Obtain the managing office of this managed object source
		OfficeFloorOfficeModel managingOffice = OFCU.get(
				this.getModel().getManagingOffice(),
				"No managing office for managed object source ${0}",
				this.getModel().getId()).getManagingOffice();

		// Configure attributes
		this.getBuilder().setManagingOffice(managingOffice.getName());

		// Specify the default timeout
		String defaultTimeoutText = this.getModel().getDefaultTimeout();
		if ((defaultTimeoutText != null)
				&& (defaultTimeoutText.trim().length() > 0)) {
			long defaultTimeout = Long.parseLong(defaultTimeoutText);
			this.getBuilder().setDefaultTimeout(defaultTimeout);
		}

		// Configure properties
		for (PropertyModel property : this.getModel().getProperties()) {
			this.getBuilder().addProperty(property.getName(),
					property.getValue());
		}

		// Provide flow node enhancing on the office
		OfficeEntry managingOfficeEntry = this.getOfficeFloorEntry()
				.getOfficeEntry(managingOffice);

		// Register the teams
		for (ManagedObjectTeamModel moTeam : this.getModel().getTeams()) {

			// Obtain the managed object name
			String managedObjectTeamName = OfficeBuilderImpl.getNamespacedName(
					this.getId(), moTeam.getTeamName());

			// Register the team
			TeamModel team = OFCU.get(moTeam.getTeam(),
					"Team ${0} not specified for managed object ${1}",
					moTeam.getTeamName(), this.getModel().getId()).getTeam();
			managingOfficeEntry.getBuilder().registerTeam(
					managedObjectTeamName, team.getId());
		}

		// Enhance with addition configuration of Managed Object Source
		managingOfficeEntry.getBuilder().addOfficeEnhancer(
				new OfficeEnhancer() {
					@Override
					public void enhanceOffice(OfficeEnhancerContext context)
							throws BuildException {
						// TODO implement
						System.err.println("TODO ["
								+ ManagedObjectSourceEntry.class
										.getSimpleName()
								+ "] implement office enhancement");

						// Obtain the managed object id
						String managedObjectId = ManagedObjectSourceEntry.this
								.getModel().getId();

						// Load the tasks for the handler link processes
						for (ManagedObjectHandlerModel handler : ManagedObjectSourceEntry.this
								.getModel().getHandlers()) {

							// Obtain the handler key class
							Class handlerKeyClass;
							try {
								handlerKeyClass = builderUtil
										.obtainClass(handler
												.getHandlerKeyClass());
							} catch (Exception ex) {
								throw new BuildException(OFCU.exMsg(ex));
							}

							// Obtain the handler key
							Enum handlerKey = null;
							String handlerKeyName = handler.getHandlerKey();
							for (Object keyObject : handlerKeyClass
									.getEnumConstants()) {
								Enum key = (Enum) keyObject;
								if (key.name().equals(handlerKeyName)) {
									handlerKey = key;
								}
							}

							// Obtain the handler builder
							ManagedObjectHandlerBuilder moHandlerBuilder = context
									.getManagedObjectHandlerBuilder(
											managedObjectId, handlerKeyClass);
							HandlerBuilder<Indexed> handlerBuilder = moHandlerBuilder
									.registerHandler(handlerKey);

							// Link in the processes
							ManagedObjectHandlerInstanceModel handlerInstance = handler
									.getHandlerInstance();
							if (handlerInstance != null) {
								for (ManagedObjectHandlerLinkProcessModel linkProcess : handlerInstance
										.getLinkProcesses()) {

									// Obtain the index of the link process
									int linkProcessIndex = Integer
											.parseInt(linkProcess
													.getLinkProcessId());

									// Link in the starting task of the process
									LinkProcessToOfficeTaskModel officeTask = linkProcess
											.getOfficeTask();
									if (officeTask != null) {
										handlerBuilder.linkProcess(
												linkProcessIndex, officeTask
														.getWorkName(),
												officeTask.getTaskName());
									}
								}
							}
						}

						// Load the tasks for the managed object task flows
						for (ManagedObjectTaskModel task : ManagedObjectSourceEntry.this
								.getModel().getTasks()) {
							for (ManagedObjectTaskFlowModel flow : task
									.getFlows()) {
								FlowTaskToOfficeTaskModel link = flow
										.getOfficeTask();
								if (link != null) {
									FlowNodeBuilder<?> flowNodeBuilder = context
											.getFlowNodeBuilder(
													ManagedObjectSourceEntry.this
															.getModel().getId(),
													task.getWorkName(), task
															.getTaskName());

									// Obtain the flow index
									int flowIndex = Integer.parseInt(flow
											.getFlowId());

									// Link in the task of the flow
									flowNodeBuilder
											.linkFlow(
													flowIndex,
													link.getWorkName(),
													link.getTaskName(),
													FlowInstigationStrategyEnum.SEQUENTIAL);
								}
							}
						}
					}
				});

		// Register managed object source with the office floor
		this.officeFloorEntry.getBuilder().addManagedObject(this.getId(),
				this.getBuilder());
	}
}
