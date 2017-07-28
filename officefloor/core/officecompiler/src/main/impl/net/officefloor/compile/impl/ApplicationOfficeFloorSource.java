/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.compile.impl;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.extension.OfficeExtensionService;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.office.source.impl.AbstractOfficeSource;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.compile.spi.officefloor.source.RequiredProperties;
import net.officefloor.compile.spi.officefloor.source.impl.AbstractOfficeFloorSource;
import net.officefloor.frame.api.manage.Office;

/**
 * <p>
 * {@link OfficeFloorSource} to create a single empty {@link Office}.
 * <p>
 * It is expected that functionality will be loaded via
 * {@link OfficeExtensionService} instances.
 *
 * @author Daniel Sagenschneider
 */
public class ApplicationOfficeFloorSource extends AbstractOfficeFloorSource {

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// no properties required
	}

	@Override
	public void specifyConfigurationProperties(RequiredProperties requiredProperties, OfficeFloorSourceContext context)
			throws Exception {
		// no additional properties required
	}

	@Override
	public void sourceOfficeFloor(OfficeFloorDeployer deployer, OfficeFloorSourceContext context) throws Exception {
		// Add the single office
		deployer.addDeployedOffice("OFFICE", new ApplicationOfficeSource(), null);
	}

	/**
	 * {@link OfficeSource} to create an empty {@link Office}.
	 */
	private static class ApplicationOfficeSource extends AbstractOfficeSource {

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