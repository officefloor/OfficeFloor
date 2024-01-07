package net.officefloor.woof.mock;

import net.officefloor.frame.api.manage.UnknownObjectException;
import net.officefloor.test.TestDependencyService;
import net.officefloor.test.TestDependencyServiceContext;

/**
 * {@link TestDependencyService} for testing adding.
 */
public class MockAddTestDependencyService implements TestDependencyService {

	/*
	 * ==================== TestDependencyService ====================
	 */

	@Override
	public boolean isObjectAvailable(TestDependencyServiceContext context) {
		return this.getClass().isAssignableFrom(context.getObjectType());
	}

	@Override
	public Object getObject(TestDependencyServiceContext context) throws UnknownObjectException, Throwable {
		return this;
	}

}
