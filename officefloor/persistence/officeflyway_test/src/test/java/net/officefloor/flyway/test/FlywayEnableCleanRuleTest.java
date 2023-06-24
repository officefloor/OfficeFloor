package net.officefloor.flyway.test;

import org.junit.ClassRule;
import org.junit.Test;

public class FlywayEnableCleanRuleTest extends AbstractFlywayEnableCleanTestCase {

	public static final @ClassRule FlywayEnableCleanRule clean = new FlywayEnableCleanRule();

	/**
	 * Ensure can clean with {@link FlywayEnableCleanRule}.
	 */
	@Test
	public void clean() throws Throwable {
		this.doClean();
	}
}
