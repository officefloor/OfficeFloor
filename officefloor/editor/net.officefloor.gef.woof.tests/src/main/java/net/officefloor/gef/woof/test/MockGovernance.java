package net.officefloor.gef.woof.test;

import java.sql.Connection;

import net.officefloor.frame.api.governance.Governance;
import net.officefloor.plugin.governance.clazz.Disregard;
import net.officefloor.plugin.governance.clazz.Enforce;
import net.officefloor.plugin.governance.clazz.Govern;

/**
 * Mock {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockGovernance {

	@Govern
	public void register(Connection connection) {
		// only mocking
	}

	@Enforce
	public void commit() {
		// would commit transaction
	}

	@Disregard
	public void rollback() {
		// would rollback transaction
	}
}