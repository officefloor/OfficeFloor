/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.eclipse.bootstrap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Boot straps {@link Bootable} implementations.
 * 
 * @author Daniel Sagenschneider
 */
public class Bootstrap implements Runnable {

	/**
	 * Bootstraps the {@link Bootable}.
	 * 
	 * @param bootableClassName
	 *            Class name of the {@link Bootable} implementation.
	 * @param arguments
	 *            Arguments for the {@link Bootable}.
	 * @param classLoader
	 *            {@link ClassLoader} to boot strap the {@link Bootable}
	 *            implementation.
	 * @throws Throwable
	 *             If fails to boot strap the {@link Bootable}.
	 */
	public static void bootstrap(String bootableClassName,
			Map<String, String> arguments, ClassLoader classLoader)
			throws Throwable {

		// Create the bootstrap class loader
		BootstrapClassLoader bootstrapClassLoader = new BootstrapClassLoader(
				classLoader);

		// Create the bootable implementation
		Object instance = bootstrapClassLoader.loadClass(bootableClassName)
				.newInstance();

		// Obtain the method to boot
		Method bootMethod = instance.getClass().getMethod(
				Bootable.BOOT_METHOD_NAME, Map.class);

		// Create the boot strap in its own thread
		Bootstrap bootstrap = new Bootstrap(instance, bootMethod, arguments);
		Thread thread = new Thread(bootstrap);
		thread.setContextClassLoader(bootstrapClassLoader);

		// Bootstrap the implementation and wait for completion
		thread.start();
		bootstrap.waitToComplete();
	}

	/**
	 * {@link Bootable} implementation.
	 */
	private final Object bootable;

	/**
	 * {@link Method} to boot the {@link Bootable} implementation.
	 */
	private final Method bootMethod;

	/**
	 * Arguments for the {@link Bootable}.
	 */
	private final Map<String, String> arguments;

	/**
	 * Indicate if complete.
	 */
	private boolean isComplete = false;

	/**
	 * Failure of boot strap.
	 */
	private Throwable failure;

	/**
	 * Initiate.
	 * 
	 * @param bootable
	 *            {@link Bootable} instance.
	 * @param bootMethod
	 *            {@link Method} to boot the {@link Bootable} implementation.
	 * @param arguments
	 *            Arguments for the {@link Bootable}.
	 */
	public Bootstrap(Object bootable, Method bootMethod,
			Map<String, String> arguments) {
		this.bootable = bootable;
		this.bootMethod = bootMethod;
		this.arguments = arguments;
	}

	/**
	 * Wait to be complete.
	 * 
	 * @throws Throwable
	 *             If boot strap fails.
	 */
	public synchronized void waitToComplete() throws Throwable {
		// Determine if require waiting
		if (!this.isComplete) {
			this.wait();
		}

		// Determine if failure in running
		if (this.failure != null) {
			throw this.failure;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public synchronized void run() {
		try {

			// Boot the bootable
			this.bootMethod.invoke(this.bootable, this.arguments);

		} catch (InvocationTargetException ex) {
			// Indicate cause of invocation failure
			this.failure = ex.getCause();

		} catch (Throwable ex) {
			// Indicate failure
			this.failure = ex;

		} finally {
			// Indicate complete
			this.isComplete = true;
			this.notify();
		}
	}

}
