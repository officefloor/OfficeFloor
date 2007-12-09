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
package net.officefloor.repository.classloader;

import java.io.InputStream;

import net.officefloor.repository.ConfigurationContext;
import net.officefloor.repository.ConfigurationItem;

/**
 * {@link ConfigurationItem} for the {@link ClassLoader}.
 * 
 * @author Daniel
 */
public class ClassLoaderConfigurationItem implements ConfigurationItem {

	/**
	 * Id.
	 */
	private final String id;

	/**
	 * {@link InputStream}.
	 */
	private final InputStream inputStream;

	/**
	 * {@link ConfigurationContext}.
	 */
	private final ConfigurationContext context;

	/**
	 * Initiate.
	 * 
	 * @param id
	 *            Id.
	 * @param inputStream
	 *            {@link InputStream} of the resource.
	 * @param context
	 *            {@link ConfigurationContext}.
	 */
	public ClassLoaderConfigurationItem(String id, InputStream inputStream,
			ConfigurationContext context) {
		this.id = id;
		this.inputStream = inputStream;
		this.context = context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.repository.ConfigurationItem#getId()
	 */
	@Override
	public String getId() {
		return this.id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.repository.ConfigurationItem#getConfiguration()
	 */
	@Override
	public InputStream getConfiguration() throws Exception {
		return this.inputStream;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.repository.ConfigurationItem#setConfiguration(java.io.InputStream)
	 */
	@Override
	public void setConfiguration(InputStream configuration) throws Exception {
		throw new UnsupportedOperationException(
				"Can not change item on class path for a "
						+ this.getClass().getSimpleName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.repository.ConfigurationItem#getContext()
	 */
	@Override
	public ConfigurationContext getContext() {
		return this.context;
	}

}
