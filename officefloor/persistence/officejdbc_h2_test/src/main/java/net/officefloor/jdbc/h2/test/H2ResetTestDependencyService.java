/*-
 * #%L
 * H2 Test
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
