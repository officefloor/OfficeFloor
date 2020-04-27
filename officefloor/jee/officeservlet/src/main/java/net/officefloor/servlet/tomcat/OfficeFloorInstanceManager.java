/*-
 * #%L
 * Servlet
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.servlet.tomcat;

import java.lang.reflect.InvocationTargetException;

import javax.naming.NamingException;

import org.apache.tomcat.InstanceManager;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.servlet.inject.InjectContextFactory;

/**
 * {@link OfficeFloor} {@link InstanceManager}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorInstanceManager implements InstanceManager {

	/**
	 * {@link InjectContextFactory}.
	 */
	private final InjectContextFactory injectContextFactory;

	/**
	 * {@link ClassLoader}.
	 */
	private final ClassLoader classLoader;

	/**
	 * Instantiate.
	 * 
	 * @param injectContextFactory {@link InjectContextFactory}.
	 * @param classLoader          {@link ClassLoader}.
	 */
	public OfficeFloorInstanceManager(InjectContextFactory injectContextFactory, ClassLoader classLoader) {
		this.injectContextFactory = injectContextFactory;
		this.classLoader = classLoader;
	}

	/*
	 * ==================== InstanceManager ===========================
	 */

	@Override
	public Object newInstance(String fqcn, ClassLoader classLoader)
			throws IllegalAccessException, InvocationTargetException, NamingException, InstantiationException,
			ClassNotFoundException, IllegalArgumentException, NoSuchMethodException, SecurityException {
		Class<?> clazz = classLoader.loadClass(fqcn);
		return this.newInstance(clazz);
	}

	@Override
	public Object newInstance(String className)
			throws IllegalAccessException, InvocationTargetException, NamingException, InstantiationException,
			ClassNotFoundException, IllegalArgumentException, NoSuchMethodException, SecurityException {
		return this.newInstance(this.classLoader.loadClass(className));
	}

	@Override
	public Object newInstance(Class<?> clazz) throws IllegalAccessException, InvocationTargetException, NamingException,
			InstantiationException, IllegalArgumentException, NoSuchMethodException, SecurityException {

		// Instantiate
		Object object = clazz.getConstructor().newInstance();

		// Load any possible injection
		this.newInstance(object);

		// Return the instance
		return object;
	}

	@Override
	public void newInstance(Object o) throws IllegalAccessException, InvocationTargetException, NamingException {

		// Undertake injection of dependencies
		this.injectContextFactory.injectDependencies(o);
	}

	@Override
	public void destroyInstance(Object o) throws IllegalAccessException, InvocationTargetException {
		// Nothing to destroy
	}

}
