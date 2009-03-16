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
package net.officefloor.frame.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.execute.Handler;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.impl.construct.managedobjectsource.ManagedObjectSourceContextImpl;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ResourceLocator;

/**
 * Loads {@link ManagedObjectSource} for stand-alone use.
 * 
 * @author Daniel
 */
public class ManagedObjectSourceLoader {

	/**
	 * Name of the {@link ManagedObjectSource} being loaded.
	 */
	public static final String STAND_ALONE_MANAGED_OBJECT_SOURCE_NAME = "managed.object.source";

	/**
	 * Name of the {@link Office} managing the {@link ManagedObjectSource} being
	 * loaded.
	 */
	public static final String STAND_ALONE_MANAGING_OFFICE_NAME = "office";

	/**
	 * {@link Properties}.
	 */
	private final Properties properties = new Properties();

	/**
	 * {@link ResourceLocator}. Defaults to use the system class loader to find
	 * resources and if not on class path goes looking as a {@link File}.
	 */
	private ResourceLocator resourceLocator = new ResourceLocator() {

		@Override
		public InputStream locateInputStream(String name) {

			// Find first by system class loader
			InputStream inputStream = ClassLoader
					.getSystemResourceAsStream(name);
			if (inputStream != null) {
				return inputStream;
			}

			// Find by file paths
			File file = this.getFile(name);
			if (file != null) {
				try {
					return new FileInputStream(file);
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}

			// File not found
			return null;
		}

		@Override
		public URL locateURL(String name) {
			// Only find by class path
			return ClassLoader.getSystemResource(name);
		}

		/**
		 * Obtains the {@link File} for the name.
		 * 
		 * @param name
		 *            Name of the file.
		 * @return {@link File} or <code>null</code> if not found.
		 */
		private File getFile(String name) {

			// Obtain the current directory
			File currentDirectory = new File(".");

			// Create the listing of paths to find the file
			List<File> paths = new LinkedList<File>();

			// Absolute and relative
			paths.add(new File(name));
			paths.add(new File(currentDirectory, name));

			// Maven locations to be searched
			paths.add(new File(
					new File(currentDirectory, "target/test-classes"), name));
			paths.add(new File(new File(currentDirectory, "target/classes"),
					name));
			paths.add(new File(new File(currentDirectory, "target"), name));

			// Obtain the file
			for (File path : paths) {
				if (path.exists()) {
					return path;
				}
			}

			// File not found if here
			return null;
		}
	};

	/**
	 * Default constructor.
	 */
	public ManagedObjectSourceLoader() {
	}

	/**
	 * Adds a property for the {@link ManagedObjectSource}.
	 * 
	 * @param name
	 *            Name of the property.
	 * @param value
	 *            Value for the property.
	 */
	public void addProperty(String name, String value) {
		this.properties.setProperty(name, value);
	}

	/**
	 * Specifies the {@link ResourceLocator}.
	 * 
	 * @param resourceLocator
	 *            {@link ResourceLocator}.
	 */
	public void setResourceLocator(ResourceLocator resourceLocator) {
		this.resourceLocator = resourceLocator;
	}

	/**
	 * Instantiates and initialises the {@link ManagedObjectSource}.
	 * 
	 * @param managedObjectSourceClass
	 *            Class of the {@link ManagedObjectSource}.
	 * @return Initialised {@link ManagedObjectSource}.
	 * @throws Exception
	 *             If fails to initialise {@link ManagedObjectSource}.
	 */
	@SuppressWarnings("unchecked")
	public <D extends Enum<D>, H extends Enum<H>, MS extends ManagedObjectSource<D, H>> MS initManagedObjectSource(
			Class<MS> managedObjectSourceClass) throws Exception {

		// Create a new instance of the managed object source
		MS moSource = managedObjectSourceClass.newInstance();

		// Create necessary builders
		OfficeFloorBuilder officeFloorBuilder = OfficeFrame
				.createOfficeFloorBuilder();
		ManagedObjectBuilder<H> managedObjectBuilder = officeFloorBuilder
				.addManagedObject(STAND_ALONE_MANAGED_OBJECT_SOURCE_NAME,
						managedObjectSourceClass);
		OfficeBuilder officeBuilder = officeFloorBuilder
				.addOffice(STAND_ALONE_MANAGING_OFFICE_NAME);

		// Initialise the managed object source
		ManagedObjectSourceContextImpl sourceContext = new ManagedObjectSourceContextImpl(
				STAND_ALONE_MANAGED_OBJECT_SOURCE_NAME, this.properties,
				this.resourceLocator, managedObjectBuilder, officeBuilder);
		moSource.init(sourceContext);

		// Return the initialised managed object source
		return moSource;
	}

	/**
	 * Starts the {@link ManagedObjectSource}.
	 * 
	 * @param managedObjectSource
	 *            {@link ManagedObjectSource}.
	 * @throws Exception
	 *             If fails to start the {@link ManagedObjectSource}.
	 */
	@SuppressWarnings("unchecked")
	public <D extends Enum<D>, H extends Enum<H>, MS extends ManagedObjectSource<D, H>> void startManagedObjectSource(
			MS managedObjectSource) throws Exception {
		// Start the managed object source
		managedObjectSource.start(new LoadExecuteContext());
	}

	/**
	 * Loads (init and start) the {@link ManagedObjectSource}.
	 * 
	 * @return Loaded {@link ManagedObjectSource}.
	 * @throws Exception
	 *             If fails to init and start the {@link ManagedObjectSource}.
	 */
	public <D extends Enum<D>, H extends Enum<H>, MS extends ManagedObjectSource<D, H>> MS loadManagedObjectSource(
			Class<MS> managedObjectSourceClass) throws Exception {

		// Initialise the managed object source
		MS moSource = this.initManagedObjectSource(managedObjectSourceClass);

		// Start the managed object source
		this.startManagedObjectSource(moSource);

		// Return the loaded managed object source
		return moSource;
	}

	/**
	 * {@link ManagedObjectExecuteContext}.
	 */
	private class LoadExecuteContext<H extends Enum<H>> implements
			ManagedObjectExecuteContext<H> {

		@Override
		public Handler<?> getHandler(H key) {
			throw new UnsupportedOperationException(
					"Managed Object Source may not use Handlers when running stand-alone");
		}
	}

}
