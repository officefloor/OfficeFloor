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

package net.officefloor.test;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import net.officefloor.frame.api.manage.UnknownObjectException;
import net.officefloor.frame.api.source.ServiceContext;

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
