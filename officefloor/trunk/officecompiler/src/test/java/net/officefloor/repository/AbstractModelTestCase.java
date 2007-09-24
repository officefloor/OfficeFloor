/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.repository;

import java.io.File;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.repository.ConfigurationContext;
import net.officefloor.repository.ModelRepository;
import net.officefloor.repository.filesystem.FileSystemConfigurationContext;

/**
 * Abstract test for a model.
 * 
 * @author Daniel
 */
public abstract class AbstractModelTestCase extends OfficeFrameTestCase {

	/**
	 * {@link ConfigurationContext}.
	 */
	private ConfigurationContext context;

	/**
	 * {@link ModelRepository}.
	 */
	private ModelRepository repository;

	/**
	 * Initiate the file system model repository.
	 */
	protected void setUp() throws Exception {
		// Initiate the root directory
		File rootDir = new File(".", "temp");
		if (rootDir.exists()) {
			rootDir.delete();
		}
		rootDir.mkdirs();

		// Create the model repository
		this.context = new FileSystemConfigurationContext(rootDir);
		this.repository = new ModelRepository();
	}

	/**
	 * Obtains the {@link ConfigurationContext}.
	 * 
	 * @return {@link ConfigurationContext}.
	 */
	protected ConfigurationContext getConfigurationContext() {
		return this.context;
	}

	/**
	 * Obtains the {@link ModelRepository}.
	 * 
	 * @return {@link ModelRepository}.
	 */
	protected ModelRepository getModelRepository() {
		return this.repository;
	}

}
