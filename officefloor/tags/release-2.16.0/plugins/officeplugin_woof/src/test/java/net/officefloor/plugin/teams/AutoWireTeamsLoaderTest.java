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
package net.officefloor.plugin.teams;

import org.easymock.AbstractMatcher;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireApplication;
import net.officefloor.autowire.AutoWireTeam;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.impl.repository.classloader.ClassLoaderConfigurationContext;
import net.officefloor.model.repository.ConfigurationContext;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.teams.AutoWireTeamsRepositoryImpl;

/**
 * Tests the {@link AutoWireTeamsLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWireTeamsLoaderTest extends OfficeFrameTestCase {

	/**
	 * {@link OfficeFloorCompiler}.
	 */
	private final OfficeFloorCompiler compiler = OfficeFloorCompiler
			.newOfficeFloorCompiler(null);

	/**
	 * {@link AutoWireTeamsLoader} to test.
	 */
	private final AutoWireTeamsLoader loader = new AutoWireTeamsLoaderImpl(
			new AutoWireTeamsRepositoryImpl(new ModelRepositoryImpl()));

	/**
	 * Mock {@link AutoWireApplication}.
	 */
	private final AutoWireApplication app = this
			.createMock(AutoWireApplication.class);

	/**
	 * Ensure can load configuration to {@link AutoWireApplication}.
	 */
	public void testLoading() throws Exception {

		// Record first team
		final AutoWireTeam teamOne = this.createMock(AutoWireTeam.class);
		this.recordReturn(this.app,
				this.app.assignTeam("net.example.ExampleTeamSource",
						new AutoWire("QUALIFIED_ONE", "TYPE_ONE"),
						new AutoWire("QUALIFIED_TWO", "TYPE_TWO")), teamOne,
				new AbstractMatcher() {
					@Override
					public boolean matches(Object[] expected, Object[] actual) {
						boolean isMatch = expected[0].equals(actual[0]);
						AutoWire[] eAutoWiring = (AutoWire[]) expected[1];
						AutoWire[] aAutoWiring = (AutoWire[]) actual[1];
						isMatch = isMatch
								&& (eAutoWiring.length == aAutoWiring.length);
						if (isMatch) {
							for (int i = 0; i < eAutoWiring.length; i++) {
								isMatch = isMatch
										&& (eAutoWiring[i]
												.equals(aAutoWiring[i]));
							}
						}
						return isMatch;
					}
				});
		teamOne.addProperty("NAME_ONE", "VALUE_ONE");
		teamOne.loadProperties("example/team.properties");
		teamOne.addProperty("NAME_TWO", "VALUE_TWO");

		// Record second team
		final AutoWireTeam teamTwo = this.createMock(AutoWireTeam.class);
		this.recordReturn(this.app, this.app.assignTeam("PASSIVE",
				new AutoWire("QUALIFIED", "net.example.Type")), teamTwo);

		// Test
		this.replayMockObjects();
		this.loader.loadAutoWireTeamsConfiguration(
				this.getConfiguration("application.teams"), this.app);
		this.verifyMockObjects();
	}

	/**
	 * Obtains the {@link ConfigurationItem}.
	 * 
	 * @param fileName
	 *            File name for {@link ConfigurationItem}.
	 * @return {@link ConfigurationItem}.
	 */
	private ConfigurationItem getConfiguration(String fileName)
			throws Exception {
		String location = this.getFileLocation(this.getClass(), fileName);
		ConfigurationContext context = new ClassLoaderConfigurationContext(
				this.compiler.getClassLoader());
		ConfigurationItem configuration = context
				.getConfigurationItem(location);
		assertNotNull("Can not find configuration '" + fileName + "'",
				configuration);
		return configuration;
	}

}