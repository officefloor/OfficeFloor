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
