/*-
 * #%L
 * Servlet
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
