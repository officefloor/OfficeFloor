package net.officefloor.nosql.objectify.mock;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * Tests the {@link ObjectifyExtension}.
 * 
 * @author Daniel Sagenschneider
 */
public class ObjectifyExtensionTest extends AbstractObjectifyTestCase {

	/**
	 * {@link Extension} under test.
	 */
	@RegisterExtension
	public final ObjectifyExtension extension = new ObjectifyExtension();

	/*
	 * =================== AbstractObjectifyTestCase ====================
	 */

	@Override
	protected AbstractObjectifyJUnit getObjectify() {
		return this.extension;
	}

	@Test
	@Override
	public void storeGet() throws Throwable {
		super.storeGet();
	}

}