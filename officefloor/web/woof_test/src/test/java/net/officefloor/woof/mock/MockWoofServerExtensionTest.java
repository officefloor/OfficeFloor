/*-
 * #%L
 * Web on OfficeFloor Testing
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

	/**
	 * {@link MockWoofServerExtension} to test.
	 */
	@RegisterExtension
	public static final MockWoofServerExtension server = new MockWoofServerExtension();

	private final MockObject constructorDependency;

	public MockWoofServerExtensionTest(MockObject dependency) {
		this.constructorDependency = dependency;
	}

	private @Dependency MockObject fieldDependency;

	private MockObject setterDependency;

	public @Dependency void setDependency(MockObject dependency) {
		this.setterDependency = dependency;
	}

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

}
