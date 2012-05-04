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

import net.officefloor.autowire.AutoWireOfficeFloor;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.plugin.web.http.application.WebAutoWireApplication;
import net.officefloor.plugin.web.http.resource.source.SourceHttpResourceFactory;
import net.officefloor.plugin.woof.WoofOfficeFloorSource;

import com.google.gwt.core.ext.ServletContainer;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.UnableToCompleteException;

/**
 * WoOF {@link ServletContainer}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofServletContainer extends ServletContainer {

	/**
	 * {@link TreeLogger}.
	 */
	private final TreeLogger logger;

	/**
	 * Port.
	 */
	private final int port;

	/**
	 * Resource directories.
	 */
	private final File[] resourceDirectories;

	/**
	 * {@link AutoWireOfficeFloor}.
	 */
	private AutoWireOfficeFloor officeFloor;

	/**
	 * Initiate.
	 * 
	 * @param logger
	 *            {@link TreeLogger}.
	 * @param port
	 *            Port.
	 * @param resourceDirectories
	 *            Resource directories.
	 * @throws Exception
	 *             If fails to open {@link OfficeFloor}.
	 */
	public WoofServletContainer(TreeLogger logger, int port,
			File[] resourceDirectories) throws Exception {
		this.logger = logger;
		this.port = port;
		this.resourceDirectories = resourceDirectories;

		// Start
		this.start();
	}

	/**
	 * <p>
	 * Creates the {@link WebAutoWireApplication}.
	 * <p>
	 * Allows overriding to provide alternate implementation.
	 * 
	 * @return {@link WebAutoWireApplication}.
	 */
	protected WebAutoWireApplication createWebAutoWireApplication() {
		return new WoofOfficeFloorSource();
	}

	/**
	 * Starts WoOF.
	 * 
	 * @throws Exception
	 *             If fails to start.
	 */
	public void start() throws Exception {

		// Create WoOF
		WebAutoWireApplication source = this.createWebAutoWireApplication();
		final OfficeFloorCompiler compiler = source.getOfficeFloorCompiler();

		// Configure port
		compiler.addProperty(WoofOfficeFloorSource.PROPERTY_HTTP_PORT,
				String.valueOf(port));

		// Configure resource directories
		SourceHttpResourceFactory.loadProperties(null,
				this.resourceDirectories, null, Boolean.FALSE,
				new SourceHttpResourceFactory.PropertyTarget() {
					@Override
					public void addProperty(String name, String value) {
						compiler.addProperty(name, value);
					}
				});

		try {
			// Open the OfficeFloor
			this.officeFloor = source.openOfficeFloor();

		} catch (Throwable ex) {
			// Log failure
			this.logger.log(Type.ERROR, ex.getMessage(), ex);
		}
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

		// Stop
		this.stop();

		// Start
		try {
			this.start();

		} catch (Exception ex) {
			// Log failure
			this.logger.log(Type.ERROR, "Failed to re-start WoOF", ex);

			// Indicate failure
			throw new UnableToCompleteException();
		}
	}

	@Override
	public void stop() throws UnableToCompleteException {

		// Close OfficeFloor if running (stopping container)
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
			this.officeFloor = null; // clear reference
		}
	}

}