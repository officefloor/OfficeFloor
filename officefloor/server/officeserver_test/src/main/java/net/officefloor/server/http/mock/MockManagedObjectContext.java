package net.officefloor.server.http.mock;

import static org.junit.Assert.fail;

import java.util.logging.Logger;

import net.officefloor.frame.api.managedobject.ManagedObjectContext;
import net.officefloor.frame.api.managedobject.ProcessSafeOperation;

/**
 * Mock {@link ManagedObjectContext} that just runs the
 * {@link ProcessSafeOperation}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockManagedObjectContext implements ManagedObjectContext {

	/*
	 * =================== ManagedObjectContext =====================
	 */

	@Override
	public String getBoundName() {
		fail("Should not require bound name");
		return null;
	}

	@Override
	public Logger getLogger() {
		fail("Should not require logger");
		return null;
	}

	@Override
	public <R, T extends Throwable> R run(ProcessSafeOperation<R, T> operation) throws T {
		return operation.run();
	}

}