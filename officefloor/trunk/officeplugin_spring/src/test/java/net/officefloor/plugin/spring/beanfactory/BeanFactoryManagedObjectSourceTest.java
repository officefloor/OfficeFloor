/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.plugin.spring.beanfactory;

import java.sql.Connection;

import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.plugin.spring.beans.TestBean;

import org.springframework.beans.factory.BeanFactory;

/**
 * Tests the {@link BeanFactoryManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class BeanFactoryManagedObjectSourceTest extends OfficeFrameTestCase {

	/**
	 * Validates the specification.
	 */
	public void testSpecification() {
		ManagedObjectLoaderUtil.validateSpecification(
				BeanFactoryManagedObjectSource.class,
				BeanFactoryManagedObjectSource.BEAN_FACTORY_PATH_PROPERTY_NAME,
				"BeanFactory configuration");
	}

	/**
	 * Validates the {@link ManagedObjectType}.
	 */
	public void testManagedObjectType() {

		// Create the expected type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil
				.createManagedObjectTypeBuilder();
		type.setObjectClass(BeanFactory.class);
		type.addDependency("testFactory", Connection.class, 0, null);

		// Validate the managed object type
		ManagedObjectLoaderUtil.validateManagedObjectType(type,
				BeanFactoryManagedObjectSource.class,
				BeanFactoryManagedObjectSource.BEAN_FACTORY_PATH_PROPERTY_NAME,
				this.getBeanFactoryConfigurationPath());
	}

	/**
	 * Ensures able to obtain the {@link BeanFactory}.
	 */
	public void testObtainBeanFactory() throws Throwable {

		// Obtain the path to the configuration
		String configurationPath = this.getBeanFactoryConfigurationPath();

		// Ensure able to obtain the bean factory
		ManagedObjectSourceStandAlone standAlone = new ManagedObjectSourceStandAlone();
		standAlone.addProperty(
				BeanFactoryManagedObjectSource.BEAN_FACTORY_PATH_PROPERTY_NAME,
				configurationPath);
		BeanFactoryManagedObjectSource managedObjectSource = standAlone
				.loadManagedObjectSource(BeanFactoryManagedObjectSource.class);
		ManagedObject managedObject = ManagedObjectUserStandAlone
				.sourceManagedObject(managedObjectSource);
		Object object = managedObject.getObject();
		assertTrue("Must be bean factory", (object instanceof BeanFactory));

		// Ensure correct bean factory
		BeanFactory beanFactory = (BeanFactory) object;
		TestBean bean = (TestBean) beanFactory.getBean("test", TestBean.class);
		assertNotNull("Incorrect bean factory", bean);
	}

	/**
	 * Ensures able to obtain dependency via the {@link DependencyFactoryBean}.
	 */
	@SuppressWarnings("unchecked")
	public void testObtainDependency() throws Throwable {

		final ObjectRegistry<Indexed> objectRegistry = this
				.createMock(ObjectRegistry.class);
		final Connection DEPENDENCY = this.createMock(Connection.class);

		// Record obtaining the dependency
		this.recordReturn(objectRegistry, objectRegistry.getObject(0),
				DEPENDENCY);
		this.recordReturn(objectRegistry, objectRegistry.getObject(0),
				DEPENDENCY);

		// Record delegating functionality
		this.recordReturn(DEPENDENCY, DEPENDENCY.getClientInfo("ONE"), "A");
		this.recordReturn(DEPENDENCY, DEPENDENCY.getClientInfo("TWO"), "B");

		this.replayMockObjects();

		// Obtain the path to the configuration
		String configurationPath = this.getBeanFactoryConfigurationPath();

		// Ensure able to obtain the bean factory
		ManagedObjectSourceStandAlone standAlone = new ManagedObjectSourceStandAlone();
		standAlone.addProperty(
				BeanFactoryManagedObjectSource.BEAN_FACTORY_PATH_PROPERTY_NAME,
				configurationPath);
		BeanFactoryManagedObjectSource managedObjectSource = standAlone
				.loadManagedObjectSource(BeanFactoryManagedObjectSource.class);
		CoordinatingManagedObject<Indexed> managedObject = (CoordinatingManagedObject<Indexed>) ManagedObjectUserStandAlone
				.sourceManagedObject(managedObjectSource);
		BeanFactory beanFactory = (BeanFactory) managedObject.getObject();

		// Ensure able to obtain dependency before coordinating
		Connection beforeDependency = (Connection) beanFactory
				.getBean("testFactory");

		// Provide registry
		managedObject.loadObjects(objectRegistry);

		// Ensure able to obtain dependency after coordinating
		Connection afterDependency = (Connection) beanFactory
				.getBean("testFactory");

		// Ensure delegate to actual dependency
		assertEquals("Incorrect before delegation", "A", beforeDependency
				.getClientInfo("ONE"));
		assertEquals("Incorrect after delegation", "B", afterDependency
				.getClientInfo("TWO"));

		this.verifyMockObjects();
	}

	/**
	 * Obtains the path to the configuration for the {@link BeanFactory}.
	 * 
	 * @return Path to the configuration for the {@link BeanFactory}.
	 */
	private String getBeanFactoryConfigurationPath() {
		return this.getFileLocation(TestBean.class, "Test.beans.xml");
	}
}