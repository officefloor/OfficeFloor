/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.impl.execute.administration;

import java.util.logging.Logger;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.AdministrationContext;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Ensure able to obtain {@link Logger} for {@link Administration}.
 * 
 * @author Daniel Sagenschneider
 */
public class AdministrationLoggerTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure appropriate {@link Logger}.
	 */
	public void testLogger() throws Exception {

		// Capture office name
		final String officeName = this.getOfficeName();

		// Create the managed function with pre and post administration
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder function = this.constructFunction(work, "function");
		function.buildManagedFunctionContext();
		function.preAdminister("preAdminister").buildAdministrationContext();
		function.postAdminister("postAdminister").buildAdministrationContext();

		// Ensure provide appropriate logger
		try (OfficeFloor officeFloor = this.constructOfficeFloor()) {
			officeFloor.openOfficeFloor();

			// Invoke function and confirm correct logger
			officeFloor.getOffice(officeName).getFunctionManager("function").invokeProcess(null, null);

			// Ensure correct loggers
			assertEquals("Incorrect pre-admin logger", "function.pre.preAdminister",
					work.preAdministerLogger.getName());
			assertEquals("Incorrect function logger", work.functionLogger.getName(), "function");
			assertEquals("Incorrect post-admin logger", "function.post.postAdminister",
					work.postAdministerLogger.getName());
		}
	}

	public class TestWork {

		private Logger preAdministerLogger;

		private Logger functionLogger;

		private Logger postAdministerLogger;

		public void preAdminister(Object[] extensions, AdministrationContext<Object, None, None> context) {
			this.preAdministerLogger = context.getLogger();
		}

		public void function(ManagedFunctionContext<None, None> context) {
			this.functionLogger = context.getLogger();
		}

		public void postAdminister(Object[] extensions, AdministrationContext<Object, None, None> context) {
			this.postAdministerLogger = context.getLogger();
		}
	}

}
