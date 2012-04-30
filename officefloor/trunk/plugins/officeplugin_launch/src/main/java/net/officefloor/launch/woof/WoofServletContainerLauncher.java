/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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
package net.officefloor.launch.woof;

import java.io.File;
import java.net.BindException;

import com.google.gwt.core.ext.ServletContainer;
import com.google.gwt.core.ext.ServletContainerLauncher;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;

/**
 * WoOF {@link ServletContainerLauncher}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofServletContainerLauncher extends ServletContainerLauncher {

	/*
	 * ==================== ServletContainerLauncher =======================
	 */

	@Override
	public ServletContainer start(TreeLogger logger, int port, File appRootDir)
			throws BindException, Exception {

		// TODO remove
		System.out.println("Starting WoOF container on port " + port + " ...");

		// Create and return the WoOF container
		return new WoofServletContainer(port);
	}

	/**
	 * Woof {@link ServletContainer}.
	 */
	private static class WoofServletContainer extends ServletContainer {

		/**
		 * Port.
		 */
		private final int port;

		/**
		 * Initiate.
		 * 
		 * @param port
		 *            Port.
		 */
		public WoofServletContainer(int port) {
			this.port = port;
		}

		/*
		 * ====================== ServletContainer ==========================
		 */

		@Override
		public int getPort() {
			return this.port;
		}

		@Override
		public void refresh() throws UnableToCompleteException {
			// TODO implement ServletContainer.refresh
			throw new UnsupportedOperationException(
					"TODO implement ServletContainer.refresh");
		}

		@Override
		public void stop() throws UnableToCompleteException {
			// TODO implement ServletContainer.stop
			throw new UnsupportedOperationException(
					"TODO implement ServletContainer.stop");
		}
	}

}