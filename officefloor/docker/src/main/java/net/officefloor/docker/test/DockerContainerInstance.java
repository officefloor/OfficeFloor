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

import java.io.Closeable;
import java.io.IOException;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.exception.ConflictException;
import com.github.dockerjava.api.model.Frame;

/**
 * Instance of running Docker.
 * 
 * @author Daniel Sagenschneider
 */
public class DockerContainerInstance implements AutoCloseable {

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
	 * Indicates if closed.
	 */
	private boolean isClosed = false;

	/**
	 * Instantiate.
	 * 
	 * @param containerId Identifier for the container of the docker instance.
	 * @param docker      {@link DockerClient}.
	 */
	public DockerContainerInstance(String containerName, String imageName, String containerId, DockerClient docker) {
		this.containerName = containerName;
		this.imageName = imageName;
		this.containerId = containerId;
		this.docker = docker;
	}

	/**
	 * Undertakes connection to the Docker container instance.
	 * 
	 * @param <R>     Result of connection.
	 * @param <T>     Possible failure in connection.
	 * @param connect {@link DockerConnectOperation}.
	 * @return Result of connection.
	 * @throws T Possible failure in connection.
	 */
	@SuppressWarnings("unchecked")
	public <R, T extends Throwable> R connectToDockerInstance(DockerConnectOperation<R, T> connect) throws T {
		try {
			// Undertake connection
			return connect.connect();

		} catch (Throwable ex) {

			// Failed to connect, so log details of the container
			System.err.println("Failed to connect to Docker");
			boolean[] isComplete = new boolean[] { false };
			this.docker.logContainerCmd(this.containerId).withStdOut(true).withStdErr(true)
					.exec(new ResultCallback<Frame>() {

						@Override
						public void onStart(Closeable closeable) {
							// Will not cancel
						}

						@Override
						public void onNext(Frame frame) {
							System.err.println(frame.toString());
						}

						@Override
						public void onError(Throwable throwable) {
							throwable.printStackTrace();
							this.complete();
						}

						@Override
						public void onComplete() {
							this.complete();
						}

						@Override
						public void close() throws IOException {
							this.complete();
						}

						/**
						 * Indicates complete.
						 */
						private void complete() {
							synchronized (isComplete) {
								isComplete[0] = true;
								isComplete.notifyAll();
							}
						}
					});

			// Wait until complete (or times out)
			long endTime = (System.currentTimeMillis() + 60_000);
			synchronized (isComplete) {
				while (!isComplete[0]) {

					// Determine if timed out
					if (System.currentTimeMillis() > endTime) {
						throw new RuntimeException("Took too long to obtain Docker logs");
					}

					// Wait some time
					try {
						isComplete.wait(10);
					} catch (InterruptedException interrupted) {
						// Interrupted, so consider complete
						isComplete[0] = true;
					}
				}
			}

			// Propagate the exception
			throw (T) ex;
		}
	}

	/*
	 * =================== AutoCloseable ===================
	 */

	@Override
	public synchronized void close() {

		// Determine if already closed
		if (this.isClosed) {
			return;
		}
		this.isClosed = true; // consider closed

		// Undertake close
		System.out.println("Stopping " + this.imageName + " as " + this.containerName);
		try {
			this.docker.killContainerCmd(this.containerId).exec();
		} catch (ConflictException ex) {
			// Likely container not running (carry on to attempt to remove)
			System.out.println("Failed to stop docker container " + this.containerName + " (image " + this.imageName
					+ "): " + ex.getMessage());
		}
		this.docker.removeContainerCmd(this.containerId).exec();
		try {
			this.docker.close();
		} catch (IOException ex) {
			// Avoid checked exception
			throw new RuntimeException(ex);
		}
	}

}
