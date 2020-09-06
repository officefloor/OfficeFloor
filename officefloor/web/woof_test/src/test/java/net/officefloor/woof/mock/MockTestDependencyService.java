package net.officefloor.woof.mock;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import net.officefloor.frame.api.manage.UnknownObjectException;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.test.TestDependencyService;
import net.officefloor.test.TestDependencyServiceContext;
import net.officefloor.test.TestDependencyServiceFactory;

/**
 * Mock {@link TestDependencyService}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockTestDependencyService
		implements TestDependencyService, TestDependencyServiceFactory, BeforeAllCallback, AfterAllCallback, TestRule {

	/**
	 * Dependency.
	 */
	private final Object testDependency;

	/**
	 * Instantiate.
	 * 
	 * @param testDependency Test specific dependency.
	 */
	public MockTestDependencyService(Object testDependency) {
		this.testDependency = testDependency;
	}

	/**
	 * Instantiate for {@link Extension}.
	 */
	public MockTestDependencyService() {
		this.testDependency = null;
	}

	/*
	 * ===================== TestDependencyServiceFactory ====================
	 */

	@Override
	public TestDependencyService createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ======================== Extension =====================================
	 */

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {
		dependency = this.testDependency;
	}

	@Override
	public void afterAll(ExtensionContext context) throws Exception {
		dependency = null;
	}

	/*
	 * ============================= TestRule =================================
	 */

	@Override
	public Statement apply(Statement base, Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				try {
					MockTestDependencyService.this.beforeAll(null);
					base.evaluate();
				} finally {
					MockTestDependencyService.this.afterAll(null);
				}
			}
		};
	}

	/*
	 * ======================== TestDependencyService =========================
	 */

	private static Object dependency = null;

	@Override
	public boolean isObjectAvailable(TestDependencyServiceContext context) {
		return (dependency != null) && (context.getObjectType().isAssignableFrom(dependency.getClass()));
	}

	@Override
	public Object getObject(TestDependencyServiceContext context) throws UnknownObjectException, Throwable {
		return (this.isObjectAvailable(context)) ? dependency : null;
	}

}