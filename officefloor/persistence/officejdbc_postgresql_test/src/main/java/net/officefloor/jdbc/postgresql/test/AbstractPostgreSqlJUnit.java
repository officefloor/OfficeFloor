/*-
 * #%L
 * PostgreSQL Persistence Testing
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.jdbc.postgresql.test;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.postgresql.ds.PGSimpleDataSource;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.exception.DockerClientException;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports.Binding;
import com.github.dockerjava.api.model.PullResponseItem;
import com.github.dockerjava.core.DockerClientBuilder;

import net.officefloor.jdbc.test.DatabaseTestUtil;
import net.officefloor.test.JUnitAgnosticAssert;
import net.officefloor.test.SkipUtil;

/**
 * Abstract JUnit PostgreSql functionality.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractPostgreSqlJUnit {

	/**
	 * <p>
	 * Configuration of the PostgreSql database.
	 * <p>
	 * Follows builder pattern to allow configuring and passing to
	 * {@link AbstractPostgreSqlJUnit} constructor.
	 */
	public static class Configuration {

		/**
		 * Server.
		 */
		private String server = "localhost";

		/**
		 * Port.
		 */
		private int port = 5432;

		/**
		 * Optional name of database to create.
		 */
		private String databaseName = null;

		/**
		 * Username.
		 */
		private String username = "testuser";

		/**
		 * Password.
		 */
		private String password = "testpassword";

		/**
		 * Max connections.
		 */
		private int maxConnections = 0;

		/**
		 * Specifies the server.
		 * 
		 * @param server Server.
		 * @return <code>this</code>.
		 */
		public Configuration server(String server) {
			this.server = server;
			return this;
		}

		/**
		 * Specifies the port.
		 * 
		 * @param port Port.
		 * @return <code>this</code>.
		 */
		public Configuration port(int port) {
			this.port = port;
			return this;
		}

		/**
		 * Specifies the database.
		 * 
		 * @param databaseName Database name.
		 * @return <code>this</code>.
		 */
		public Configuration database(String databaseName) {
			this.databaseName = databaseName;
			return this;
		}

		/**
		 * Specifies the user name.
		 * 
		 * @param username User name.
		 * @return <code>this</code>.
		 */
		public Configuration username(String username) {
			this.username = username;
			return this;
		}

		/**
		 * Specifies the password.
		 * 
		 * @param password Password.
		 * @return <code>this</code>.
		 */
		public Configuration password(String password) {
			this.password = password;
			return this;
		}

		/**
		 * Specifies the max connections.
		 * 
		 * @param maxConnections Max connections.
		 * @return <code>this</code>.
		 */
		public Configuration maxConnections(int maxConnections) {
			this.maxConnections = maxConnections;
			return this;
		}
	}

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

		try {

			// Pull the docker image
			client.pullImageCmd(imageName).exec(new PullImageResultCallback() {

				@Override
				public void onNext(PullResponseItem item) {
					if (item.getProgressDetail() != null) {
						System.out.println(item.getProgressDetail());
					}
					super.onNext(item);
				}
			}).awaitCompletion(10, TimeUnit.MINUTES);

		} catch (DockerClientException ex) {

			// Failed to pull image, determine if already exists
			// (typically as no connection to Internet to check)
			List<Image> images = client.listImagesCmd().exec();
			boolean isImageExist = false;
			for (Image image : images) {
				if (image.getRepoTags() != null) {
					for (String tag : image.getRepoTags()) {
						if (imageName.equals(tag)) {
							isImageExist = true;
						}
					}
				}
			}
			if (!isImageExist) {
				// Propagate the failure
				throw ex;
			}
		}

		// Flag that pulled image
		pulledDockerImages.add(imageName);
	}

	/**
	 * {@link Configuration} for this {@link AbstractPostgreSqlJUnit}.
	 */
	private final Configuration configuration;

	/**
	 * {@link DockerClient}.
	 */
	private DockerClient docker;

	/**
	 * Identifier for the PostgreSql container.
	 */
	private String postgresContainerId;

	/**
	 * Created {@link Connection} instances.
	 */
	private final Deque<Connection> connections = new ConcurrentLinkedDeque<>();

	/**
	 * Instantiate with default {@link Configuration}.
	 */
	public AbstractPostgreSqlJUnit() {
		this(new Configuration());
	}

	/**
	 * Instantiate.
	 * 
	 * @param configuration {@link Configuration}.
	 */
	public AbstractPostgreSqlJUnit(Configuration configuration) {
		this.configuration = configuration;
	}

	/**
	 * Starts PostgreSql.
	 * 
	 * @throws Exception If fails to start PostgreSql.
	 */
	public void startPostgreSql() throws Exception {

		// Avoid starting up if docker skipped
		if (SkipUtil.isSkipTestsUsingDocker()) {
			System.out.println("Docker not available. Unable to start PostgreSql.");
			return;
		}

		// Ensure possible server
		if (this.configuration.server != null) {
			InetAddress address;
			try {
				address = InetAddress.getByName(this.configuration.server);
			} catch (Throwable ex) {
				JUnitAgnosticAssert.fail("INVALID SETUP: need to configure " + this.configuration.server
						+ " as loop back (127.0.0.1). " + ex.getMessage());
				return;
			}
			JUnitAgnosticAssert.assertTrue(address.isLoopbackAddress(),
					"INVALID SETUP: dns " + this.configuration.server + " must be configured as loop back (127.0.0.1)");
		}

		final String IMAGE_NAME = "postgres:latest";
		final String CONTAINER_NAME = "officefloor_postgres";

		// Create the docker client
		this.docker = DockerClientBuilder.getInstance().build();

		// Determine if container already running
		for (Container container : this.docker.listContainersCmd().exec()) {
			for (String name : container.getNames()) {
				if (name.equals("/" + CONTAINER_NAME)) {
					this.postgresContainerId = container.getId();
				}
			}
		}

		// Start PostgreSQL (if not running)
		if (this.postgresContainerId == null) {
			System.out.println();
			System.out.println("Starting PostgreSQL");
			pullDockerImage(IMAGE_NAME, this.docker);

			// Create the container
			final HostConfig hostConfig = HostConfig.newHostConfig().withPortBindings(
					new PortBinding(Binding.bindIpAndPort("0.0.0.0", this.configuration.port), ExposedPort.tcp(5432)));
			CreateContainerCmd createContainerCmd = this.docker.createContainerCmd(IMAGE_NAME).withName(CONTAINER_NAME)
					.withHostConfig(hostConfig).withEnv("POSTGRES_USER=" + this.configuration.username,
							"POSTGRES_PASSWORD=" + this.configuration.password);
			if (this.configuration.maxConnections > 0) {
				createContainerCmd = createContainerCmd.withCmd("postgres", "-N",
						String.valueOf(this.configuration.maxConnections));
			}
			CreateContainerResponse createdContainer = createContainerCmd.exec();

			// Start the container
			this.postgresContainerId = createdContainer.getId();
			this.docker.startContainerCmd(this.postgresContainerId).exec();
		}

		// Create the possible required database
		if (AbstractPostgreSqlJUnit.this.configuration.databaseName != null) {
			try (Connection connection = AbstractPostgreSqlJUnit.this.getConnection(null)) {
				connection.createStatement()
						.execute("DROP DATABASE IF EXISTS " + AbstractPostgreSqlJUnit.this.configuration.databaseName);
				connection.createStatement()
						.execute("CREATE DATABASE " + AbstractPostgreSqlJUnit.this.configuration.databaseName);
			}
		}
	}

	/**
	 * Obtains a {@link Connection}.
	 * 
	 * @return {@link Connection}.
	 * @throws Exception If fails to obtain {@link Connection}.
	 */
	public Connection getConnection() throws Exception {
		return this.getConnection(this.configuration.databaseName);
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
		dataSource.setPortNumbers(new int[] { this.configuration.port });
		dataSource.setUser(this.configuration.username);
		dataSource.setPassword(this.configuration.password);
		if (databaseName != null) {
			dataSource.setDatabaseName(databaseName);
		}

		// Run (without logging)
		Logger logger = Logger.getLogger("org.postgresql");
		Level level = logger.getLevel();
		try {
			logger.setLevel(Level.OFF);
			logger.setUseParentHandlers(false);
			Connection connection = DatabaseTestUtil.waitForAvailableConnection((context) -> dataSource);
			this.connections.add(connection);
			return connection;
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

		// Avoid stopping up if docker skipped
		if (SkipUtil.isSkipTestsUsingDocker()) {
			return;
		}

		// Close all the connections
		for (Connection connection : this.connections) {
			try {
				connection.close();
			} catch (SQLException ex) {
				// ignore failure
			}
		}

		// Stop PostgresSQL
		System.out.println("Stopping PostgreSQL");
		this.docker.killContainerCmd(this.postgresContainerId).exec();
		this.docker.removeContainerCmd(this.postgresContainerId).exec();
		this.docker.close();
	}

}