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

import java.util.Properties;

import net.officefloor.desk.WorkLoaderContextImpl;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.work.TaskModel;
import net.officefloor.model.work.WorkModel;
import net.officefloor.work.WorkLoader;
import net.officefloor.work.WorkLoaderContext;

import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

/**
 * Tests the {@link SpringWorkLoader}.
 * 
 * @author Daniel
 */
public class SpringWorkLoaderTest extends OfficeFrameTestCase {

	/**
	 * Spring file path.
	 */
	private final String SPRING_FILE_PATH = "net/officefloor/work/spring/Test.beans.xml";

	/**
	 * Ensures Spring bean can be created.
	 */
	public void testEnsureBeanCreated() throws Exception {

		// Load the beans
		XmlBeanFactory beanFactory = new XmlBeanFactory(new ClassPathResource(
				SPRING_FILE_PATH, this.getClass().getClassLoader()));

		// Obtain the test bean
		TestBean testBean = (TestBean) beanFactory.getBean("test");

		// Ensure the dependency loaded
		assertNotNull("Ensure dependency loaded", testBean.getDependency());
	}

	/**
	 * Ensures able to load a Spring bean as {@link Work}.
	 */
	public void testLoadSpringBeanWork() throws Exception {

		// Create the spring work loader
		WorkLoader workLoader = new SpringWorkLoader();

		// Create the work loader context for loading the work
		Properties properties = new Properties();
		properties.setProperty(SpringWorkLoader.PROPERTY_SPRING_FILE,
				SPRING_FILE_PATH);
		properties.setProperty(SpringWorkLoader.PROPERTY_BEAN_NAME, "test");
		WorkLoaderContext workLoaderContext = new WorkLoaderContextImpl(
				properties, this.getClass().getClassLoader());

		// Load the work
		WorkModel<?> workModel = workLoader.loadWork(workLoaderContext);

		// Ensure spring loading (ie dependency loaded)
		SpringWorkFactory workFactory = (SpringWorkFactory) workModel
				.getWorkFactory();
		TestBean testBean = (TestBean) workFactory.createWork().getObject();
		assertNotNull("Bean not loaded by spring", testBean.getDependency());

		// Ensure only non-spring tasks included
		assertList("getTaskName", new String[] { "getTaskName" }, workModel
				.getTasks(), new TaskModel<None, None>("doFunctionality", null,
				null, null, null, null, null), new TaskModel<None, None>(
				"getDependency", null, null, null, null, null, null));
	}

}
