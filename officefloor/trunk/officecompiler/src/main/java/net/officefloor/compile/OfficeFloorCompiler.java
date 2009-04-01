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

import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.model.desk.WorkTaskModel;
import net.officefloor.model.desk.WorkTaskToTaskModel;
import net.officefloor.model.desk.WorkModel;
import net.officefloor.model.desk.TaskModel;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.office.OfficeRoomModel;
import net.officefloor.model.officefloor.ManagedObjectSourceModel;
import net.officefloor.model.officefloor.OfficeFloorModel;
import net.officefloor.model.officefloor.OfficeFloorOfficeModel;
import net.officefloor.model.officefloor.TeamModel;
import net.officefloor.model.repository.ConfigurationItem;

/**
 * Compiles the {@link OfficeFloorModel} into an {@link OfficeFloor}.
 * 
 * @author Daniel
 */
public class OfficeFloorCompiler {

	/**
	 * {@link ModelRepositoryImpl}.
	 */
	private final ModelRepositoryImpl repository;

	/**
	 * Default constructor.
	 */
	public OfficeFloorCompiler() {
		this(new ModelRepositoryImpl());
	}

	/**
	 * Initiate.
	 * 
	 * @param repository
	 *            {@link ModelRepositoryImpl}.
	 */
	public OfficeFloorCompiler(ModelRepositoryImpl repository) {
		this.repository = repository;
	}

	/**
	 * Compiles the {@link OfficeFloor} from the {@link OfficeFloorModel}.
	 * 
	 * @param configuration
	 *            Configuration containing the {@link OfficeFloorModel}.
	 * @param officeFrame
	 *            {@link OfficeFrame} to construct the {@link OfficeFloor}.
	 * @param builderContext
	 *            {@link LoaderContext} to use in building and compiling the
	 *            {@link net.officefloor.model.officefloor.OfficeFloorModel}.
	 * @return Compiled {@link OfficeFloor}.
	 */
	public OfficeFloor compileOfficeFloor(ConfigurationItem configuration,
			OfficeFrame officeFrame, LoaderContext builderContext)
			throws Exception {

		// Create the compiler context
		OfficeFloorCompilerContext context = new OfficeFloorCompilerContext(
				configuration.getContext(), this.repository, builderContext);

		// Load the office floor
		OfficeFloorEntry officeFloorEntry = OfficeFloorEntry.loadOfficeFloor(
				configuration, context);

		// TODO remove
		System.out.println("[" + this.getClass().getName() + " (todo remove):");
		for (OfficeFloorOfficeModel officeModel : officeFloorEntry.getModel()
				.getOffices()) {
			System.out.println(officeModel.getName());
			OfficeEntry officeEntry = officeFloorEntry
					.getOfficeEntry(officeModel);

			OfficeRoomModel officeRoom = officeEntry.getModel().getRoom();
			if (officeRoom != null) {
				RoomEntry roomEntry = officeEntry.getRoomEntry(officeRoom);
				for (DeskEntry deskEntry : roomEntry.getDeskEntries()) {
					for (WorkModel workModel : deskEntry.getModel()
							.getWorks()) {
						WorkEntry<?> workEntry = deskEntry
								.getWorkEntry(workModel);
						System.out.print("   "
								+ workEntry.getCanonicalWorkName() + " [");
						for (WorkTaskModel taskModel : workEntry.getModel()
								.getWorkTasks()) {
							for (WorkTaskToTaskModel taskToFlow : taskModel
									.getTasks()) {
								TaskModel flowItemModel = taskToFlow
										.getTask();
								TaskEntry<?> taskEntry = workEntry
										.getTaskEntry(flowItemModel);
								System.out.print(" "
										+ taskEntry.getModel().getTaskName());
							}
						}
						System.out.println(" ]");
					}
				}
			}
		}
		System.out.print("Managed Object Sources:");
		for (ManagedObjectSourceModel mosModel : officeFloorEntry.getModel()
				.getManagedObjectSources()) {
			ManagedObjectSourceEntry mosEntry = officeFloorEntry
					.getManagedObjectSourceEntry(mosModel);
			System.out.print(" " + mosEntry.getModel().getId());
		}
		System.out.println();
		System.out.print("Teams:");
		for (TeamModel teamModel : officeFloorEntry.getModel().getTeams()) {
			TeamEntry teamEntry = officeFloorEntry.getTeamEntry(teamModel);
			System.out.print(" " + teamEntry.getId());
		}
		System.out.println();

		// Build the office floor
		for (WorkEntry<?> workEntry : officeFloorEntry.getWorkEntries()) {
			workEntry.build();
		}
		for (OfficeEntry officeEntry : officeFloorEntry.getOfficeEntries()) {
			officeEntry.build(builderContext);
		}
		for (ManagedObjectSourceEntry mosEntry : officeFloorEntry
				.getManagedObjectSourceEntries()) {
			mosEntry.build(builderContext);
		}
		for (TeamEntry teamEntry : officeFloorEntry.getTeamEntries()) {
			teamEntry.build(builderContext);
		}
		
		// Return the built office
		// TODO use OfficeFloorIssues to record construct failures
		return officeFloorEntry.getBuilder().buildOfficeFloor();
	}

}