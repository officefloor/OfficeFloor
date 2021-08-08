/*-
 * #%L
 * Web on OfficeFloor Testing
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

package net.officefloor.woof.mock;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import net.officefloor.plugin.clazz.Dependency;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.test.TestDependencyService;
import net.officefloor.woof.MockObject;

/**
 * Tests the {@link MockWoofServerRule}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockWoofServerRuleTest {

	/**
	 * {@link MockWoofServerRule} to test.
	 */
	@Rule
	public final MockWoofServerRule server = new MockWoofServerRule(this);

	private @Dependency MockObject fieldDependency;

	private MockObject setterDependency;

	public @Dependency void setDependency(MockObject dependency) {
		this.setterDependency = dependency;
	}

	/**
	 * Ensure the dependencies are available.
	 */
	@Test
	public void injectDependencies() {
		assertNotNull("Should inject field dependency", this.fieldDependency);
		assertSame("Incorrect setter dependency", this.fieldDependency, this.setterDependency);
	}

	/**
	 * Ensure able to service a request.
	 */
	@Test
	public void serviceRequest() {
		MockHttpResponse response = this.server.send(MockWoofServer.mockRequest("/template"));
		response.assertResponse(200, "TEMPLATE");
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

}
