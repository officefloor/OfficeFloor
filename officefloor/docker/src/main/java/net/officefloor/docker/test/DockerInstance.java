package net.officefloor.docker.test;

import java.io.IOException;

import com.github.dockerjava.api.DockerClient;

/**
 * Instance of running Docker.
 * 
 * @author Daniel Sagenschneider
 */
public class DockerInstance {

	/**
	 * Name of the Docker container.
	 */
	private final String containerName;

	/**
	 * Name of the Docker image.
	 */
	private final String imageName;

	/**
	 * Identifier for the container of the docker instance.
	 */
	private final String containerId;

	/**
	 * {@link DockerClient}.
	 */
	private final DockerClient docker;

	/**
	 * Instantiate.
	 * 
	 * @param containerId Identifier for the container of the docker instance.
	 * @param docker      {@link DockerClient}.
	 */
	public DockerInstance(String containerName, String imageName, String containerId, DockerClient docker) {
		this.containerName = containerName;
		this.imageName = imageName;
		this.containerId = containerId;
		this.docker = docker;
	}

	/**
	 * <p>
	 * Shuts down the Docker container and removes it.
	 * <p>
	 * This enables a fresh Docker container for next use / test.
	 */
	public void shutdown() {
		System.out.println("Stopping " + this.imageName + " as " + this.containerName);
		this.docker.killContainerCmd(this.containerId).exec();
		this.docker.removeContainerCmd(this.containerId).exec();
		try {
			this.docker.close();
		} catch (IOException ex) {
			// Avoid checked exception
			throw new RuntimeException(ex);
		}
	}

}
