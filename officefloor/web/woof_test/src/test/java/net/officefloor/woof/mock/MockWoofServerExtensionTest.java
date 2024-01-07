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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.plugin.clazz.Dependency;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.test.TestDependencyService;
import net.officefloor.woof.MockObject;

/**
 * Tests the {@link MockWoofServerExtension}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockWoofServerExtensionTest {

	private static final MockAddTestDependencyService MOCK_ADD = new MockAddTestDependencyService();

	/**
	 * {@link MockWoofServerExtension} to test.
	 */
	public @RegisterExtension static final MockWoofServerExtension server = new MockWoofServerExtension()
			.testDependencyService(MOCK_ADD);

	private final MockObject constructorDependency;

	public MockWoofServerExtensionTest(MockObject dependency) {
		this.constructorDependency = dependency;
	}

	private @Dependency MockObject fieldDependency;

	private MockObject setterDependency;

	public @Dependency void setDependency(MockObject dependency) {
		this.setterDependency = dependency;
	}

	private @Dependency MockAddTestDependencyService mockAdded;

	/**
	 * Ensure the dependencies are available.
	 */
	@Test
	public void injectDependencies(MockObject parameter) {
		assertNotNull(parameter, "Should inject parameter");
		assertSame(parameter, this.constructorDependency, "Should inject constructor dependency");
		assertSame(parameter, this.fieldDependency, "Should inject field dependency");
		assertSame(parameter, this.setterDependency, "Incorrect setter dependency");
	}

	/**
	 * Ensure able to service a request.
	 */
	@Test
	public void serviceRequest() {
		MockHttpResponse response = server.send(MockWoofServer.mockRequest("/template"));
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
	@RegisterExtension
	public static final MockTestDependencyService extraDependency = new MockTestDependencyService(DEPENDENCY);

	/**
	 * Ensure can inject into dependency from {@link TestDependencyService}.
	 */
	@Test
	public void injectTestDependency(MockTestDepedency dependency) throws Throwable {
		assertSame(DEPENDENCY, dependency, "Should inject extra test dependency");
	}

	private @Dependency MockTestDepedency testDependency;

	/**
	 * Ensure can inject into dependency from {@link TestDependencyService}.
	 */
	@Test
	public void dependencyTestDependency() throws Throwable {
		assertSame(DEPENDENCY, this.testDependency, "Should inject extra test dependency");
	}

	/**
	 * Ensure can add {@link TestDependencyService}.
	 */
	@Test
	public void addedDependency(MockAddTestDependencyService parameter) throws Throwable {
		assertSame(MOCK_ADD, this.mockAdded, "Should include added dependency");
		assertSame(MOCK_ADD, parameter, "Should be able to use added as parameter");
	}

}
