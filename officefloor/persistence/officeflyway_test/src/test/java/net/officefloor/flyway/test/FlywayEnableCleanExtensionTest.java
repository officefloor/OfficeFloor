package net.officefloor.flyway.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class FlywayEnableCleanExtensionTest extends AbstractFlywayEnableCleanTestCase {

	public static final @RegisterExtension FlywayEnableCleanExtension clean = new FlywayEnableCleanExtension();

	/**
	 * Ensure can clean.
	 */
	@Test
	public void clean() throws Throwable {
		this.doClean();
	}
}
