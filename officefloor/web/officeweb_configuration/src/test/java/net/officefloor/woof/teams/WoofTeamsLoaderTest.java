/*-
 * #%L
 * Web configuration
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

package net.officefloor.woof.teams;

import java.io.ByteArrayInputStream;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeTeam;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionContext;
import net.officefloor.configuration.ConfigurationContext;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.impl.configuration.ClassLoaderConfigurationContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.woof.model.teams.WoofTeamsRepositoryImpl;

/**
 * Tests the {@link WoofTeamsLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofTeamsLoaderTest extends OfficeFrameTestCase {

	/**
	 * {@link OfficeFloorCompiler}.
	 */
	private final OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);

	/**
	 * {@link WoofTeamsLoader} to test.
	 */
	private final WoofTeamsLoader loader = new WoofTeamsLoaderImpl(
			new WoofTeamsRepositoryImpl(new ModelRepositoryImpl()));

	/**
	 * Ensure can load configuration to {@link OfficeFloorDeployer} with the teams.
	 */
	public void testLoadingOfficeFloorTeams() throws Exception {

		// Create specific OfficeFloor mocks
		final WoofTeamsLoaderContext loaderContext = this.createMock(WoofTeamsLoaderContext.class);
		final OfficeFloorDeployer deployer = this.createMock(OfficeFloorDeployer.class);
		final OfficeFloorExtensionContext extensionContext = this.createMock(OfficeFloorExtensionContext.class);
		final DeployedOffice office = this.createMock(DeployedOffice.class);

		// Obtain the configuration
		this.recordReturn(loaderContext, loaderContext.getOfficeFloorDeployer(), deployer);
		this.recordReturn(loaderContext, loaderContext.getOfficeFloorExtensionContext(), extensionContext);
		this.recordReturn(loaderContext, loaderContext.getDeployedOffice(), office);
		this.recordReturn(loaderContext, loaderContext.getConfiguration(), this.getConfiguration("Teams.teams.xml"));

		// Enable auto wire teams (for other managed object sources possibly deployed)
		deployer.enableAutoWireTeams();

		// Record first team
		final String teamOneName = "QUALIFIED_ONE:TYPE_ONE";
		OfficeFloorTeam teamOne = this.createMock(OfficeFloorTeam.class);
		this.recordReturn(deployer, deployer.addTeam(teamOneName, "net.example.ExampleTeamSource"), teamOne);
		teamOne.setTeamSize(50);
		teamOne.addTypeQualification("QUALIFIED_ONE", "TYPE_ONE");
		teamOne.addTypeQualification("QUALIFIED_TWO", "TYPE_TWO");
		teamOne.addProperty("NAME_ONE", "VALUE_ONE");
		this.recordReturn(extensionContext, extensionContext.getResource("example/team.properties"),
				new ByteArrayInputStream("file=value".getBytes()));
		teamOne.addProperty("file", "value");
		teamOne.addProperty("NAME_TWO", "VALUE_TWO");

		// Record link directly to Office team
		// Ensures application links to its specified team
		// (and not from another WoOF loading teams)
		OfficeTeam officeTeamOne = this.createMock(OfficeTeam.class);
		this.recordReturn(office, office.getDeployedOfficeTeam(teamOneName), officeTeamOne);
		deployer.link(officeTeamOne, teamOne);

		// Record second team
		final String teamTwoName = "QUALIFIED:net.example.Type";
		OfficeFloorTeam teamTwo = this.createMock(OfficeFloorTeam.class);
		this.recordReturn(deployer, deployer.addTeam(teamTwoName, "PASSIVE"), teamTwo);
		teamTwo.addTypeQualification("QUALIFIED", "net.example.Type");

		// Record link directly to Office team
		OfficeTeam officeTeamTwo = this.createMock(OfficeTeam.class);
		this.recordReturn(office, office.getDeployedOfficeTeam(teamTwoName), officeTeamTwo);
		deployer.link(officeTeamTwo, teamTwo);

		// Tests
		this.replayMockObjects();
		this.loader.loadWoofTeamsConfiguration(loaderContext);
		this.verifyMockObjects();
	}

	/**
	 * Ensure can load configuration to {@link OfficeArchitect} with the teams.
	 */
	public void testLoadingOfficeUsageOfTeams() throws Exception {

		// Create specific Office mocks
		final WoofTeamsUsageContext usageContext = this.createMock(WoofTeamsUsageContext.class);
		final OfficeArchitect architect = this.createMock(OfficeArchitect.class);

		// Obtain the configuration
		this.recordReturn(usageContext, usageContext.getOfficeArchitect(), architect);
		this.recordReturn(usageContext, usageContext.getConfiguration(), this.getConfiguration("Teams.teams.xml"));

		// Enable auto wire of teams
		architect.enableAutoWireTeams();

		// Record first team
		OfficeTeam teamOne = this.createMock(OfficeTeam.class);
		this.recordReturn(architect, architect.addOfficeTeam("QUALIFIED_ONE:TYPE_ONE"), teamOne);
		teamOne.addTypeQualification("QUALIFIED_ONE", "TYPE_ONE");
		teamOne.addTypeQualification("QUALIFIED_TWO", "TYPE_TWO");

		// Record second team
		OfficeTeam teamTwo = this.createMock(OfficeTeam.class);
		this.recordReturn(architect, architect.addOfficeTeam("QUALIFIED:net.example.Type"), teamTwo);
		teamTwo.addTypeQualification("QUALIFIED", "net.example.Type");

		// Test
		this.replayMockObjects();
		this.loader.loadWoofTeamsUsage(usageContext);
		this.verifyMockObjects();
	}

	/**
	 * Obtains the {@link ConfigurationItem}.
	 * 
	 * @param fileName File name for {@link ConfigurationItem}.
	 * @return {@link ConfigurationItem}.
	 */
	private ConfigurationItem getConfiguration(String fileName) throws Exception {
		String location = this.getFileLocation(this.getClass(), fileName);
		ConfigurationContext context = new ClassLoaderConfigurationContext(this.compiler.getClassLoader(), null);
		ConfigurationItem configuration = context.getConfigurationItem(location, null);
		assertNotNull("Can not find configuration '" + fileName + "'", configuration);
		return configuration;
	}

}
