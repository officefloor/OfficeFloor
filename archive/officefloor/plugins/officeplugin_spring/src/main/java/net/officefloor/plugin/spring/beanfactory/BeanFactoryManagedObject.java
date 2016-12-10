/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.plugin.spring.beanfactory;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

/**
 * {@link ManagedObject} {@link BeanFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class BeanFactoryManagedObject implements
		CoordinatingManagedObject<Indexed>, BeanFactory {

	/**
	 * {@link BeanFactory} to delegate functionality.
	 */
	private final BeanFactory delegate;

	/**
	 * {@link ObjectRegistry} for the {@link DependencyFactoryBean} instances.
	 */
	private ObjectRegistry<Indexed> objectRegistry = null;

	/**
	 * Initiate.
	 * 
	 * @param delegate
	 *            {@link BeanFactory} to delegate functionality.
	 */
	public BeanFactoryManagedObject(BeanFactory delegate) {
		this.delegate = delegate;
	}

	/*
	 * ================= CoordinatingManagedObject ===============
	 */

	@Override
	public void loadObjects(ObjectRegistry<Indexed> registry) throws Throwable {
		// Keep hold of registry for creation of beans
		this.objectRegistry = registry;
	}

	@Override
	public Object getObject() throws Exception {
		return this;
	}

	/*
	 * ================== BeanFactory =============================
	 */

	@Override
	public Object getBean(String name) throws BeansException {

		// Ensure object registry is set/cleared before obtaining bean
		DependencyFactoryBean.setObjectRegistry(this.objectRegistry);

		// Return the bean
		return this.delegate.getBean(name);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Object getBean(String name, Class requiredType)
			throws BeansException {

		// Ensure object registry is set/cleared before obtaining bean
		DependencyFactoryBean.setObjectRegistry(this.objectRegistry);

		// Return the bean
		return this.delegate.getBean(name, requiredType);
	}

	@Override
	public boolean containsBean(String name) {
		return this.delegate.containsBean(name);
	}

	@Override
	public String[] getAliases(String name) {
		return this.delegate.getAliases(name);
	}

	@Override
	public Class<?> getType(String name) throws NoSuchBeanDefinitionException {
		return this.delegate.getType(name);
	}

	@Override
	public boolean isPrototype(String name)
			throws NoSuchBeanDefinitionException {
		return this.delegate.isPrototype(name);
	}

	@Override
	public boolean isSingleton(String name)
			throws NoSuchBeanDefinitionException {
		return this.delegate.isSingleton(name);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public boolean isTypeMatch(String name, Class targetType)
			throws NoSuchBeanDefinitionException {
		return this.delegate.isTypeMatch(name, targetType);
	}

}