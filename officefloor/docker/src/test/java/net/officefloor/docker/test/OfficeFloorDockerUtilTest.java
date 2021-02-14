/*-
 * #%L
 * Docker test utilities for OfficeFloor
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
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

package net.officefloor.docker.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.function.Supplier;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Network;
import com.github.dockerjava.core.DockerClientBuilder;

import net.officefloor.test.UsesDockerTest;

/**
 * Tests the {@link OfficeFloorDockerUtil}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorDockerUtilTest {

	private static DockerClient docker;

	@BeforeAll
	public static void createDocker() {
		docker = DockerClientBuilder.getInstance().build();
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
	 * Tests the container.
	 * 
	 * @throws Exception
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
		}

		// Ensure after close, container removed
		assertNull(findContainer.get(), "Container should be removed");

		// Ensure can close again without failure
		closeAgain.close();
	}

}
