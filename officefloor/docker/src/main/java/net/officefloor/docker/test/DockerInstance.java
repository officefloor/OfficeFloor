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
