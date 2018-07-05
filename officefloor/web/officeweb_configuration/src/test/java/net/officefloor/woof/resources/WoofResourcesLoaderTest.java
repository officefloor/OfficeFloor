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
package net.officefloor.woof.resources;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.configuration.ConfigurationContext;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.impl.classloader.ClassLoaderConfigurationContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.web.resource.build.HttpResourceArchitect;
import net.officefloor.web.resource.build.HttpResourcesBuilder;
import net.officefloor.web.security.build.HttpSecurableBuilder;
import net.officefloor.woof.model.resources.WoofResourcesRepositoryImpl;

/**
 * Tests the {@link WoofResourcesLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofResourcesLoaderTest extends OfficeFrameTestCase {

	/**
	 * {@link OfficeFloorCompiler}.
	 */
	private final OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);

	/**
	 * {@link WoofResourcesLoader} to test.
	 */
	private final WoofResourcesLoader loader = new WoofResourcesLoaderImpl(
			new WoofResourcesRepositoryImpl(new ModelRepositoryImpl()));

	/**
	 * {@link WoofResourcesLoaderContext}.
	 */
	private final WoofResourcesLoaderContext loaderContext = this.createMock(WoofResourcesLoaderContext.class);

	/**
	 * {@link HttpResourceArchitect}.
	 */
	private final HttpResourceArchitect resourceArchitect = this.createMock(HttpResourceArchitect.class);

	/**
	 * Ensure can load configuration to {@link OfficeFloorDeployer} with the
	 * teams.
	 */
	public void testLoading() throws Exception {

		// Obtain the configuration
		this.recordReturn(this.loaderContext, this.loaderContext.getConfiguration(),
				this.getConfiguration("Resources.resources.xml"));
		this.recordReturn(this.loaderContext, this.loaderContext.getHttpResourceArchitect(), this.resourceArchitect);

		// Record first resource
		HttpResourcesBuilder resourceOne = this.createMock(HttpResourcesBuilder.class);
		this.recordReturn(this.resourceArchitect, this.resourceArchitect.addHttpResources("file:/location"),
				resourceOne);
		resourceOne.setContextPath("context");
		resourceOne.addTypeQualifier("QUALIFIED");
		resourceOne.addTypeQualifier("ANOTHER");
		resourceOne.addResourceTransformer("zip");
		resourceOne.addResourceTransformer("another");
		HttpSecurableBuilder securer = this.createMock(HttpSecurableBuilder.class);
		this.recordReturn(resourceOne, resourceOne.getHttpSecurer(), securer);
		securer.setHttpSecurityName("security");
		securer.addRole("RoleOne");
		securer.addRole("RoleTwo");
		securer.addRequiredRole("RequiredOne");
		securer.addRequiredRole("RequiredTwo");

		// Record second resource
		HttpResourcesBuilder resourceTwo = this.createMock(HttpResourcesBuilder.class);
		this.recordReturn(this.resourceArchitect, this.resourceArchitect.addHttpResources("classpath:PUBLIC"),
				resourceTwo);

		// Test
		this.replayMockObjects();
		this.loader.loadWoofResourcesConfiguration(this.loaderContext);
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