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
package net.officefloor.model.teams;

import java.util.List;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.RemoveConnectionsAction;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.impl.repository.filesystem.FileSystemConfigurationItem;
import net.officefloor.model.impl.repository.memory.MemoryConfigurationItem;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.repository.ModelRepository;

/**
 * Tests the marshaling/unmarshaling of the {@link WoofTeamsModel} via the
 * {@link ModelRepository}.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWireTeamsModelRepositoryTest extends OfficeFrameTestCase {

	/**
	 * {@link ConfigurationItem} containing the {@link WoofTeamsModel}.
	 */
	private ConfigurationItem configurationItem;

	@Override
	protected void setUp() throws Exception {
		// Specify location of the configuration
		this.configurationItem = new FileSystemConfigurationItem(this.findFile(
				this.getClass(), "Teams.teams.xml"), null);
	}

	/**
	 * Ensure retrieve the {@link WoofTeamsModel}.
	 */
	public void testRetrieveTeams() throws Exception {

		// Load the Teams
		ModelRepository repository = new ModelRepositoryImpl();
		WoofTeamsModel teams = new WoofTeamsModel();
		teams = repository.retrieve(teams, this.configurationItem);

		// ----------------------------------------
		// Validate the teams
		// ----------------------------------------
		assertList(new String[] { "getTeamSourceClassName", "getQualifier",
				"getType" }, teams.getAutoWireTeams(), new WoofTeamModel(
				"net.example.ExampleTeamSource", null, null),
				new WoofTeamModel("PASSIVE", "QUALIFIED",
						"net.example.Type"));
		WoofTeamModel team = teams.getAutoWireTeams().get(0);

		// Validate the properties
		assertProperties(new PropertyModel("NAME_ONE", "VALUE_ONE"),
				new PropertyFileModel("example/team.properties"),
				new PropertyModel("NAME_TWO", "VALUE_TWO"),
				team.getPropertySources());

		// Validate the auto-wiring
		assertList(new String[] { "getQualifier", "getType" },
				team.getAutoWiring(), new TypeQualificationModel("QUALIFIED_ONE",
						"TYPE_ONE"), new TypeQualificationModel("QUALIFIED_TWO",
						"TYPE_TWO"));
	}

	/**
	 * Asserts the object is of the type.
	 * 
	 * @param type
	 *            Expected type.
	 * @param object
	 *            Object to validate.
	 * @return Object cast to type for convenience.
	 */
	@SuppressWarnings("unchecked")
	private static <T> T assertType(Class<T> type, Object object) {
		assertEquals("Incorrect object type", type, object.getClass());
		return (T) object;
	}

	/**
	 * Asserts the {@link PropertySourceModel}.
	 * 
	 * @param propertyOne
	 *            Expected {@link PropertyModel}.
	 * @param propertyFile
	 *            Expected {@link PropertyFileModel}.
	 * @param propertyTwo
	 *            Expected {@link PropertyModel}.
	 * @param actual
	 *            Actual {@link PropertySourceModel} instances.
	 */
	private static void assertProperties(PropertyModel propertyOne,
			PropertyFileModel propertyFile, PropertyModel propertyTwo,
			List<PropertySourceModel> actual) {
		assertEquals("Incorrect number of property sources", 3, actual.size());
		assertProperties(propertyOne,
				assertType(PropertyModel.class, actual.get(0)), "getName",
				"getValue");
		assertProperties(propertyFile,
				assertType(PropertyFileModel.class, actual.get(1)), "getPath");
		assertProperties(propertyTwo,
				assertType(PropertyModel.class, actual.get(2)), "getName",
				"getValue");
	}

	/**
	 * Ensure able to round trip storing and retrieving the
	 * {@link WoofTeamsModel}.
	 */
	public void testRoundTripStoreRetrieveTeams() throws Exception {

		// Load the teams
		ModelRepository repository = new ModelRepositoryImpl();
		WoofTeamsModel teams = new WoofTeamsModel();
		teams = repository.retrieve(teams, this.configurationItem);

		// Store the teams
		MemoryConfigurationItem contents = new MemoryConfigurationItem();
		repository.store(teams, contents);

		// Reload the teams
		WoofTeamsModel reloadedTeams = new WoofTeamsModel();
		reloadedTeams = repository.retrieve(reloadedTeams, contents);

		// Validate round trip
		assertGraph(teams, reloadedTeams,
				RemoveConnectionsAction.REMOVE_CONNECTIONS_METHOD_NAME);
	}

}