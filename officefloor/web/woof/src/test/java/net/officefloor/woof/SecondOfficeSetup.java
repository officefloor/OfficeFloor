/*-
 * #%L
 * Web on OfficeFloor
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
