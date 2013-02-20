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
package net.officefloor.compile.impl.officefloor;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;

/**
 * Tests loading a {@link Task}.
 * 
 * @author Daniel Sagenschneider
 */
public class LoadTaskTest extends AbstractOfficeFloorTestCase {

	/**
	 * {@link WorkFactory}.
	 */
	private final WorkFactory<Work> workFactory = this.createMockWorkFactory();

	/**
	 * {@link TaskFactory}.
	 */
	private final TaskFactory<Work, ?, ?> taskFactory = this
			.createMockTaskFactory();

	/**
	 * Ensure can load a simple {@link Task}.
	 */
	public void testLoadSimpleTask() {

		// Record loading the task
		this.record_initiateOfficeFloorBuilder();
		this.record_officefloor_addTeam("OFFICE_FLOOR_TEAM");
		OfficeBuilder officeBuilder = this
				.record_officefloor_addOffice("OFFICE");
		officeBuilder.registerTeam("OFFICE_TEAM", "OFFICE_FLOOR_TEAM");
		this.record_office_addWork("SECTION.WORK", this.workFactory);
		TaskBuilder<?, ?, ?> taskBuilder = this.record_work_addTask("TASK",
				this.taskFactory);
		taskBuilder.setTeam("OFFICE_TEAM");

		// Loads the OfficeFloor with a simple task
		this.loadOfficeFloor(true, new OfficeFloorMaker() {
			@Override
			public void make(OfficeFloorMakerContext context) throws Exception {
				OfficeFloorDeployer deployer = context.getDeployer();

				// Add an office with a team
				DeployedOffice office = context.addOffice("OFFICE",
						new OfficeMaker() {
							@Override
							public void make(OfficeMakerContext context) {
								OfficeArchitect architect = context
										.getArchitect();

								// Add the section with a team
								OfficeSection section = context.addSection(
										"SECTION", new SectionMaker() {
											@Override
											public void make(
													SectionMakerContext context) {
												context.addTask("WORK",
														workFactory, "TASK",
														taskFactory, null);
											}
										});

								// Link in team responsible for task
								architect.link(section.getOfficeTasks()[0]
										.getTeamResponsible(), architect
										.addOfficeTeam("OFFICE_TEAM"));
							}
						});

				// Specify team responsible for task
				deployer.link(office.getDeployedOfficeTeam("OFFICE_TEAM"),
						context.addTeam("OFFICE_FLOOR_TEAM", null));
			}
		});
	}

}