package net.officefloor.flyway;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;

/**
 * Configures the {@link Flyway}.
 * 
 * @author Daniel Sagenschneider
 */
public interface FlywayConfigurer {

	/**
	 * Configures {@link Flyway}.
	 * 
	 * @param configuration {@link FluentConfiguration} for {@link Flyway}.
	 * @throws Exception If fails to configure.
	 */
	void configure(FluentConfiguration configuration) throws Exception;

}