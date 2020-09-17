/*-
 * #%L
 * Maven OfficeFloor Plugin
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
