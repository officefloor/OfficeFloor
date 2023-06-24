/*-
 * #%L
 * Docker test utilities for OfficeFloor
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.docker.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.function.Supplier;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.Network;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;

import net.officefloor.frame.test.FileTestSupport;
import net.officefloor.frame.test.LogTestSupport;
import net.officefloor.frame.test.TestSupportExtension;
import net.officefloor.test.UsesDockerTest;

/**
 * Tests the {@link OfficeFloorDockerUtil}.
 * 
 * @author Daniel Sagenschneider
 */
@ExtendWith(TestSupportExtension.class)
public class OfficeFloorDockerUtilTest {

	private final LogTestSupport log = new LogTestSupport();

	private final FileTestSupport file = new FileTestSupport();

	private static DockerClient docker;

	@BeforeAll
	public static void createDocker() {
		DockerClientConfig dockerConfig = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
		DockerHttpClient dockerHttpClient = new ApacheDockerHttpClient.Builder()
				.dockerHost(dockerConfig.getDockerHost()).sslConfig(dockerConfig.getSSLConfig()).maxConnections(100)
				.connectionTimeout(Duration.ofSeconds(30)).responseTimeout(Duration.ofSeconds(45)).build();
		docker = DockerClientImpl.getInstance(dockerConfig, dockerHttpClient);
	}

	@AfterAll
	public static void closeDocker() throws Exception {
		docker.close();
	}

	/**
	 * Tests the network.
	 */
	@UsesDockerTest
	public void network() throws Exception {

		// Name of network
		final String networkName = "officefloor-test";

		// Provide means to get network
		@SuppressWarnings("resource")
		Supplier<Network> findNetwork = () -> {
			for (Network network : docker.listNetworksCmd().exec()) {
				if (networkName.equals(network.getName())) {
					return network;
				}
			}
			return null; // network not found
		};

		// Ensure create network
		DockerNetworkInstance closeAgain;
		try (DockerNetworkInstance network = OfficeFloorDockerUtil.ensureNetworkAvailable(networkName)) {
			assertNotNull(findNetwork.get(), "Should have network available");
			closeAgain = network;
		}

		// Ensure after close, network removed
		assertNull(findNetwork.get(), "Network should be removed");

		// Ensure can close again without failure
		closeAgain.close();
	}

	/**
	 * Tests the image creation.
	 */
	@UsesDockerTest
	public void image() throws Exception {

		// Docker container details
		final String imageName = "officefloor-test:test";

		// Ensure build image
		OfficeFloorDockerUtil.ensureImageAvailable(imageName,
				() -> this.file.findFile(this.getClass(), "Dockerfile").getParentFile());

		// Ensure have image
		boolean isImageAvailable = false;
		for (Image image : docker.listImagesCmd().withShowAll(true).exec()) {
			String[] repoTags = image.getRepoTags();
			if (repoTags != null) {
				for (String repoTag : repoTags) {
					if (imageName.equals(repoTag)) {
						isImageAvailable = true;
					}
				}
			}
		}
		assertTrue(isImageAvailable, "Should build and have image available");
	}

	/**
	 * Tests the container.
	 */
	@UsesDockerTest
	public void container() throws Exception {

		// Docker container details (image requires tag)
		final String imageName = "hello-world:latest";
		final String containerName = "officefloor-test";

		// Provides means to obtain image
		@SuppressWarnings("resource")
		Supplier<Container> findContainer = () -> {
			for (Container container : docker.listContainersCmd().withShowAll(true).exec()) {
				for (String name : container.getNames()) {
					if (name.equals("/" + containerName)) {
						return container;
					}
				}
			}
			return null; // container not found
		};

		// Ensure start container
		DockerContainerInstance closeAgain;
		try (DockerContainerInstance container = OfficeFloorDockerUtil.ensureContainerAvailable("officefloor-test",
				imageName, (client) -> client.createContainerCmd(imageName).withName(containerName))) {
			assertNotNull(findContainer.get(), "Should have container available");
			closeAgain = container;

			// Ensure can connect
			Object connectResult = "RESULT";
			Object result = container.connectToDockerInstance(() -> connectResult);
			assertSame(connectResult, result, "Should have same result");

			// Ensure log container details on connect failure
			String dockerLogs = this.log.captureStdOutErr(() -> {
				Throwable failure = new Throwable("TEST");
				try {
					container.connectToDockerInstance(() -> {
						throw failure;
					});
				} catch (Throwable ex) {
					assertSame(failure, ex, "Should be same failure");
				}
			});
			assertTrue(dockerLogs.contains("Hello from Docker!"), "Invalid docker logs: " + dockerLogs);
		}

		// Ensure after close, container removed
		assertNull(findContainer.get(), "Container should be removed");

		// Ensure can close again without failure
		closeAgain.close();
	}

}
