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

package net.officefloor.plugin.managedobject.clazz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.util.logging.Logger;

import net.officefloor.frame.api.managedobject.ManagedObjectContext;
import net.officefloor.plugin.clazz.Dependency;
import net.officefloor.plugin.clazz.Init;

/**
 * Mock class for testing.
 * 
 * @author Daniel Sagenschneider
 */
public class MockClass extends ParentMockClass {

	/**
	 * Constructor dependency.
	 */
	private final String unqualifiedConstructorDependency;

	/**
	 * Constructor dependency.
	 */
	private final String qualifiedConstructorDependency;

	/**
	 * Ensure can inject dependencies.
	 */
	private @Dependency String unqualifiedFieldDependency;

	/**
	 * Qualified injected dependency.
	 */
	private @MockQualifier @Dependency String qualifiedFieldDependency;

	/**
	 * Ensure can inject method dependencies.
	 */
	private String unqualifiedMethodDependency;

	/**
	 * Qualified injected method dependency.
	 */
	private String qualifiedMethodDependency;

	/**
	 * Ensure can inject on initialise.
	 */
	private String unqualifiedInitDependency;

	/**
	 * Qualified injected on initialise.
	 */
	private String qualifiedInitDependency;

	/**
	 * {@link Logger}.
	 */
	private @Dependency Logger logger;

	/**
	 * {@link ManagedObjectContext}.
	 */
	private @Dependency ManagedObjectContext context;

	/**
	 * Single {@link Constructor} for using as instantiation.
	 * 
	 * @param unqualifiedConstructorDependency Dependency to inject.
	 * @param qualifiedConstructorDependency   Dependency to inject.
	 */
	public MockClass(String unqualifiedConstructorDependency, @MockQualifier String qualifiedConstructorDependency) {
		this.unqualifiedConstructorDependency = unqualifiedConstructorDependency;
		this.qualifiedConstructorDependency = qualifiedConstructorDependency;
	}

	/**
	 * Injects dependencies via method.
	 * 
	 * @param unqualifiedMethodDependency Dependency to inject.
	 * @param qualifiedMethodDependency   Dependency to inject.
	 */
	@Dependency
	public void inject(String unqualifiedMethodDependency, @MockQualifier String qualifiedMethodDependency) {

		// Fields should be loaded before methods
		assertNotNull("Fields loaded before methods", this.unqualifiedFieldDependency);
		assertNotNull("Fields loaded before methods", this.qualifiedFieldDependency);

		// Load the dependencies
		this.unqualifiedMethodDependency = unqualifiedMethodDependency;
		this.qualifiedMethodDependency = qualifiedMethodDependency;
	}

	/**
	 * Initialise.
	 * 
	 * @param unqualifiedInitDependency Dependency for initialising.
	 * @param qualifiedInitDependency   Dependency for initialising.
	 */
	@Init
	public void init(String unqualifiedInitDependency, @MockQualifier String qualifiedInitDependency) {

		// Method dependencies should be loaded before methods
		assertNotNull("Methods loaded before init", this.unqualifiedMethodDependency);
		assertNotNull("Methods loaded before init", this.qualifiedMethodDependency);

		// Load the dependencies
		this.unqualifiedInitDependency = unqualifiedInitDependency;
		this.qualifiedInitDependency = qualifiedInitDependency;
	}

	/**
	 * Obtains the {@link Logger}.
	 * 
	 * @return {@link Logger}
	 */
	public Logger getLogger() {
		return this.logger;
	}

	/**
	 * Verifies the dependencies.
	 * 
	 * @param unqualifiedConstructorDependency Unqualified constructor dependency.
	 * @param qualifiedConstructorDependency   Qualified constructor dependency.
	 * @param unqualifiedFieldDependency       Unqualified field dependency.
	 * @param qualifiedFieldDependency         Qualified field dependency.
	 * @param unqualifiedMethodDependency      Unqualified method dependency.
	 * @param qualifiedMethodDependency        Qualified method dependency.
	 * @param logger                           {@link Logger}.
	 * @param connection                       Expected {@link Connection}.
	 * @param unqualifiedInitDependency        Unqualified init dependency.
	 * @param qualifiedInitDependency          Qualified init dependency.
	 */
	public void verifyDependencyInjection(String unqualifiedConstructorDependency,
			String qualifiedConstructorDependency, String unqualifiedFieldDependency, String qualifiedFieldDependency,
			String unqualifiedMethodDependency, String qualifiedMethodDependency, Logger logger, Connection connection,
			String unqualifiedInitDependency, String qualifiedInitDependency) {

		// Verify dependency injection
		assertNotNull("Expecting unqualified constructor dependency", unqualifiedConstructorDependency);
		assertEquals("Incorrect unqualified constructor dependency", unqualifiedConstructorDependency,
				this.unqualifiedConstructorDependency);
		assertNotNull("Expecting qualified constructor dependency", qualifiedConstructorDependency);
		assertEquals("Incorrect qualified constructor dependency", qualifiedConstructorDependency,
				this.qualifiedConstructorDependency);
		assertNotNull("Expecting unqualified field dependency", unqualifiedFieldDependency);
		assertEquals("Incorrect unqualified field dependency", unqualifiedFieldDependency,
				this.unqualifiedFieldDependency);
		assertNotNull("Expecting qualified field dependency", qualifiedFieldDependency);
		assertEquals("Incorrect qualified field dependency", qualifiedFieldDependency, this.qualifiedFieldDependency);
		assertNotNull("Expecting unqualified method dependency", unqualifiedMethodDependency);
		assertEquals("Incorrect unqualified method dependency", unqualifiedMethodDependency,
				this.unqualifiedMethodDependency);
		assertNotNull("Expecting qualified method dependency", qualifiedMethodDependency);
		assertEquals("Incorrect qualified method dependency", qualifiedMethodDependency,
				this.qualifiedMethodDependency);
		assertEquals("Incorrect logger", this.logger.getName(), logger.getName());
		assertNotNull("Should have managed object context", this.context);
		assertSame("Should be same logger from managed object context", this.logger, this.context.getLogger());
		assertNotNull("Expecting unqualified init dependency", unqualifiedInitDependency);
		assertEquals("Incorrect unqualified init dependency", unqualifiedInitDependency,
				this.unqualifiedInitDependency);
		assertNotNull("Expecting qualified init dependency", qualifiedInitDependency);
		assertEquals("Incorrect qualified init dependency", qualifiedInitDependency, this.qualifiedInitDependency);

		// Verify parent dependencies
		super.verifyDependencyInjection(connection);
	}

}
