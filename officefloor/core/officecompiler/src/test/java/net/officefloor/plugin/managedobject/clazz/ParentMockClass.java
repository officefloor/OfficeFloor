package net.officefloor.plugin.managedobject.clazz;

import java.sql.Connection;

import junit.framework.TestCase;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.plugin.clazz.Dependency;

/**
 * Parent mock class for testing.
 * 
 * @author Daniel Sagenschneider
 */
public class ParentMockClass {

	/**
	 * {@link Connection}.
	 */
	private @Dependency Connection connection;

	/**
	 * Ensure can invoke {@link ProcessState}.
	 */
	private MockProcessInterface processes;

	/**
	 * Field not a dependency.
	 */
	protected String notDependency;

	/**
	 * Verifies the dependencies injected.
	 * 
	 * @param connection Expected {@link Connection}.
	 */
	public void verifyDependencyInjection(Connection connection) {
		// Verify dependency injection
		TestCase.assertEquals("Incorrect connection", connection, this.connection);
	}

	/**
	 * Verifies the processes injected.
	 * 
	 * @param processParameter Parameter for the invoked processes.
	 */
	public void verifyProcessInjection(Integer processParameter) {
		// Verify can invoke processes
		this.processes.doProcess();
		this.processes.parameterisedProcess(processParameter);
	}

}