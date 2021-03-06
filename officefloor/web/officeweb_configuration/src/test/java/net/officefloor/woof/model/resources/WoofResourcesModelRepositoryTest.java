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

package net.officefloor.woof.model.resources;

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
 * Tests the marshaling/unmarshaling of the {@link WoofResourceModel} via the
 * {@link ModelRepository}.
 *
 * @author Daniel Sagenschneider
 */
public class WoofResourcesModelRepositoryTest extends OfficeFrameTestCase {

	/**
	 * {@link ConfigurationItem} containing the {@link WoofResourcesModel}.
	 */
	private ConfigurationItem configurationItem;

	@Override
	protected void setUp() throws Exception {
		// Specify location of the configuration
		this.configurationItem = FileSystemConfigurationContext
				.createWritableConfigurationItem(this.findFile(this.getClass(), "Resources.resources.xml"));
	}

	/**
	 * Ensure retrieve the {@link WoofResourcesModel}.
	 */
	public void testRetrieveResources() throws Exception {

		// Load the Resources
		ModelRepository repository = new ModelRepositoryImpl();
		WoofResourcesModel resourcesModel = new WoofResourcesModel();
		repository.retrieve(resourcesModel, this.configurationItem);

		// ----------------------------------------
		// Validate the resources
		// ----------------------------------------
		List<WoofResourceModel> resources = resourcesModel.getWoofResources();
		assertList(new String[] { "getProtocol", "getLocation", "getContextPath" }, resources,
				new WoofResourceModel("file", "/location", "context", null, null, null),
				new WoofResourceModel("classpath", "PUBLIC", null, null, null, null));

		// Validate the first resources
		WoofResourceModel resourceOne = resources.get(0);
		WoofResourceSecurityModel security = resourceOne.getSecurity();
		assertProperties(security, new WoofResourceSecurityModel("security", null, null), "getHttpSecurityName");
		assertList(security.getRoles(), "RoleOne", "RoleTwo");
		assertList(security.getRequiredRoles(), "RequiredOne", "RequiredTwo");
		assertList(new String[] { "getQualifier" }, resourceOne.getTypeQualifications(),
				new TypeQualificationModel("QUALIFIED"), new TypeQualificationModel("ANOTHER"));
		assertList(new String[] { "getName" }, resourceOne.getWoofResourceTransformers(),
				new WoofResourceTransformerModel("zip"), new WoofResourceTransformerModel("another"));

		// Ensure second has no configuration
		WoofResourceModel resourceTwo = resources.get(1);
		assertNull("Should not have security", resourceTwo.getSecurity());
		assertEquals("Should be no auto-wiring for second resource", 0, resourceTwo.getTypeQualifications().size());
		assertEquals("Should be no transforming for second resource", 0,
				resourceTwo.getWoofResourceTransformers().size());
	}

	/**
	 * Ensure able to round trip storing and retrieving the
	 * {@link WoofResourcesModel}.
	 */
	public void testRoundTripStoreRetrieveResources() throws Exception {

		// Load the objects
		ModelRepository repository = new ModelRepositoryImpl();
		WoofResourcesModel resources = new WoofResourcesModel();
		repository.retrieve(resources, this.configurationItem);

		// Store the resources
		WritableConfigurationItem contents = MemoryConfigurationContext.createWritableConfigurationItem("test");
		repository.store(resources, contents);

		// Reload the resources
		WoofResourcesModel reloadedResources = new WoofResourcesModel();
		repository.retrieve(reloadedResources, contents);

		// Validate round trip
		assertGraph(resources, reloadedResources, RemoveConnectionsAction.REMOVE_CONNECTIONS_METHOD_NAME);
	}

}
