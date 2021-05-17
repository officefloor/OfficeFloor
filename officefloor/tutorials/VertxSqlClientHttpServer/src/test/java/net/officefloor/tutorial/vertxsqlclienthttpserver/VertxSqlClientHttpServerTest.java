package net.officefloor.tutorial.vertxsqlclienthttpserver;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.vertx.core.Vertx;
import io.vertx.sqlclient.SqlClient;
import net.officefloor.jdbc.postgresql.test.AbstractPostgreSqlJUnit.Configuration;
import net.officefloor.jdbc.postgresql.test.PostgreSqlExtension;
import net.officefloor.woof.mock.MockWoofResponse;
import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServerExtension;

/**
 * Tests the {@link Vertx} {@link SqlClient} Http Server.
 * 
 * @author Daniel Sagenschneider
 */
public class VertxSqlClientHttpServerTest {

	// START SNIPPET: tutorial
	@RegisterExtension
	public static final PostgreSqlExtension database = new PostgreSqlExtension(
			new Configuration().port(5433).database("test").username("sa").password("password"));

	@RegisterExtension
	public final MockWoofServerExtension server = new MockWoofServerExtension();

	@Test
	public void getData() {
		MockWoofResponse response = this.server.send(MockWoofServer.mockRequest("/message/1"));
		response.assertJson(200, new Message("TEST"));
	}
	// END SNIPPET: tutorial

}