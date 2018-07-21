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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;

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
	 * Port.
	 */
	private int port;

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
	 * @param port Port to make PostreSql available on.
	 */
	public PostgreSqlRule(int port) {
		this.port = port;
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
					.exposedPorts(ports).env("POSTGRES_USER=testuser", "POSTGRES_PASSWORD=testpassword").build();

			// Start the container
			final ContainerCreation creation = docker.createContainer(containerConfig, CONTAINER_NAME);
			this.postgresContainerId = creation.id();
			this.docker.startContainer(this.postgresContainerId);
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
				PostgreSqlRule.this.startPostgreSql();
				try {
					base.evaluate();
				} finally {
					PostgreSqlRule.this.stopPostgreSql();
				}
			}
		};
	}

}