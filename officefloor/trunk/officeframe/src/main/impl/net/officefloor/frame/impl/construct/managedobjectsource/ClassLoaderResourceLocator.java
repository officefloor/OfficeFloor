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
package net.officefloor.frame.impl.construct.managedobjectsource;

import java.io.InputStream;
import java.net.URL;

import net.officefloor.frame.spi.managedobject.source.ResourceLocator;

/**
 * Implementation of {@link ResourceLocator} that relies on a
 * {@link ClassLoader}.
 * 
 * @author Daniel
 */
public class ClassLoaderResourceLocator implements ResourceLocator {

	/**
	 * {@link ClassLoader} to locate resources.
	 */
	private final ClassLoader classLoader;

	/**
	 * Initiate.
	 */
	public ClassLoaderResourceLocator() {
		this.classLoader = this.getClass().getClassLoader();
	}

	/**
	 * Initiate.
	 * 
	 * @param classLoader
	 *            {@link ClassLoader} to locate resources.
	 */
	public ClassLoaderResourceLocator(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	/*
	 * =================== ResourceLocator ===============================
	 */

	@Override
	public InputStream locateInputStream(String name) {
		return this.classLoader.getResourceAsStream(name);
	}

	@Override
	public URL locateURL(String name) {
		return this.classLoader.getResource(name);
	}

}