/*-
 * #%L
 * Web on OfficeFloor
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

package net.officefloor.woof;

import net.officefloor.compile.OfficeFloorCompilerConfigurer;
import net.officefloor.compile.OfficeFloorCompilerConfigurerContext;
import net.officefloor.compile.OfficeFloorCompilerConfigurerServiceFactory;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.office.source.impl.AbstractOfficeSource;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.compile.spi.officefloor.source.RequiredProperties;
import net.officefloor.compile.spi.officefloor.source.impl.AbstractOfficeFloorSource;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.frame.api.source.TestSource;

/**
 * Sets up alternate {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
@TestSource
public class SecondOfficeSetup extends AbstractOfficeFloorSource
		implements OfficeFloorCompilerConfigurer, OfficeFloorCompilerConfigurerServiceFactory {

	/**
	 * Indicates whether to configure second {@link Office}.
	 */
	public static boolean isConfigureSecond = false;

	/*
	 * ============== OfficeFloorCompilerConfigurationServiceFactory ==============
	 */

	@Override
	public OfficeFloorCompilerConfigurer createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ================= OfficeFloorCompilerConfigurationService ==================
	 */

	@Override
	public void configureOfficeFloorCompiler(OfficeFloorCompilerConfigurerContext context) throws Exception {
		if (isConfigureSecond) {
			context.getOfficeFloorCompiler().setOfficeFloorSource(this);
		}
	}

	/*
	 * ============================ OfficeFloorSource =============================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// no specification
	}

	@Override
	public void specifyConfigurationProperties(RequiredProperties requiredProperties, OfficeFloorSourceContext context)
			throws Exception {
		// no configuration properties
	}

	@Override
	public void sourceOfficeFloor(OfficeFloorDeployer deployer, OfficeFloorSourceContext context) throws Exception {
		deployer.addDeployedOffice("second", new SecondOfficeSource(), null);
	}

	/**
	 * {@link OfficeSource} to create an empty {@link Office}.
	 */
	@TestSource
	private static class SecondOfficeSource extends AbstractOfficeSource {

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// no properties required
		}

		@Override
		public void sourceOffice(OfficeArchitect officeArchitect, OfficeSourceContext context) throws Exception {
			// empty office
		}
	}

}
