/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
package net.officefloor.frame.impl.construct.source;

import java.io.InputStream;

import net.officefloor.frame.spi.source.ResourceSource;
import net.officefloor.frame.spi.source.SourceContext;
import net.officefloor.frame.spi.source.SourceProperties;
import net.officefloor.frame.spi.source.UnknownClassError;
import net.officefloor.frame.spi.source.UnknownResourceError;

/**
 * {@link SourceContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class SourceContextImpl extends SourcePropertiesImpl implements
		SourceContext {

	/**
	 * Delegate {@link SourceContext}.
	 */
	private final SourceContext delegate;

	/**
	 * Initiate the raw {@link SourceContext} to seed other
	 * {@link SourceContext} instances.
	 * 
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @param resourceSources
	 *            {@link ResourceSource} instances.
	 */
	public SourceContextImpl(ClassLoader classLoader,
			ResourceSource... resourceSources) {
		this.delegate = new DelegateSourceContext(classLoader, resourceSources);
	}

	/**
	 * Initiate specific {@link SourceContext} with necessary
	 * {@link SourceProperties}.
	 * 
	 * @param delegate
	 *            Delegate {@link SourceContext}.
	 * @param sourceProperties
	 *            {@link SourceProperties}.
	 */
	public SourceContextImpl(SourceContext delegate,
			SourceProperties sourceProperties) {
		super(sourceProperties);
		this.delegate = delegate;
	}

	/*
	 * ================== SourceContext =======================
	 */

	@Override
	public Class<?> loadOptionalClass(String name) {
		return this.delegate.loadOptionalClass(name);
	}

	@Override
	public Class<?> loadClass(String name) throws UnknownClassError {
		return this.delegate.loadClass(name);
	}

	@Override
	public InputStream getOptionalResource(String location) {
		return this.delegate.getOptionalResource(location);
	}

	@Override
	public InputStream getResource(String location) throws UnknownResourceError {
		return this.delegate.getResource(location);
	}

	@Override
	public ClassLoader getClassLoader() {
		return this.delegate.getClassLoader();
	}

	/**
	 * Delegate {@link SourceContext}.
	 * 
	 * @author Daniel Sagenschneider
	 */
	private static class DelegateSourceContext extends SourcePropertiesImpl
			implements SourceContext {

		/**
		 * {@link ClassLoader}.
		 */
		private final ClassLoader classLoader;

		/**
		 * {@link ResourceSource} instances.
		 */
		private final ResourceSource[] resourceSources;

		/**
		 * Initiate.
		 * 
		 * @param classLoader
		 *            {@link ClassLoader}.
		 * @param resourceSources
		 *            {@link ResourceSource} instances.
		 */
		public DelegateSourceContext(ClassLoader classLoader,
				ResourceSource[] resourceSources) {
			this.classLoader = classLoader;
			this.resourceSources = resourceSources;
		}

		/*
		 * =================== SourceContext ====================
		 */

		@Override
		public Class<?> loadOptionalClass(String name) {
			try {
				return this.classLoader.loadClass(name);
			} catch (ClassNotFoundException ex) {
				return null;
			}
		}

		@Override
		public Class<?> loadClass(String name) throws UnknownClassError {

			// Load the class
			Class<?> clazz = this.loadOptionalClass(name);

			// Ensure have class
			if (clazz == null) {
				throw new UnknownClassError("Unknown class '" + name + "'",
						name);
			}

			// Return the class
			return clazz;
		}

		@Override
		public InputStream getOptionalResource(String location) {

			// Attempt to obtain from resource sources first
			for (ResourceSource resourceSource : this.resourceSources) {
				InputStream resource = resourceSource.sourceResource(location);
				if (resource != null) {
					return resource; // located resource
				}
			}

			// Attempt to load from class loader
			InputStream resource = this.classLoader
					.getResourceAsStream(location);

			// Return the resource
			return resource;
		}

		@Override
		public InputStream getResource(String location)
				throws UnknownResourceError {

			// Attempt to obtain the resource
			InputStream resource = this.getOptionalResource(location);

			// Ensure have resource
			if (resource == null) {
				throw new UnknownResourceError("Unknown resource '" + location
						+ "'", location);
			}

			// Return the resource
			return resource;
		}

		@Override
		public ClassLoader getClassLoader() {
			return this.classLoader;
		}
	}

}