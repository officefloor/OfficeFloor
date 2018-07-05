/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.compile.impl.officefloor;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.frame.api.build.ManagedFunctionBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionFactory;

/**
 * Tests loading a {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public class LoadFunctionTest extends AbstractOfficeFloorTestCase {

	/**
	 * Ensure can load a simple {@link ManagedFunction}.
	 */
	public void testLoadSimpleFunction() {

		ManagedFunctionFactory<?, ?> functionFactory = this.createMock(ManagedFunctionFactory.class);

		// Record loading the function
		this.record_initiateOfficeFloorBuilder();
		this.record_officefloor_addTeam("OFFICE_FLOOR_TEAM");
		OfficeBuilder officeBuilder = this.record_officefloor_addOffice("OFFICE");
		officeBuilder.registerTeam("OFFICE_TEAM", "OFFICE_FLOOR_TEAM");
		ManagedFunctionBuilder<?, ?> functionBuilder = this.record_office_addFunction("SECTION.FUNCTION",
				functionFactory);
		functionBuilder.setResponsibleTeam("OFFICE_TEAM");

		// Loads the OfficeFloor with a simple function
		this.loadOfficeFloor(true, new OfficeFloorMaker() {
			@Override
			public void make(OfficeFloorMakerContext context) throws Exception {
				OfficeFloorDeployer deployer = context.getDeployer();

				// Add an office with a team
				DeployedOffice office = context.addOffice("OFFICE", new OfficeMaker() {
					@Override
					public void make(OfficeMakerContext context) {
						OfficeArchitect architect = context.getArchitect();

						// Add the section with a team
						OfficeSection section = context.addSection("SECTION", new SectionMaker() {
							@Override
							public void make(SectionMakerContext context) {
								context.addFunction("NAMESPACE", "FUNCTION", functionFactory, null);
							}
						});

						// Link in team responsible for function
						architect.link(section.getOfficeSectionFunction("FUNCTION").getResponsibleTeam(),
								architect.addOfficeTeam("OFFICE_TEAM"));
					}
				});

				// Specify team responsible for function
				deployer.link(office.getDeployedOfficeTeam("OFFICE_TEAM"), context.addTeam("OFFICE_FLOOR_TEAM", null));
			}
		});
	}

}