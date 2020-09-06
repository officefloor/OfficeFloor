package net.officefloor.jdbc.h2.test;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.officefloor.plugin.clazz.Dependency;
import net.officefloor.woof.mock.MockWoofServerExtension;

/**
 * Tests the {@link H2Reset}.
 * 
 * @author Daniel Sagenschneider
 */
@ExtendWith(MockWoofServerExtension.class)
public class H2ResetTest extends AbstractH2ResetTestCase {

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

	/**
	 * Ensure able to inject into test method.
	 */
	@Test
	public void clear(H2Reset reset) throws Exception {
		this.doTest(false, () -> reset.clean());
	}

	/**
	 * Ensure able to inject into test method.
	 */
	@Test
	public void reset(H2Reset reset) throws Exception {
		this.doTest(true, () -> reset.reset());
	}
}