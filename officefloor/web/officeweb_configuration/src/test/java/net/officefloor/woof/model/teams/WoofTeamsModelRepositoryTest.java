/*-
 * #%L
 * Web configuration
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.woof.model.teams;

import java.util.List;

import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.configuration.impl.configuration.FileSystemConfigurationContext;
import net.officefloor.configuration.impl.configuration.MemoryConfigurationContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.RemoveConnectionsAction;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.repository.ModelRepository;

/**
 * Tests the marshaling/unmarshaling of the {@link WoofTeamsModel} via the
 * {@link ModelRepository}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofTeamsModelRepositoryTest extends OfficeFrameTestCase {

	/**
	 * {@link ConfigurationItem} containing the {@link WoofTeamsModel}.
	 */
	private ConfigurationItem configurationItem;

	@Override
	protected void setUp() throws Exception {
		// Specify location of the configuration
		this.configurationItem = FileSystemConfigurationContext
				.createWritableConfigurationItem(this.findFile(this.getClass(), "Teams.teams.xml"));
	}

	/**
	 * Ensure retrieve the {@link WoofTeamsModel}.
	 */
	public void testRetrieveTeams() throws Exception {

		// Load the Teams
		ModelRepository repository = new ModelRepositoryImpl();
		WoofTeamsModel teams = new WoofTeamsModel();
		repository.retrieve(teams, this.configurationItem);

		// ----------------------------------------
		// Validate the teams
		// ----------------------------------------
		assertList(new String[] { "getTeamSize", "getTeamSourceClassName", "getQualifier", "getType" },
				teams.getWoofTeams(), new WoofTeamModel(50, "net.example.ExampleTeamSource", null, null),
				new WoofTeamModel(0, "PASSIVE", "QUALIFIED", "net.example.Type"));
		WoofTeamModel team = teams.getWoofTeams().get(0);

		// Validate the properties
		assertProperties(new PropertyModel("NAME_ONE", "VALUE_ONE"), new PropertyFileModel("example/team.properties"),
				new PropertyModel("NAME_TWO", "VALUE_TWO"), team.getPropertySources());

		// Validate the auto-wiring
		assertList(new String[] { "getQualifier", "getType" }, team.getTypeQualifications(),
				new TypeQualificationModel("QUALIFIED_ONE", "TYPE_ONE"),
				new TypeQualificationModel("QUALIFIED_TWO", "TYPE_TWO"));
	}

	/**
	 * Asserts the object is of the type.
	 * 
	 * @param type   Expected type.
	 * @param object Object to validate.
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
	 * @param propertyOne  Expected {@link PropertyModel}.
	 * @param propertyFile Expected {@link PropertyFileModel}.
	 * @param propertyTwo  Expected {@link PropertyModel}.
	 * @param actual       Actual {@link PropertySourceModel} instances.
	 */
	private static void assertProperties(PropertyModel propertyOne, PropertyFileModel propertyFile,
			PropertyModel propertyTwo, List<PropertySourceModel> actual) {
		assertEquals("Incorrect number of property sources", 3, actual.size());
		assertProperties(propertyOne, assertType(PropertyModel.class, actual.get(0)), "getName", "getValue");
		assertProperties(propertyFile, assertType(PropertyFileModel.class, actual.get(1)), "getPath");
		assertProperties(propertyTwo, assertType(PropertyModel.class, actual.get(2)), "getName", "getValue");
	}

	/**
	 * Ensure able to round trip storing and retrieving the {@link WoofTeamsModel}.
	 */
	public void testRoundTripStoreRetrieveTeams() throws Exception {

		// Load the teams
		ModelRepository repository = new ModelRepositoryImpl();
		WoofTeamsModel teams = new WoofTeamsModel();
		repository.retrieve(teams, this.configurationItem);

		// Store the teams
		WritableConfigurationItem contents = MemoryConfigurationContext.createWritableConfigurationItem("test");
		repository.store(teams, contents);

		// Reload the teams
		WoofTeamsModel reloadedTeams = new WoofTeamsModel();
		repository.retrieve(reloadedTeams, contents);

		// Validate round trip
		assertGraph(teams, reloadedTeams, RemoveConnectionsAction.REMOVE_CONNECTIONS_METHOD_NAME);
	}

}
