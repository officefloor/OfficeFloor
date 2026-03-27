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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.model.Statement;

import net.officefloor.compile.impl.ApplicationOfficeFloorSource;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.extension.OfficeExtensionContext;
import net.officefloor.compile.spi.office.extension.OfficeExtensionService;
import net.officefloor.compile.spi.office.extension.OfficeExtensionServiceFactory;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.frame.test.Closure;
import net.officefloor.plugin.clazz.Dependency;
import net.officefloor.plugin.managedobject.singleton.Singleton;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.Parameter;

/**
 * Tests the {@link OfficeFloorRule}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorRuleTest implements OfficeExtensionService, OfficeExtensionServiceFactory {

	/**
	 * Reset for test setup.
	 */
	@BeforeClass
	public static void resetTest() {
		isLoadOffice = true;
		new OfficeFloorRuleTest().cleanUpTest();
	}

	/**
	 * Reset for the next test.
	 */
	@After
	public void cleanUpTest() {
		failure = null;
		MockSection.value = null;
	}

	/**
	 * Stop participating in {@link OfficeFloor} configuration now tests are
	 * complete.
	 */
	@AfterClass
	public static void noFurtherConfiguration() {
		isLoadOffice = false;
	}

	/**
	 * Flags to load the {@link Office}.
	 */
	private static boolean isLoadOffice = false;

	/**
	 * Possible failure.
	 */
	private static Exception failure = null;

	/**
	 * {@link OfficeFloorRule} under test.
	 */
	@Rule
	public final OfficeFloorRule officeFloor = new OfficeFloorRule(this);

	/**
	 * Field dependency.
	 */
	private @Dependency @FromOffice(ApplicationOfficeFloorSource.OFFICE_NAME) MockObject fieldDependency;

	/**
	 * Setter dependency.
	 */
	private MockObject setterDependency;

	/**
	 * Test setter of dependency.
	 * 
	 * @param object {@link MockObject}.
	 */
	public @Dependency void setMockObject(MockObject object) {
		this.setterDependency = object;
	}

	/**
	 * Ensure able to use {@link OfficeFloorRule}.
	 */
	@Test
	public void officeFloorRule() throws Throwable {

		// Ensure various dependency injection of test
		assertSame(mockObject, this.fieldDependency, "Should inject field dependency");
		assertSame(mockObject, this.setterDependency, "Should inject setter dependency");

		// Invoke the process
		final String PARAMETER = "TEST";
		this.officeFloor.invokeProcess("SECTION.function", PARAMETER);

		// Ensure appropriately triggered
		assertEquals(PARAMETER, MockSection.value, "Should invoke rule");
	}

	/**
	 * Ensure report failed compile.
	 */
	@Test
	public void failCompile() throws Throwable {

		// Setup failure
		failure = new Exception("TEST");

		// Load and trigger
		Closure<Boolean> isRuleRun = new Closure<>(false);
		Closure<Throwable> exception = new Closure<>();
		try {
			OfficeFloorRule rule = new OfficeFloorRule();
			rule.apply(new Statement() {

				@Override
				public void evaluate() throws Throwable {
					isRuleRun.value = true;
				}
			}, null).evaluate();

			fail("Should not successfully compile");

		} catch (Error ex) {
			exception.value = ex;
		}

		// Should not run rule with failed compile
		assertFalse(isRuleRun.value, "Should not run rule");
		assertSame(failure, exception.value.getCause(), "Should propagate failure to test");
	}

	public static class MockSection {

		private static String value;

		public void function(@Parameter String argument) {
			value = argument;
		}
	}

	/**
	 * {@link MockTestDependency}.
	 */
	private static class MockTestDepedency {
	}

	/**
	 * Test dependency.
	 */
	private static MockTestDepedency DEPENDENCY = new MockTestDepedency();

	/**
	 * Ensure provide extra dependency.
	 */
	@ClassRule
	public static final MockTestDependencyService extraDependency = new MockTestDependencyService(DEPENDENCY);

	private @Dependency MockTestDepedency testDependency;

	/**
	 * Ensure can inject into dependency from {@link TestDependencyService}.
	 */
	@Test
	public void dependencyTestDependency() throws Throwable {
		assertSame(DEPENDENCY, this.testDependency, "Should inject extra test dependency");
	}

	/*
	 * =================== OfficeExtensionService =====================
	 */

	private static final MockObject mockObject = new MockObject();

	@Override
	public OfficeExtensionService createService(ServiceContext context) throws Throwable {
		return this;
	}

	@Override
	public void extendOffice(OfficeArchitect officeArchitect, OfficeExtensionContext context) throws Exception {

		// Determine if load office
		if (!isLoadOffice) {
			return;
		}

		// Determine if fail compile
		if (failure != null) {
			throw failure;
		}

		// Add function
		officeArchitect.addOfficeSection("SECTION", ClassSectionSource.class.getName(), MockSection.class.getName());
		Singleton.load(officeArchitect, mockObject);
	}

	/**
	 * Mock object.
	 */
	private static class MockObject {
	}

}
