/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.impl.execute.managedobject.logger;

import java.util.logging.Logger;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;

/**
 * Ensure the {@link Logger} is available from the
 * {@link ManagedObjectExecuteContext}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectExecuteLoggerTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure appropriate {@link Logger} provided.
	 */
	public void testLogger() throws Exception {

		// Access the loggers
		final String MOS_NAME = "MOS_LOGGER";
		LoggerManagedObjectSource mos = new LoggerManagedObjectSource();

		// Construct the managed object
		this.constructManagedObject(MOS_NAME, mos, this.getOfficeName());

		// Ensure appropriate loggers
		try (OfficeFloor officeFloor = this.constructOfficeFloor()) {
			officeFloor.openOfficeFloor();

			// Ensure correct source logger
			assertEquals("Incorrect source logger", "of-" + MOS_NAME, mos.sourceLogger.getName());
			assertSame("Should be same for execute logger", mos.sourceLogger, mos.executelogger);
		}
	}

	/**
	 * Provides the {@link Logger}.
	 */
	public static class LoggerManagedObjectSource extends AbstractManagedObjectSource<None, None>
			implements ManagedObject {

		private Logger sourceLogger;

		private Logger executelogger;

		/*
		 * ====================== ManagedObjectSource =================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// Nothing required
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
			this.sourceLogger = context.getManagedObjectSourceContext().getLogger();
			context.setObjectClass(Logger.class);
		}

		@Override
		public void start(ManagedObjectExecuteContext<None> context) throws Exception {
			this.executelogger = context.getLogger();
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			return this;
		}

		/*
		 * ======================= ManagedObject ========================
		 */

		@Override
		public Object getObject() throws Throwable {
			return this.executelogger;
		}
	}

}
