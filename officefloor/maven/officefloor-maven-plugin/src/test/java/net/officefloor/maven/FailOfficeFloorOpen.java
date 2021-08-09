/*-
 * #%L
 * Maven OfficeFloor Plugin
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

package net.officefloor.maven;

import net.officefloor.compile.OfficeFloorCompilerConfigurer;
import net.officefloor.compile.OfficeFloorCompilerConfigurerContext;
import net.officefloor.compile.OfficeFloorCompilerConfigurerServiceFactory;
import net.officefloor.frame.api.build.OfficeFloorEvent;
import net.officefloor.frame.api.build.OfficeFloorListener;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.source.ServiceContext;

/**
 * Fails {@link OfficeFloor} open.
 * 
 * @author Daniel Sagenschneider
 */
public class FailOfficeFloorOpen implements OfficeFloorCompilerConfigurer, OfficeFloorCompilerConfigurerServiceFactory {

	/**
	 * Message of failure to open {@link OfficeFloor}.
	 */
	public static final String FAIL_OPEN_MESSAGE = "TEST failed to open OfficeFloor";

	/*
	 * ================ OfficeFloorCompilerConfigurerServiceFactory ================
	 */

	@Override
	public OfficeFloorCompilerConfigurer createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ======================= OfficeFloorCompilerConfigurer =======================
	 */

	@Override
	public void configureOfficeFloorCompiler(OfficeFloorCompilerConfigurerContext context) throws Exception {

		// Determine if fail open
		boolean isFailOpen = Boolean.parseBoolean(System.getProperty("test.fail.officefloor.open", "false"));
		if (isFailOpen) {
			context.getOfficeFloorCompiler().addOfficeFloorListener(new OfficeFloorListener() {

				@Override
				public void officeFloorOpened(OfficeFloorEvent event) throws Exception {
					throw new Exception(FAIL_OPEN_MESSAGE);
				}

				@Override
				public void officeFloorClosed(OfficeFloorEvent event) throws Exception {
					System.out.println("Closing");
				}
			});
		}
	}

}
