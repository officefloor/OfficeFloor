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
