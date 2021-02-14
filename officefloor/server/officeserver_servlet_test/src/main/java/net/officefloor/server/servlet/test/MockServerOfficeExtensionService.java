/*-
 * #%L
 * Provides testing using HttpServlet
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
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

package net.officefloor.server.servlet.test;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.extension.OfficeExtensionContext;
import net.officefloor.compile.spi.office.extension.OfficeExtensionService;
import net.officefloor.compile.spi.office.extension.OfficeExtensionServiceFactory;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.server.http.HttpServer;

/**
 * {@link OfficeExtensionService} to mock {@link HttpServer} setup.
 * 
 * @author Daniel Sagenschneider
 */
public class MockServerOfficeExtensionService implements OfficeExtensionService, OfficeExtensionServiceFactory {

	/*
	 * ========================== OfficeExtensionService ========================
	 */

	@Override
	public OfficeExtensionService createService(ServiceContext context) throws Throwable {
		return this;
	}

	@Override
	public void extendOffice(OfficeArchitect officeArchitect, OfficeExtensionContext context) throws Exception {
		if (MockServerSettings.officeExtensionService != null) {
			MockServerSettings.officeExtensionService.extendOffice(officeArchitect, context);
		}
	}

}
