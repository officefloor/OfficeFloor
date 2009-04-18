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
package net.officefloor.compile.impl.officefloor;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.compile.spi.work.source.TaskFactoryManufacturer;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.build.TeamBuilder;
import net.officefloor.frame.api.build.WorkBuilder;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;

/**
 * Tests loading a {@link Task}.
 * 
 * @author Daniel
 */
public class LoadTaskTest extends AbstractOfficeFloorTestCase {

	/**
	 * Ensure can load a simple {@link Task}.
	 */
	public void testLoadSimpleTask() {

		final TeamBuilder<MakerTeamSource> teamBuilder = this
				.createMockTeamBuilder();
		final OfficeBuilder officeBuilder = this.createMockOfficeBuilder();
		final WorkBuilder<Work> workBuilder = this.createMockWorkBuilder();
		final WorkFactory<Work> workFactory = this.createMockWorkFactory();
		final TaskBuilder<Work, ?, ?> taskBuilder = this
				.createMockTaskBuilder();
		final TaskFactoryManufacturer<Work, ?, ?> manufacturer = this
				.createMockTaskFactoryManufacturer();
		final TaskFactory<Work, ?, ?> taskFactory = this
				.createMockTaskFactory();

		// Record loading the task
		this.recordReturn(this.officeFloorBuilder, this.officeFloorBuilder
				.addTeam("TEAM", MakerTeamSource.class), teamBuilder);
		this.recordReturn(this.officeFloorBuilder, this.officeFloorBuilder
				.addOffice("OFFICE"), officeBuilder);
		officeBuilder.registerTeam("OFFICE_TEAM", "OFFICE_FLOOR_TEAM");
		this.recordReturn(officeBuilder, officeBuilder.addWork("SECTION.WORK",
				workFactory), workBuilder);
		this.recordReturn(manufacturer, manufacturer.createTaskFactory(),
				taskFactory);
		this.recordReturn(workBuilder,
				workBuilder.addTask("TASK", taskFactory), taskBuilder);
		taskBuilder.setTeam("OFFICE_TEAM");

		// Loads the office floor with a single task
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
														manufacturer, null);
											}
										});

								// Link in team responsible for task
								architect.link(section.getOfficeTasks()[0]
										.getTeamResponsible(), architect
										.getTeam("OFFICE_TEAM"));
							}
						});

				// Specify team responsible for task
				OfficeFloorTeam team = context.addTeam("OFFICE_FLOOR_TEAM",
						null);
				deployer.link(office.getOfficeTeam("OFFICE_TEAM"), team);
			}
		});
	}
}