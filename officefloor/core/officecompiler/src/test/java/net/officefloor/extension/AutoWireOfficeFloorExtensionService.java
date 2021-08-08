/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.extension;

import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionContext;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionService;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionServiceFactory;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.source.ServiceContext;

/**
 * Provides means to auto-wire aspects of the {@link OfficeFloor} within
 * testing.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWireOfficeFloorExtensionService
		implements OfficeFloorExtensionService, OfficeFloorExtensionServiceFactory {

	/**
	 * Indicates whether to auto-wire the objects.
	 */
	private static boolean isEnableAutoWireObjects = false;

	/**
	 * Indicates whether to auto-wire the teams.
	 */
	private static boolean isEnableAutoWireTeams = false;

	/**
	 * Resets the state for next test.
	 */
	public static void reset() {
		isEnableAutoWireObjects = false;
		isEnableAutoWireTeams = false;
	}

	/**
	 * Enable auto-wire of the objects.
	 */
	public static void enableAutoWireObjects() {
		isEnableAutoWireObjects = true;
	}

	/**
	 * Enable auto-wire of the teams.
	 */
	public static void enableAutoWireTeams() {
		isEnableAutoWireTeams = true;
	}

	/*
	 * =================== OfficeFloorExtensionService ===================
	 */

	@Override
	public OfficeFloorExtensionService createService(ServiceContext context) throws Throwable {
		return this;
	}

	@Override
	public void extendOfficeFloor(OfficeFloorDeployer officeFloorDeployer, OfficeFloorExtensionContext context)
			throws Exception {

		// Enable auto-wiring as appropriate
		if (isEnableAutoWireObjects) {
			officeFloorDeployer.enableAutoWireObjects();
		}
		if (isEnableAutoWireTeams) {
			officeFloorDeployer.enableAutoWireTeams();
		}
	}

}
