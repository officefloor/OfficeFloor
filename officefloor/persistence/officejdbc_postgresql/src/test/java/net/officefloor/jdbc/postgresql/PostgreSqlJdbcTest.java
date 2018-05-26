/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.jdbc.postgresql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;

import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.jdbc.ConnectionManagedObjectSource;
import net.officefloor.jdbc.test.AbstractJdbcTestCase;

/**
 * Tests the {@link PostgreSqlConnectionManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class PostgreSqlJdbcTest extends AbstractJdbcTestCase {

	private DockerClient docker;

	private String postgresContainerId;

	@Override
	protected void setUp() throws Exception {

		// Start PostgreSQL
		System.out.println();
		System.out.println("Starting PostgreSQL");
		Consumer<String> print = (message) -> {
			System.out.print(message == null ? "" : " " + message);
			System.out.flush();
		};
		this.docker = DefaultDockerClient.fromEnv().build();
		this.docker.pull("postgres:latest", (message) -> {
			print.accept(message.progress());
			print.accept(message.status());
			print.accept(message.id());
			System.out.println();
		});

		// Bind container port to host port
		final String[] ports = { "5432" };
		final Map<String, List<PortBinding>> portBindings = new HashMap<>();
		for (String port : ports) {
			portBindings.put(port, Arrays.asList(PortBinding.of("0.0.0.0", port)));
		}
		final HostConfig hostConfig = HostConfig.builder().portBindings(portBindings).build();
		final ContainerConfig containerConfig = ContainerConfig.builder().hostConfig(hostConfig).image("postgres:latest")
				.exposedPorts(ports).env("POSTGRES_USER=test", "POSTGRES_PASSWORD=test").build();

		// Start the container
		final ContainerCreation creation = docker.createContainer(containerConfig, "officefloor_postgres");
		this.postgresContainerId = creation.id();
		this.docker.startContainer(this.postgresContainerId);

		// Run setup (now database available to connect)
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {

		// Stop PostgresSQL
		this.docker.killContainer(this.postgresContainerId);
		this.docker.removeContainer(this.postgresContainerId);
		this.docker.close();

		// Complete tear down
		super.tearDown();
	}

	@Override
	protected Class<? extends ConnectionManagedObjectSource> getConnectionManagedObjectSourceClass() {
		return PostgreSqlConnectionManagedObjectSource.class;
	}

	@Override
	protected void loadProperties(PropertyConfigurable mos) {
		mos.addProperty(PostgreSqlConnectionManagedObjectSource.PROPERTY_SERVER_NAME, "localhost");
		mos.addProperty(PostgreSqlConnectionManagedObjectSource.PROPERTY_PORT, "5432");
		mos.addProperty(PostgreSqlConnectionManagedObjectSource.PROPERTY_DATABASE_NAME, "test");
		mos.addProperty(PostgreSqlConnectionManagedObjectSource.PROPERTY_USER, "test");
		mos.addProperty(PostgreSqlConnectionManagedObjectSource.PROPERTY_PASSWORD, "test");
	}

	@Override
	protected void cleanDatabase(Connection connection) throws SQLException {
		try (Statement statement = connection.createStatement()) {
			// New docker image each time, so always clean
			// Just create table to ensure connection up
			statement.executeUpdate("CREATE TABLE RUNNING ( ID INT)");
		}
	}

}