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
import net.officefloor.frame.api.execute.Handler;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.ManagedObjectSourceContextImpl;
import net.officefloor.frame.impl.construct.WorkBuilderImpl;
import net.officefloor.frame.spi.managedobject.ManagedObject;
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
	 * {@link WorkBuilderImpl} containing the recycle details.
	 */
	private WorkBuilderImpl<? extends Work> recycle = null;

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
	 * Loads and returns the {@link ManagedObjectSource}.
	 * 
	 * @return Loaded {@link ManagedObjectSource}.
	 */
	@SuppressWarnings("unchecked")
	public <D extends Enum<D>, H extends Enum<H>, MS extends ManagedObjectSource<D, H>> MS loadManagedObjectSource(
			Class<MS> managedObjectSourceClass) throws Exception {

		// Create a new instance of the managed object source
		MS moSource = managedObjectSourceClass.newInstance();

		final String MANAGED_OBJECT_SOURCE_NAME = "mos";

		// Create necessary builders
		OfficeFrame officeFrame = OfficeFrame.getInstance();
		OfficeBuilder officeBuilder = officeFrame.getBuilderFactory()
				.createOfficeBuilder();
		ManagedObjectBuilder<H> managedObjectBuilder = officeFrame
				.getBuilderFactory().createManagedObjectBuilder(
						managedObjectSourceClass);

		// Initialise the managed object source
		ManagedObjectSourceContextImpl sourceContext = new ManagedObjectSourceContextImpl(
				MANAGED_OBJECT_SOURCE_NAME, this.properties,
				this.resourceLocator, managedObjectBuilder, officeBuilder,
				officeFrame);
		moSource.init(sourceContext);

		// Start the managed object source
		moSource.start(new LoadExecuteContext());

		// Return the loaded managed object source
		return moSource;
	}

	/**
	 * Recycle the {@link ManagedObject}.
	 * 
	 * @param managedObject
	 *            {@link ManagedObject} to be recycled.
	 */
	public void recycleManagedObject(ManagedObject managedObject) {

		// Ensure able to recycle
		if (this.recycle == null) {
			return;
		}

		// TODO implement
		throw new UnsupportedOperationException(
				"TODO implement recycling managed object in stand alone");
	}

	/**
	 * {@link ManagedObjectExecuteContext}.
	 */
	private class LoadExecuteContext<H extends Enum<H>> implements
			ManagedObjectExecuteContext<H> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext#getHandler(H)
		 */
		public Handler<?> getHandler(H key) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("TODO implement");
		}

	}

}
