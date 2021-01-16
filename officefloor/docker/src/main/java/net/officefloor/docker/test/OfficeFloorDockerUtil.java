package net.officefloor.docker.test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.PullResponseItem;
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
	 * Ensures the docker image is available.
	 * 
	 * @param containerName   Name of docker container.
	 * @param imageName       Name of the docker image.
	 * @param createContainer Factory for the {@link CreateContainerCmd} if
	 *                        container not running.
	 * @return {@link DockerInstance} to manage running docker container.
	 * @throws Exception If fails to ensure docker container available.
	 */
	@SuppressWarnings("resource")
	public static DockerInstance ensureAvailable(String containerName, String imageName,
			Function<DockerClient, CreateContainerCmd> createContainer) throws Exception {

		// Create the docker client
		DockerClient docker = DockerClientBuilder.getInstance().build();

		// Determine if container already running
		for (Container container : docker.listContainersCmd().exec()) {
			for (String name : container.getNames()) {
				if (name.equals("/" + containerName)) {

					// Ensure for correct image
					String runningContainerImage = container.getImage();
					if (!imageName.equals(runningContainerImage)) {
						throw new DockerException("Container " + containerName + " running image "
								+ runningContainerImage + " (required to be " + imageName + ")", 500);
					}

					// Return the running instance
					System.out.println(containerName + " already running for " + imageName);
					return new DockerInstance(containerName, imageName, container.getId(), docker);
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
		return new DockerInstance(containerName, imageName, containerId, docker);
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
			docker.pullImageCmd(imageName).exec(new PullImageResultCallback() {

				@Override
				public void onNext(PullResponseItem item) {
					if (item.getProgressDetail() != null) {
						System.out.println(item.getProgressDetail());
					}
					super.onNext(item);
				}
			}).awaitCompletion(10, TimeUnit.MINUTES);

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