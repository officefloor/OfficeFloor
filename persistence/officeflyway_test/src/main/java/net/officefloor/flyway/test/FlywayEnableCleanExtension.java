package net.officefloor.flyway.test;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.internal.configuration.ConfigUtils;
import org.junit.jupiter.api.extension.Extension;

import net.officefloor.test.system.SystemPropertiesExtension;

/**
 * {@link Extension} to enable {@link Flyway} clean.
 * 
 * @author Daniel Sagenschneider
 *
 */
public class FlywayEnableCleanExtension extends SystemPropertiesExtension {

	/**
	 * Instantiate.
	 */
	public FlywayEnableCleanExtension() {
		super(ConfigUtils.CLEAN_DISABLED, "false");
	}

}