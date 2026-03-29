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

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.CreateNetworkResponse;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.model.BuildResponseItem;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.Network;
import com.github.dockerjava.api.model.PullResponseItem;
import com.github.dockerjava.api.model.ResponseItem.ProgressDetail;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;

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
	 * <p>
	 * Obtains the image name qualified with appropriate tag name.
	 * <p>
	 * This allows overriding the default tag name with a configured file on the
	 * class path for the image name.
	 * 
	 * @param imageName     Name of the image.
	 * @param defaulTagName Default tag name if no overriding configuration.
	 * @return Qualified image name.
	 * @throws Exception If fails to load configured tag name.
	 */
	public static String getImageQualifiedName(String imageName, String defaulTagName) throws Exception {

		// Determine the tag name
		String tagName = defaulTagName;

		// Determine if configured tag name
		Class<?> officeFloorDockerUtilClass = OfficeFloorDockerUtil.class;
		String packagePrefix = officeFloorDockerUtilClass.getPackageName().replace('.', '/');
		String overrideResourceName = packagePrefix + "/" + imageName;
		InputStream overrideResourceInput = officeFloorDockerUtilClass.getClassLoader()
				.getResourceAsStream(overrideResourceName);
		if (overrideResourceInput != null) {

			// Load the configured tag name
			Reader overrideResourceReader = new InputStreamReader(overrideResourceInput);
			StringWriter overrideTag = new StringWriter();
			for (int character = overrideResourceReader.read(); character != -1; character = overrideResourceReader
					.read()) {
				overrideTag.write(character);
			}

			// Specify the tag name
			tagName = overrideTag.toString().trim();
		}

		// Return the qualified name
		return imageName + ":" + tagName;
	}

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
		DockerClient docker = getDockerClient();

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
	 * Factory to create the build directory.
	 */
	@FunctionalInterface
	public static interface BuildDirectoryFactory {

		/**
		 * Creates the build directory.
		 * 
		 * @return Build directory.
		 * @throws Exception If fails to create the build directory.
		 */
		File createBuildDirectory() throws Exception;
	}

	/**
	 * Ensures the image is available. If not, will build image from input build
	 * directory.
	 * 
	 * @param imageName             Name of image to check exists or build.
	 * @param defaultTagName        Default tag name.
	 * @param buildDirectoryFactory {@link BuildDirectoryFactory} to use if image
	 *                              not built.
	 */
	@SuppressWarnings("resource")
	public static void ensureImageAvailable(String imageName, String defaultTagName,
			BuildDirectoryFactory buildDirectoryFactory) throws Exception {

		// Obtain the image name
		String qualifiedImageName = getImageQualifiedName(imageName, defaultTagName);

		// Create the docker client
		try (DockerClient docker = getDockerClient()) {

			// Determine if image already available
			for (Image image : docker.listImagesCmd().withShowAll(true).exec()) {
				String[] repoTags = image.getRepoTags();
				if (repoTags != null) {
					for (String tag : repoTags) {
						if (qualifiedImageName.equals(tag)) {
							return; // image already built and available
						}
					}
				}
			}

			// Build the image
			File buildDir = buildDirectoryFactory.createBuildDirectory();
			System.out
					.println("Building image " + qualifiedImageName + " from directory " + buildDir.getAbsolutePath());
			BuildImageResultCallback result = docker.buildImageCmd(buildDir)
					.withTags(new HashSet<>(Arrays.asList(qualifiedImageName))).exec(new BuildImageResultCallback() {
						@Override
						public void onNext(BuildResponseItem item) {

							// Log progress of build
							String stream = item.getStream();
							if (stream != null) {
								System.out.print(stream);
							}

							// Undertake default actions
							super.onNext(item);
						}
					});
			result.awaitCompletion(10, TimeUnit.MINUTES);
			System.out.println(); // flush any remaining output
		}
	}

	/**
	 * Ensures the docker container is available.
	 * 
	 * @param containerName   Name of docker container.
	 * @param imageName       Name of the docker image.
	 * @param defaultTagName  Default tag name.
	 * @param createContainer Factory for the {@link CreateContainerCmd} if
	 *                        container not running.
	 * @return {@link DockerContainerInstance} to manage running docker container.
	 * @throws Exception If fails to ensure docker container available.
	 */
	@SuppressWarnings("resource")
	public static DockerContainerInstance ensureContainerAvailable(String containerName, String imageName,
			String defaultTagName, BiFunction<DockerClient, String, CreateContainerCmd> createContainer)
			throws Exception {

		// Obtain the image name
		String qualifiedImageName = getImageQualifiedName(imageName, defaultTagName);

		// Create the docker client
		DockerClient docker = getDockerClient();

		// Determine if container already running
		NEXT_CONTAINER: for (Container container : docker.listContainersCmd().withShowAll(true).exec()) {
			for (String name : container.getNames()) {
				if (name.equals("/" + containerName)) {

					// Ensure for correct image
					String runningContainerImage = container.getImage();
					if (!qualifiedImageName.equals(runningContainerImage)) {
						throw new DockerException("Container " + containerName + " running image "
								+ runningContainerImage + " (required to be " + qualifiedImageName + ")", 500);
					}

					// Ensure running
					if (!"running".equals(container.getState())) {
						// Not running, so attempt to remove
						docker.removeContainerCmd(container.getId()).exec();
						continue NEXT_CONTAINER;
					}

					// Return the running instance
					System.out.println(containerName + " already running for " + qualifiedImageName);
					return new DockerContainerInstance(containerName, qualifiedImageName, container.getId(), docker);
				}
			}
		}

		// Ensure image available
		pullDockerImage(qualifiedImageName, docker);

		// Indicate starting docker container
		System.out.println("Starting " + qualifiedImageName + " as " + containerName);

		// Create the container (and always run as user)
		CreateContainerCmd createContainerCmd = createContainer.apply(docker, qualifiedImageName);
		CreateContainerResponse createdContainer = createContainerCmd.exec();

		// Start the container
		String containerId = createdContainer.getId();
		docker.startContainerCmd(containerId).exec();

		// Provide means to shutdown container
		return new DockerContainerInstance(containerName, qualifiedImageName, containerId, docker);
	}

	/**
	 * Obtains the {@link DockerClient}.
	 * 
	 * @return {@link DockerClient}.
	 */
	private static DockerClient getDockerClient() {
		DockerClientConfig dockerConfig = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
		DockerHttpClient dockerHttpClient = new ApacheDockerHttpClient.Builder()
				.dockerHost(dockerConfig.getDockerHost()).sslConfig(dockerConfig.getSSLConfig()).maxConnections(100)
				.connectionTimeout(Duration.ofSeconds(30)).responseTimeout(Duration.ofSeconds(45)).build();
		return DockerClientImpl.getInstance(dockerConfig, dockerHttpClient);
	}

	/**
	 * Pulls the docker image.
	 * 
	 * @param qualifiedImageName Docker qualified image name.
	 * @param docker             {@link DockerClient}.
	 * @throws Exception If fails to pull image.
	 */
	private static void pullDockerImage(String qualifiedImageName, DockerClient docker) throws Exception {

		// Determine if already pulled
		if (pulledDockerImages.contains(qualifiedImageName)) {
			return;
		}

		try {

			// Pull the docker image
			System.out.println("Pulling image " + qualifiedImageName);
			System.out.println(); // line for progress
			int progressViewSize = 60;
			docker.pullImageCmd(qualifiedImageName).exec(new PullImageResultCallback() {

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
						if (qualifiedImageName.equals(tag)) {
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
			System.out.println("Using existing cached image " + qualifiedImageName + ", as likely offline (error: "
					+ ex.getMessage() + " - " + ex.getClass().getName() + ")");
		}

		// Flag that pulled image
		pulledDockerImages.add(qualifiedImageName);
	}

}
