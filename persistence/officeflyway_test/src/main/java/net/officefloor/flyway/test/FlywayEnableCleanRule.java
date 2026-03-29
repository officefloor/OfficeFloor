package net.officefloor.flyway.test;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.internal.configuration.ConfigUtils;
import org.junit.rules.TestRule;

import net.officefloor.test.system.SystemPropertiesRule;

/**
 * {@link TestRule} to enable {@link Flyway} clean.
 * 
 * @author Daniel Sagenschneider
 */
public class FlywayEnableCleanRule extends SystemPropertiesRule {

	/**
	 * Instantiate.
	 */
	public FlywayEnableCleanRule() {
		super(ConfigUtils.CLEAN_DISABLED, "false");
	}

}
