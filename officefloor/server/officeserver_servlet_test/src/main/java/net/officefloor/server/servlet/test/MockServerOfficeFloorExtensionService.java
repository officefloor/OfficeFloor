/*-
 * #%L
 * Provides testing using HttpServlet
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
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

package net.officefloor.server.servlet.test;

import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionContext;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionService;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionServiceFactory;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.server.http.HttpServer;

/**
 * {@link OfficeFloorExtensionService} to mock {@link HttpServer} setup.
 * 
 * @author Daniel Sagenschneider
 */
public class MockServerOfficeFloorExtensionService
		implements OfficeFloorExtensionService, OfficeFloorExtensionServiceFactory {

	/*
	 * ======================= OfficeFloorExtensionService =======================
	 */

	@Override
	public OfficeFloorExtensionService createService(ServiceContext context) throws Throwable {
		return this;
	}

	@Override
	public void extendOfficeFloor(OfficeFloorDeployer officeFloorDeployer, OfficeFloorExtensionContext context)
			throws Exception {
		if (MockServerSettings.officeFloorExtensionService != null) {
			MockServerSettings.officeFloorExtensionService.extendOfficeFloor(officeFloorDeployer, context);
		}
	}

}
