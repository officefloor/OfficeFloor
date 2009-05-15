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
package net.officefloor.plugin.spring.beanfactory;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;

/**
 * {@link ManagedObjectSource} to obtain the {@link BeanFactory}.
 * 
 * @author Daniel
 */
public class BeanFactoryManagedObjectSource extends
		AbstractManagedObjectSource<None, None> implements ManagedObject {

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
	private BeanFactory beanFactory;

	/*
	 * ================= AbstractManagedObjectSource ======================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(BEAN_FACTORY_PATH_PROPERTY_NAME,
				"BeanFactory configuration");
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, None> context)
			throws Exception {
		ManagedObjectSourceContext<None> mosContext = context
				.getManagedObjectSourceContext();

		// Obtain the path to the bean factory configuration
		String configurationPath = mosContext
				.getProperty(BEAN_FACTORY_PATH_PROPERTY_NAME);

		// Obtain the bean factory
		this.beanFactory = getXmlBeanFactory(configurationPath, mosContext
				.getClassLoader());

		// Indicate the object type
		context.setObjectClass(BeanFactory.class);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return this;
	}

	/*
	 * ================= ManagedObject ===================================
	 */

	@Override
	public Object getObject() throws Exception {
		return this.beanFactory;
	}

}