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
package net.officefloor.plugin.spring.bean;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.XmlBeanFactory;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.plugin.spring.beanfactory.BeanFactoryManagedObjectSource;

/**
 * {@link ManagedObjectSource} that provides a Spring bean from the
 * {@link BeanFactory}.
 * 
 * @author Daniel Sagenschneider
 * 
 * @see BeanFactoryManagedObjectSource
 */
public class BeanManagedObjectSource
		extends
		AbstractManagedObjectSource<BeanManagedObjectSource.BeanDependency, None> {

	/**
	 * Dependency key to obtain the {@link BeanFactory}.
	 */
	public static enum BeanDependency {
		BEAN_FACTORY
	}

	/**
	 * Name of the bean.
	 */
	public static final String BEAN_NAME_PROPERTY_NAME = "bean.name";

	/**
	 * Path to the {@link BeanFactory} configuration.
	 */
	public static final String BEAN_FACTORY_PATH_PROPERTY_NAME = "bean.factory.path";

	/**
	 * Name of the bean.
	 */
	private String beanName;

	/**
	 * Type of the bean.
	 */
	private Class<?> beanClass;

	/*
	 * ================ AbstractManagedObjectSource =======================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(BEAN_NAME_PROPERTY_NAME, "Bean");
		context.addProperty(BEAN_FACTORY_PATH_PROPERTY_NAME,
				"BeanFactory configuration");
	}

	@Override
	protected void loadMetaData(MetaDataContext<BeanDependency, None> context)
			throws Exception {
		ManagedObjectSourceContext<None> mosContext = context
				.getManagedObjectSourceContext();

		// Obtain the bean name
		this.beanName = mosContext.getProperty(BEAN_NAME_PROPERTY_NAME);

		// Obtain the XML bean factory (to obtain type information)
		String configurationPath = mosContext
				.getProperty(BEAN_FACTORY_PATH_PROPERTY_NAME);
		XmlBeanFactory beanFactory = BeanFactoryManagedObjectSource
				.getXmlBeanFactory(configurationPath,
						mosContext.getClassLoader());

		// Obtain the definition and subsequently bean type
		BeanDefinition beanDefinition = beanFactory
				.getBeanDefinition(this.beanName);
		String beanClassName = beanDefinition.getBeanClassName();
		this.beanClass = mosContext.loadClass(beanClassName);

		// Specify object type
		context.setObjectClass(this.beanClass);

		// Ensure have bean factory dependency
		context.addDependency(BeanDependency.BEAN_FACTORY, BeanFactory.class)
				.setLabel("BeanFactory");
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new BeanManagedObject();
	}

	/**
	 * Bean {@link ManagedObject}.
	 */
	private class BeanManagedObject implements
			CoordinatingManagedObject<BeanDependency> {

		/**
		 * Bean.
		 */
		private Object bean;

		/*
		 * =========== CoordinatingManagedObject =========================
		 */

		@Override
		public void loadObjects(ObjectRegistry<BeanDependency> registry)
				throws Throwable {

			// Obtain the bean factory
			BeanFactory beanFactory = (BeanFactory) registry
					.getObject(BeanDependency.BEAN_FACTORY);

			// Obtain the bean
			this.bean = beanFactory.getBean(
					BeanManagedObjectSource.this.beanName,
					BeanManagedObjectSource.this.beanClass);
		}

		@Override
		public Object getObject() throws Exception {
			return this.bean;
		}
	}

}