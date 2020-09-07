package net.officefloor.jdbc.h2.test;

import java.lang.reflect.Field;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import net.officefloor.plugin.clazz.Dependency;
import net.officefloor.woof.mock.MockWoofServerRule;

/**
 * Tests the {@link H2Reset} with {@link TestRule}.
 * 
 * @author Daniel Sagenschneider
 */
public class H2ResetJUnit4Test extends AbstractH2ResetTestCase {

	/**
	 * {@link MockWoofServerRule}.
	 */
	@Rule
	public final MockWoofServerRule server = new MockWoofServerRule(this);

	/**
	 * {@link H2Reset} being tested on {@link Field} injection.
	 */
	private @Dependency H2Reset reset;

	/**
	 * Ensure able to inject into test.
	 */
	@Test
	public void clear() throws Exception {
		this.doTest(false, () -> this.reset.clean());
	}

	/**
	 * Ensure able to inject into test.
	 */
	@Test
	public void reset() throws Exception {
		this.doTest(true, () -> this.reset.reset());
	}

}