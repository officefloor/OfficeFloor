package net.officefloor.docker.test;

import java.io.IOException;

import com.github.dockerjava.api.DockerClient;

/**
 * Docker network.
 * 
 * @author Daniel Sagenschneider
 */
public class DockerNetworkInstance implements AutoCloseable {

	/**
	 * Name of the network.
	 */
	private final String networkName;

	/**
	 * Identifier of the network.
	 */
	private final String networkId;

	/**
	 * {@link DockerClient}.
	 */
	private final DockerClient docker;

	/**
	 * Indicates if closed.
	 */
	private boolean isClosed = false;

	/**
	 * Instantiate.
	 * 
	 * @param networkName Name of the network.
	 * @param networkId   Identifier of the network.
	 * @param docker      {@link DockerClient}.
	 */
	public DockerNetworkInstance(String networkName, String networkId, DockerClient docker) {
		this.networkName = networkName;
		this.networkId = networkId;
		this.docker = docker;
	}

	/*
	 * ===================== AutoCloseable ======================
	 */

	@Override
	public synchronized void close() {

		// Determine if already closed
		if (this.isClosed) {
			return;
		}
		this.isClosed = true; // consider closed

		// Undertake close
		System.out.println("Removing docker network " + this.networkName);
		this.docker.removeNetworkCmd(this.networkId).exec();
		try {
			this.docker.close();
		} catch (IOException ex) {
			// Avoid checked exception
			throw new RuntimeException(ex);
		}
	}

}