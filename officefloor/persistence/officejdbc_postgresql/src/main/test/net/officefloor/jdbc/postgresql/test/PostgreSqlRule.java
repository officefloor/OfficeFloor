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
package net.officefloor.jdbc.postgresql.test;

import java.net.InetAddress;
import java.sql.Connection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.postgresql.ds.PGSimpleDataSource;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;

import net.officefloor.jdbc.test.DataSourceRule;

/**
 * {@link TestRule} to run PostgreSql.
 * 
 * @author Daniel Sagenschneider
 */
public class PostgreSqlRule implements TestRule {

	/**
	 * Pulled docker images.
	 */
	private static final Set<String> pulledDockerImages = new HashSet<>();

	/**
	 * Pulls the docker image.
	 * 
	 * @param imageName Docker image name.
	 * @param client    {@link DockerClient}.
	 * @throws Exception If fails to pull image.
	 */
	public static void pullDockerImage(String imageName, DockerClient client) throws Exception {

		// Determine if already pulled
		if (pulledDockerImages.contains(imageName)) {
			return;
		}

		// Pull the docker image
		Consumer<String> print = (message) -> {
			System.out.print(message == null ? "" : " " + message);
			System.out.flush();
		};
		client.pull(imageName, (message) -> {
			print.accept(message.progress());
			print.accept(message.status());
			print.accept(message.id());
			System.out.println();
		});

		// Flag that pulled image
		pulledDockerImages.add(imageName);
	}

	/**
	 * Server.
	 */
	private final String server;

	/**
	 * Port.
	 */
	private final int port;

	/**
	 * Optional name of database to create.
	 */
	private final String databaseName;

	/**
	 * Username.
	 */
	private final String username;

	/**
	 * Password.
	 */
	private final String password;

	/**
	 * {@link DockerClient}.
	 */
	private DockerClient docker;

	/**
	 * Identifier for the PostgreSql container.
	 */
	private String postgresContainerId;

	/**
	 * Instantiate.
	 * 
	 * @param server       Server to ensure can connect (confirms accessible from
	 *                     127.0.0.1 address).
	 * @param port         Port to make PostreSql available on.
	 * @param databaseName Name of the database to create within PostgreSql.
	 * @param username     Username to connect to PostgreSql.
	 * @param password     Password to connect to PostgreSql.
	 */
	public PostgreSqlRule(String server, int port, String databaseName, String username, String password) {
		this.server = server;
		this.port = port;
		this.databaseName = databaseName;
		this.username = username;
		this.password = password;
	}

	/**
	 * Instantiate.
	 * 
	 * @param port     Port to make PostreSql available on.
	 * @param username Username to connect to PostgreSql.
	 * @param password Password to connect to PostgreSql.
	 */
	public PostgreSqlRule(int port, String username, String password) {
		this(null, port, null, username, password);
	}

	/**
	 * Starts PostgreSql.
	 * 
	 * @throws Exception If fails to start PostgreSql.
	 */
	public void startPostgreSql() throws Exception {

		final String IMAGE_NAME = "postgres:latest";
		final String CONTAINER_NAME = "officefloor_postgres";

		// Create the docker client
		this.docker = DefaultDockerClient.fromEnv().build();

		// Determine if container already running
		for (Container container : this.docker.listContainers()) {
			for (String name : container.names()) {
				if (name.equals("/" + CONTAINER_NAME)) {
					this.postgresContainerId = container.id();
				}
			}
		}

		// Start PostgreSQL (if not running)
		if (this.postgresContainerId == null) {
			System.out.println();
			System.out.println("Starting PostgreSQL");
			pullDockerImage(IMAGE_NAME, this.docker);

			// Bind container port to host port
			final String[] ports = { "5432" };
			final Map<String, List<PortBinding>> portBindings = new HashMap<>();
			portBindings.put("5432", Arrays.asList(PortBinding.of("0.0.0.0", String.valueOf(this.port))));
			final HostConfig hostConfig = HostConfig.builder().portBindings(portBindings).build();
			final ContainerConfig containerConfig = ContainerConfig.builder().hostConfig(hostConfig).image(IMAGE_NAME)
					.exposedPorts(ports).env("POSTGRES_USER=" + this.username, "POSTGRES_PASSWORD=" + this.password)
					.build();

			// Start the container
			final ContainerCreation creation = docker.createContainer(containerConfig, CONTAINER_NAME);
			this.postgresContainerId = creation.id();
			this.docker.startContainer(this.postgresContainerId);
		}
	}

	/**
	 * Obtains a {@link Connection}.
	 * 
	 * @return {@link Connection}.
	 * @throws Exception If fails to obtain {@link Connection}.
	 */
	public Connection getConnection() throws Exception {
		return this.getConnection(this.databaseName);
	}

	/**
	 * Obtains the {@link Connection}.
	 * 
	 * @param databaseName Database name. May be <code>null</code> (for creating the
	 *                     database).
	 * @return {@link Connection}.
	 * @throws Exception If fails to get {@link Connection} to the database.
	 */
	private Connection getConnection(String databaseName) throws Exception {

		// Create DataSource
		PGSimpleDataSource dataSource = new PGSimpleDataSource();
		dataSource.setPortNumber(this.port);
		dataSource.setUser(this.username);
		dataSource.setPassword(this.password);
		if (databaseName != null) {
			dataSource.setDatabaseName(databaseName);
		}

		// Run (without logging)
		Logger logger = Logger.getLogger("org.postgresql");
		Level level = logger.getLevel();
		try {
			logger.setLevel(Level.OFF);
			logger.setUseParentHandlers(false);
			return DataSourceRule.waitForDatabaseAvailable(() -> dataSource.getConnection());
		} finally {
			logger.setLevel(level);
		}
	}

	/**
	 * Stops PostgreSql.
	 * 
	 * @throws Exception If fails to stop PostgreSql.
	 */
	public void stopPostgreSql() throws Exception {

		// Stop PostgresSQL
		System.out.println("Stopping PostgreSQL");
		this.docker.killContainer(this.postgresContainerId);
		this.docker.removeContainer(this.postgresContainerId);
		this.docker.close();
	}

	/*
	 * ================== TestRule ===================
	 */

	@Override
	public Statement apply(Statement base, Description description) {
		return new Statement() {

			@Override
			public void evaluate() throws Throwable {

				// Ensure possible server
				if (PostgreSqlRule.this.server != null) {
					InetAddress address;
					try {
						address = InetAddress.getByName(PostgreSqlRule.this.server);
					} catch (Throwable ex) {
						throw new IllegalStateException("INVALID SETUP: need to configure " + PostgreSqlRule.this.server
								+ " as loop back (127.0.0.1)", ex);
					}
					Assert.assertTrue("INVALID SETUP: dns " + PostgreSqlRule.this.server
							+ " must be configured as loop back (127.0.0.1)", address.isLoopbackAddress());
				}

				// Start PostgreSql
				PostgreSqlRule.this.startPostgreSql();
				try {

					// Create the possible required database
					if (PostgreSqlRule.this.databaseName != null) {
						try (Connection connection = PostgreSqlRule.this.getConnection(null)) {
							connection.createStatement().execute("CREATE DATABASE " + PostgreSqlRule.this.databaseName);
						}
					}

					// Run the test
					base.evaluate();

				} finally {
					// Stop PostgreSql
					PostgreSqlRule.this.stopPostgreSql();
				}
			}
		};
	}

}