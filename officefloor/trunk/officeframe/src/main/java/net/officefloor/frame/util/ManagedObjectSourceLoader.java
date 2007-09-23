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

import java.util.Properties;

import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.WorkBuilder;
import net.officefloor.frame.api.execute.Handler;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.ResourceLocator;

/**
 * Loads
 * {@link net.officefloor.frame.spi.managedobject.source.ManagedObjectSource}
 * for stand-alone use.
 * 
 * @author Daniel
 */
public class ManagedObjectSourceLoader {

	/**
	 * {@link Properties}.
	 */
	private final Properties properties = new Properties();

	/**
	 * {@link ResourceLocator}.
	 */
	private ResourceLocator resourceLocator = null;

	/**
	 * Default constructor.
	 */
	public ManagedObjectSourceLoader() {
		super();
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
	public <MS extends ManagedObjectSource> MS loadManagedObjectSource(
			Class<MS> managedObjectSourceClass) throws Exception {

		// Create a new instance of the managed object source
		MS moSource = managedObjectSourceClass.newInstance();

		// Initialise the managed object source
		moSource.init(new LoadSourceContext());

		// Start the managed object source
		moSource.start(new LoadExecuteContext());

		// Return the loaded managed object source
		return moSource;
	}

	/**
	 * {@link ManagedObjectSourceContext}.
	 */
	private class LoadSourceContext implements ManagedObjectSourceContext {

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext#getProperties()
		 */
		public Properties getProperties() {
			return properties;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext#getResourceLocator()
		 */
		public ResourceLocator getResourceLocator() {
			return resourceLocator;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext#getRecycleWorkBuilder(java.lang.Class)
		 */
		public <W extends Work> WorkBuilder<W> getRecycleWorkBuilder(
				Class<W> typeOfWork) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("TODO implement");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext#getManagedObjectBuilder()
		 */
		public ManagedObjectBuilder<?> getManagedObjectBuilder() {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("TODO implement");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext#getOfficeBuilder()
		 */
		public OfficeBuilder getOfficeBuilder() {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("TODO implement");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext#getOfficeFrame()
		 */
		public OfficeFrame getOfficeFrame() {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("TODO implement");
		}

	}

	/**
	 * {@link ManagedObjectExecuteContext}.
	 */
	private class LoadExecuteContext<H extends Enum<H>> implements ManagedObjectExecuteContext<H> {

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
