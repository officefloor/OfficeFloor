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
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.RequiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * {@link ManagedObjectSource} to obtain the {@link BeanFactory}.
 * 
 * @author Daniel Sagenschneider
 * 
 * @see DependencyFactoryBean
 */
public class BeanFactoryManagedObjectSource extends
		AbstractManagedObjectSource<Indexed, None> {

	/**
	 * Path to the {@link BeanFactory} configuration.
	 */
	public static final String BEAN_FACTORY_PATH_PROPERTY_NAME = "bean.factory.path";

	/**
	 * Convenience method to obtain the {@link BeanFactory}.
	 * 
	 * @param beanFactoryPath
	 *            Path to the configuration of the {@link BeanFactory}.
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @return {@link XmlBeanFactory}.
	 */
	public static XmlBeanFactory getXmlBeanFactory(String beanFactoryPath,
			ClassLoader classLoader) {

		// Create the resource to the bean factory configuration
		Resource resource = new ClassPathResource(beanFactoryPath, classLoader);

		// Create and return the bean factory
		return new XmlBeanFactory(resource);
	}

	/**
	 * {@link BeanFactory}.
	 */
	private XmlBeanFactory beanFactory;

	/*
	 * ================= AbstractManagedObjectSource ======================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(BEAN_FACTORY_PATH_PROPERTY_NAME,
				"BeanFactory configuration");
	}

	@Override
	protected void loadMetaData(MetaDataContext<Indexed, None> context)
			throws Exception {
		ManagedObjectSourceContext<None> mosContext = context
				.getManagedObjectSourceContext();

		// Obtain the path to the bean factory configuration
		String configurationPath = mosContext
				.getProperty(BEAN_FACTORY_PATH_PROPERTY_NAME);

		// Obtain the bean factory
		this.beanFactory = getXmlBeanFactory(configurationPath,
				mosContext.getClassLoader());

		// Ensure 'Required' properties are configured.
		// This is not totally necessary but putting in to ensure configured.
		this.beanFactory
				.addBeanPostProcessor(new RequiredAnnotationBeanPostProcessor());

		// Indicate the meta-data
		context.setManagedObjectClass(BeanFactoryManagedObject.class);
		context.setObjectClass(BeanFactory.class);

		// Load the dependencies
		for (String beanName : this.beanFactory.getBeanDefinitionNames()) {
			BeanDefinition beanDefinition = this.beanFactory
					.getBeanDefinition(beanName);

			// Obtain the bean class
			String beanClassName = beanDefinition.getBeanClassName();
			Class<?> beanClass = mosContext.loadClass(beanClassName);

			// Ignore non dependency factory beans
			if (!(DependencyFactoryBean.class.isAssignableFrom(beanClass))) {
				continue;
			}

			// Obtain required type for dependency
			PropertyValue property = beanDefinition.getPropertyValues()
					.getPropertyValue(
							DependencyFactoryBean.TYPE_SPRING_PROPERTY);
			if (property == null) {
				throw new BeanInitializationException("Property '"
						+ DependencyFactoryBean.TYPE_SPRING_PROPERTY
						+ "' is required for bean '" + beanName + "'");
			}
			TypedStringValue requiredTypeName = (TypedStringValue) property
					.getValue();
			Class<?> requiredType = mosContext.loadClass(requiredTypeName
					.getValue());

			// Add the dependency
			int dependencyIndex = context.addDependency(requiredType)
					.setLabel(beanName).getIndex();

			// Provide the dependency index for bean
			MutablePropertyValues propertyValues = beanDefinition
					.getPropertyValues();
			propertyValues.addPropertyValue(
					DependencyFactoryBean.DEPENDENCY_INDEX_SPRING_PROPERTY,
					new Integer(dependencyIndex));
		}
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new BeanFactoryManagedObject(this.beanFactory);
	}

}