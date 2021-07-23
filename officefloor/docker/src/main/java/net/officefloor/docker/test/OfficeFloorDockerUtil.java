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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.CreateNetworkResponse;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.Network;
import com.github.dockerjava.api.model.PullResponseItem;
import com.github.dockerjava.api.model.ResponseItem.ProgressDetail;
import com.github.dockerjava.core.DockerClientBuilder;

/**
 * Docker wrapper for running third party services (typically for testing).
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorDockerUtil {

	/**
	 * Pulled docker images.
	 */
	private static final Set<String> pulledDockerImages = new HashSet<>();

	/**
	 * Ensures the docker network is available.
	 * 
	 * @param networkName Network name.
	 * @return {@link DockerNetworkInstance} of manage docker network.
	 * @throws Exception If fails to ensure docker network available.
	 */
	@SuppressWarnings("resource")
	public static DockerNetworkInstance ensureNetworkAvailable(String networkName) throws Exception {

		// Create the docker client
		DockerClient docker = DockerClientBuilder.getInstance().build();

		// Determine if network exists
		for (Network network : docker.listNetworksCmd().exec()) {
			if (networkName.equals(network.getName())) {
				System.out.println("Docker network " + networkName + " available");
				return new DockerNetworkInstance(networkName, network.getId(), docker);
			}
		}

		// Create the network
		System.out.println("Creating docker network " + networkName);
		CreateNetworkResponse response = docker.createNetworkCmd().withName(networkName).exec();
		return new DockerNetworkInstance(networkName, response.getId(), docker);
	}

	/**
	 * Ensures the docker container is available.
	 * 
	 * @param containerName   Name of docker container.
	 * @param imageName       Name of the docker image.
	 * @param createContainer Factory for the {@link CreateContainerCmd} if
	 *                        container not running.
	 * @return {@link DockerContainerInstance} to manage running docker container.
	 * @throws Exception If fails to ensure docker container available.
	 */
	@SuppressWarnings("resource")
	public static DockerContainerInstance ensureContainerAvailable(String containerName, String imageName,
			Function<DockerClient, CreateContainerCmd> createContainer) throws Exception {

		// Create the docker client
		DockerClient docker = DockerClientBuilder.getInstance().build();

		// Determine if container already running
		NEXT_CONTAINER: for (Container container : docker.listContainersCmd().withShowAll(true).exec()) {
			for (String name : container.getNames()) {
				if (name.equals("/" + containerName)) {

					// Ensure for correct image
					String runningContainerImage = container.getImage();
					if (!imageName.equals(runningContainerImage)) {
						throw new DockerException("Container " + containerName + " running image "
								+ runningContainerImage + " (required to be " + imageName + ")", 500);
					}

					// Ensure running
					if (!"running".equals(container.getState())) {
						// Not running, so attempt to remove
						docker.removeContainerCmd(container.getId()).exec();
						continue NEXT_CONTAINER;
					}

					// Return the running instance
					System.out.println(containerName + " already running for " + imageName);
					return new DockerContainerInstance(containerName, imageName, container.getId(), docker);
				}
			}
		}

		// Ensure image available
		pullDockerImage(imageName, docker);

		// Indicate starting docker container
		System.out.println("Starting " + imageName + " as " + containerName);

		// Create the container
		CreateContainerCmd createContainerCmd = createContainer.apply(docker);
		CreateContainerResponse createdContainer = createContainerCmd.exec();

		// Start the container
		String containerId = createdContainer.getId();
		docker.startContainerCmd(containerId).exec();

		// Provide means to shutdown container
		return new DockerContainerInstance(containerName, imageName, containerId, docker);
	}

	/**
	 * Pulls the docker image.
	 * 
	 * @param imageName Docker image name.
	 * @param docker    {@link DockerClient}.
	 * @throws Exception If fails to pull image.
	 */
	private static void pullDockerImage(String imageName, DockerClient docker) throws Exception {

		// Determine if already pulled
		if (pulledDockerImages.contains(imageName)) {
			return;
		}

		try {

			// Pull the docker image
			System.out.println("Pulling image " + imageName);
			System.out.println(); // line for progress
			int progressViewSize = 60;
			docker.pullImageCmd(imageName).exec(new PullImageResultCallback() {

				private final Map<String, Integer> items = new HashMap<>();

				@Override
				public void onNext(PullResponseItem item) {
					ProgressDetail progress = item.getProgressDetail();
					if ((progress != null) && (progress.getTotal() != null) && (progress.getCurrent() != null)) {

						// Determine progress
						long progressStart = progress.getStart() != null ? progress.getStart() : 0;
						long progressRange = progress.getTotal() - progressStart;
						long progressValue = progress.getCurrent() - progressStart;
						int progressViewCurrent = (int) ((progressValue / (double) progressRange) * progressViewSize);

						// Determine if requires update
						String id = item.getId();
						Integer previousProgress = this.items.get(id);
						if ((previousProgress == null) || (!previousProgress.equals(progressViewCurrent))) {

							// Update entry
							this.items.put(id, progressViewCurrent);

							// Build progress update
							StringBuilder entry = new StringBuilder();
							entry.append(item.getStatus() + " ");
							for (int i = 0; i < progressViewCurrent; i++) {
								entry.append("=");
							}
							for (int i = progressViewCurrent; i < progressViewSize; i++) {
								entry.append("-");
							}
							entry.append(" [" + progress.getCurrent() + "/" + progress.getTotal()
									+ "]                           ");

							// Provide progress (on same line)
							System.out.println("\033[F\r" + entry.toString());
						}
					}
					super.onNext(item);
				}
			}).awaitCompletion(10, TimeUnit.MINUTES);

			// Provide completion (on same line)
			System.out.print("\033[F\r" + "Complete");
			for (int i = 0; i < (progressViewSize * 2); i++) {
				System.out.print(" ");
			}
			System.out.println();

		} catch (Exception ex) {

			// Failed to pull image, determine if already exists
			// (typically as no connection to Internet to check)
			List<Image> images = docker.listImagesCmd().exec();
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

			// Provide warning
			System.out.println("Using existing cached image " + imageName + ", as likely offline (error: "
					+ ex.getMessage() + " - " + ex.getClass().getName() + ")");
		}

		// Flag that pulled image
		pulledDockerImages.add(imageName);
	}

}
