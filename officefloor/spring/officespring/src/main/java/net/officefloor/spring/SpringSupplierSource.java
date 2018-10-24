/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.spring;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.spi.supplier.source.SupplierSourceContext;
import net.officefloor.compile.spi.supplier.source.impl.AbstractSupplierSource;
import net.officefloor.plugin.section.clazz.Property;

/**
 * Spring {@link SupplierSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class SpringSupplierSource extends AbstractSupplierSource {

	/**
	 * Name of {@link Property} for the Spring Boot configuration {@link Class}.
	 */
	public static final String CONFIGURATION_CLASS_NAME = "configuration.class";

	/*
	 * ================== SupplierSource ============================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(CONFIGURATION_CLASS_NAME, "Configuration Class");
	}

	@Override
	public void supply(SupplierSourceContext context) throws Exception {

		// Load the configurable application context
		String configurationClassName = context.getProperty(CONFIGURATION_CLASS_NAME);
		Class<?> configurationClass = context.loadClass(configurationClassName);
		ConfigurableApplicationContext springContext = SpringApplication.run(configurationClass);

		// Load listing of all the beans (mapped by their type)
		Map<String, List<String>> beanNamesByType = new HashMap<>();
		ConfigurableListableBeanFactory beanFactory = springContext.getBeanFactory();
		for (String name : springContext.getBeanDefinitionNames()) {
			BeanDefinition definition = beanFactory.getBeanDefinition(name);
			String beanClassName = definition.getBeanClassName();
			
			// Determine if factory method
			if (beanClassName == null) {
				if (definition instanceof AnnotatedBeanDefinition) {
					AnnotatedBeanDefinition annotatedDefinition = (AnnotatedBeanDefinition) definition;
					if (annotatedDefinition.getFactoryMethodMetadata() != null) {
						beanClassName = annotatedDefinition.getFactoryMethodMetadata().getReturnTypeName();
					}
				}
			}

			// Add the bean
			if (beanClassName != null) {
				List<String> beanNames = beanNamesByType.get(beanClassName);
				if (beanNames == null) {
					beanNames = new LinkedList<>();
					beanNamesByType.put(beanClassName, beanNames);
				}
				beanNames.add(name);
			}
		}

		// Load the supplied managed object sources
		for (String beanClassName : beanNamesByType.keySet()) {
			Class<?> beanType = context.loadClass(beanClassName);
			List<String> beanNames = beanNamesByType.get(beanClassName);
			switch (beanNames.size()) {
			case 1:
				// Only the one type (so no qualifier)
				String singleBeanName = beanNames.get(0);
				context.addManagedObjectSource(null, beanType,
						new SpringBeanManagedObjectSource(singleBeanName, beanType, springContext));
				break;

			default:
				// Multiple, so provide qualifier
				for (String beanName : beanNames) {
					context.addManagedObjectSource(beanName, beanType,
							new SpringBeanManagedObjectSource(beanName, beanType, springContext));
				}
				break;
			}
		}
	}

}