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
package net.officefloor.work.spring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.officefloor.frame.api.execute.Work;
import net.officefloor.model.work.TaskModel;
import net.officefloor.model.work.WorkModel;
import net.officefloor.work.AbstractWorkLoader;
import net.officefloor.work.WorkLoader;
import net.officefloor.work.WorkLoaderContext;
import net.officefloor.work.clazz.ClassWorkLoader;

import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * {@link WorkLoader} for loading Spring beans as {@link Work}.
 * 
 * @author Daniel
 */
public class SpringWorkLoader extends AbstractWorkLoader {

	/**
	 * Property to obtain the location of the Spring configuration file.
	 */
	public static final String PROPERTY_SPRING_FILE = "spring.file";

	/**
	 * Name of the Spring bean to use as the {@link Work}.
	 */
	public static final String PROPERTY_BEAN_NAME = "bean.name";

	/**
	 * Registry of the {@link XmlBeanFactory} instances by their location to
	 * stop duplicate loading.
	 */
	private static final Map<String, XmlBeanFactory> beanFactories = new HashMap<String, XmlBeanFactory>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.work.AbstractWorkLoader#loadSpecification(net.officefloor.work.AbstractWorkLoader.SpecificationContext)
	 */
	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_SPRING_FILE);
		context.addProperty(PROPERTY_BEAN_NAME);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.work.WorkLoader#loadWork(net.officefloor.work.WorkLoaderContext)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public WorkModel<?> loadWork(WorkLoaderContext context) throws Exception {

		// Obtain the spring configuration file
		String springFilePath = context.getProperty(PROPERTY_SPRING_FILE);

		// Obtain the XML bean factory
		XmlBeanFactory beanFactory = beanFactories.get(springFilePath);
		if (beanFactory == null) {
			// Create the XML bean factory
			Resource resource = new ClassPathResource(springFilePath, context
					.getClassLoader());
			beanFactory = new XmlBeanFactory(resource);

			// Register XML bean factory for next work
			beanFactories.put(springFilePath, beanFactory);
		}

		// Obtain the name of the bean to use as work
		String beanName = context.getProperty(PROPERTY_BEAN_NAME);

		// Obtain the definition for the particular bean
		BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);

		// Obtain the name of the bean
		String beanClassName = beanDefinition.getBeanClassName();

		// Use the class work loader to reflectively invoke the bean
		context.getProperties().setProperty(
				ClassWorkLoader.CLASS_NAME_PROPERTY_NAME, beanClassName);
		WorkModel workModel = new ClassWorkLoader().loadWork(context);

		// Create the set methods used by Spring to initialise the bean
		Set<String> springMethods = new HashSet<String>();
		for (PropertyValue property : beanDefinition.getPropertyValues()
				.getPropertyValues()) {
			String propertyName = property.getName();

			// Transform into set method
			String setMethodName = "set"
					+ propertyName.substring(0, 1).toUpperCase()
					+ (propertyName.length() > 1 ? propertyName.substring(1)
							: "");

			// Add set method name
			springMethods.add(setMethodName);
		}

		// Remove setter methods on bean used by Spring
		List<TaskModel<?, ?>> taskListCopy = new ArrayList<TaskModel<?, ?>>(
				workModel.getTasks());
		for (TaskModel<?, ?> taskModel : taskListCopy) {
			// Determine if task is for a spring method
			String taskMethodName = taskModel.getTaskName();
			if (springMethods.contains(taskMethodName)) {
				// Remove task as a spring method
				workModel.getTasks().remove(taskModel);
			}
		}

		// Override the work factory
		workModel.setWorkFactory(new SpringWorkFactory(beanFactory, beanName));

		// Return the work model
		return workModel;
	}

}
