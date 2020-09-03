package net.officefloor.tutorial.flywayhttpserver;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.plugin.clazz.Dependency;
import net.officefloor.woof.mock.MockWoofResponse;
import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServerExtension;

/**
 * Tests the {@link Flyway} HTTP server.
 * 
 * @author Daniel Sagenschneider
 */
public class FlywayHttpServerTest {

	@RegisterExtension
	public final MockWoofServerExtension server = new MockWoofServerExtension();

	private @Dependency Flyway flyway;

	@BeforeEach
	public void resetDatabase() {
		this.flyway.clean();
		this.flyway.migrate();
	}

	@Test
	public void migrationAvailable() {
		MockWoofResponse response = this.server.send(MockWoofServer.mockRequest("/migration?id=1"));
		response.assertJson(200, new Migration(1L, "MIGRATED"));
	}
}