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