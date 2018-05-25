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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		System.out.println("Starting PostgreSQL");
		this.docker = DefaultDockerClient.fromEnv().build();
		this.docker.pull("postgres:latest", (message) -> {
			System.out.println(message);
		});

		// Bind container port to host port
		final String[] ports = { "5432" };
		final Map<String, List<PortBinding>> portBindings = new HashMap<>();
		for (String port : ports) {
			List<PortBinding> hostPorts = new ArrayList<>();
			hostPorts.add(PortBinding.of("0.0.0.0", port));
			portBindings.put(port, hostPorts);
		}
		final HostConfig hostConfig = HostConfig.builder().portBindings(portBindings).build();
		final ContainerConfig containerConfig = ContainerConfig.builder().hostConfig(hostConfig).image("postgres")
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
		// TODO Auto-generated method stub

	}

}