package net.officefloor.nosql.objectify.mock;

import org.junit.Rule;
import org.junit.Test;

/**
 * Tests the {@link ObjectifyRule}.
 * 
 * @author Daniel Sagenschneider
 */
public class ObjectifyRuleTest extends AbstractObjectifyTestCase {

	/**
	 * {@link Rule} under test.
	 */
	@Rule
	public final ObjectifyRule rule = new ObjectifyRule();

	/*
	 * =================== AbstractObjectifyTestCase ====================
	 */

	@Override
	protected AbstractObjectifyJUnit getObjectify() {
		return this.rule;
	}

	@Test
	@Override
	public void storeGet() throws Throwable {
		super.storeGet();
	}

}