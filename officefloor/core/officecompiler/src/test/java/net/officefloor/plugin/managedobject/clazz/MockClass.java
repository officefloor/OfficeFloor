package net.officefloor.plugin.managedobject.clazz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.util.logging.Logger;

import net.officefloor.frame.api.managedobject.ManagedObjectContext;
import net.officefloor.plugin.clazz.Dependency;

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
	 * Verifies the dependencies.
	 * 
	 * @param unqualifiedConstructorDependency Unqualified constructor dependency.
	 * @param qualifiedConstructorDependency   Qualified constructor dependency.
	 * @param unqualifiedFieldDependency       Unqualified field dependency.
	 * @param qualifiedFieldDependency         Qualified field dependency.
	 * @param logger                           {@link Logger}.
	 * @param connection                       Expected {@link Connection}.
	 */
	public void verifyDependencyInjection(String unqualifiedConstructorDependency,
			String qualifiedConstructorDependency, String unqualifiedFieldDependency, String qualifiedFieldDependency,
			Logger logger, Connection connection) {

		// Verify dependency injection
		assertNotNull("Expecting unqualified constructor dependency", unqualifiedConstructorDependency);
		assertEquals("Incorrect unqualified constructor dependency", unqualifiedConstructorDependency,
				this.unqualifiedConstructorDependency);
		assertNotNull("Expecting qualified constructor dependency", qualifiedConstructorDependency);
		assertEquals("Incorrect qualified constructor dependency", qualifiedConstructorDependency,
				this.qualifiedConstructorDependency);
		assertNotNull("Expecting unqualified dependency", unqualifiedFieldDependency);
		assertEquals("Incorrect unqualified dependency", unqualifiedFieldDependency, this.unqualifiedFieldDependency);
		assertNotNull("Expecting qualified dependency", qualifiedFieldDependency);
		assertEquals("Incorrect qualified dependency", qualifiedFieldDependency, this.qualifiedFieldDependency);
		assertEquals("Incorrect logger", this.logger.getName(), logger.getName());
		assertNotNull("Should have managed object context", this.context);
		assertSame("Should be same logger from managed object context", this.logger, this.context.getLogger());

		// Verify parent dependencies
		super.verifyDependencyInjection(connection);
	}

}