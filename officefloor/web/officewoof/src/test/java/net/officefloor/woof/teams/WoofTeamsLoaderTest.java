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
package net.officefloor.woof.teams;

import java.io.ByteArrayInputStream;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionContext;
import net.officefloor.configuration.ConfigurationContext;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.impl.classloader.ClassLoaderConfigurationContext;
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
	 * {@link WoofTeamsLoaderContext}.
	 */
	private final WoofTeamsLoaderContext loaderContext = this.createMock(WoofTeamsLoaderContext.class);

	/**
	 * Mock {@link OfficeFloorDeployer}.
	 */
	private final OfficeFloorDeployer deployer = this.createMock(OfficeFloorDeployer.class);

	/**
	 * Mock {@link OfficeFloorExtensionContext}.
	 */
	private final OfficeFloorExtensionContext extensionContext = this.createMock(OfficeFloorExtensionContext.class);

	/**
	 * Ensure can load configuration to {@link OfficeFloorDeployer} with the
	 * teams.
	 */
	public void testLoading() throws Exception {

		// Obtain the configuration
		this.recordReturn(this.loaderContext, this.loaderContext.getConfiguration(),
				this.getConfiguration("Teams.teams.xml"));
		this.recordReturn(this.loaderContext, this.loaderContext.getOfficeFloorDeployer(), this.deployer);
		this.recordReturn(this.loaderContext, this.loaderContext.getOfficeFloorExtensionContext(),
				this.extensionContext);

		// Record first team
		OfficeFloorTeam teamOne = this.createMock(OfficeFloorTeam.class);
		this.recordReturn(this.deployer,
				this.deployer.addTeam("QUALIFIED_ONE:TYPE_ONE", "net.example.ExampleTeamSource"), teamOne);
		teamOne.addTypeQualification("QUALIFIED_ONE", "TYPE_ONE");
		teamOne.addTypeQualification("QUALIFIED_TWO", "TYPE_TWO");
		teamOne.addProperty("NAME_ONE", "VALUE_ONE");
		this.recordReturn(this.extensionContext, this.extensionContext.getResource("example/team.properties"),
				new ByteArrayInputStream("file=value".getBytes()));
		teamOne.addProperty("file", "value");
		teamOne.addProperty("NAME_TWO", "VALUE_TWO");

		// Record second team
		OfficeFloorTeam teamTwo = this.createMock(OfficeFloorTeam.class);
		this.recordReturn(this.deployer, this.deployer.addTeam("QUALIFIED:net.example.Type", "PASSIVE"), teamTwo);
		teamTwo.addTypeQualification("QUALIFIED", "net.example.Type");

		// Test
		this.replayMockObjects();
		this.loader.loadWoofTeamsConfiguration(this.loaderContext);
		this.verifyMockObjects();
	}

	/**
	 * Obtains the {@link ConfigurationItem}.
	 * 
	 * @param fileName
	 *            File name for {@link ConfigurationItem}.
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