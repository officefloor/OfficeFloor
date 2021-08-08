/*-
 * #%L
 * H2 Test
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

package net.officefloor.jdbc.h2.test;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;

import net.officefloor.compile.state.autowire.AutoWireStateManager;
import net.officefloor.frame.api.manage.UnknownObjectException;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.test.TestDependencyService;
import net.officefloor.test.TestDependencyServiceContext;
import net.officefloor.test.TestDependencyServiceFactory;

/**
 * {@link TestDependencyService} providing the {@link H2Reset}.
 * 
 * @author Daniel Sagenschneider
 */
public class H2ResetTestDependencyService implements TestDependencyService, TestDependencyServiceFactory {

	/*
	 * ====================== TestDependencyServiceFactory =======================
	 */

	@Override
	public TestDependencyService createService(ServiceContext context) throws Throwable {
		return this;
	}

	@Override
	public boolean isObjectAvailable(TestDependencyServiceContext context) {
		return (context.getQualifier() == null) && (H2Reset.class.isAssignableFrom(context.getObjectType()));
	}

	@Override
	public Object getObject(TestDependencyServiceContext context) throws UnknownObjectException, Throwable {

		// Ensure supports the required dependency
		if (!this.isObjectAvailable(context)) {
			throw new UnknownObjectException(
					H2Reset.class.getSimpleName() + " can not be used for " + context.getObjectType().getName());
		}

		// Obtain the dependencies
		AutoWireStateManager stateManager = context.getStateManager();
		long loadTimeout = context.getLoadTimeout();
		DataSource dataSource = stateManager.getObject(null, DataSource.class, loadTimeout);
		Flyway flyway = stateManager.isObjectAvailable(null, Flyway.class)
				? stateManager.getObject(null, Flyway.class, loadTimeout)
				: null;

		// Return the H2 reset
		return new H2Reset(dataSource, flyway);
	}

}
