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
import net.officefloor.plugin.spring.beans.MockBean;

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
		type.addDependency("testFactory", Connection.class, null, 0, null);

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
		ManagedObject managedObject = new ManagedObjectUserStandAlone()
				.sourceManagedObject(managedObjectSource);
		Object object = managedObject.getObject();
		assertTrue("Must be bean factory", (object instanceof BeanFactory));

		// Ensure correct bean factory
		BeanFactory beanFactory = (BeanFactory) object;
		MockBean bean = (MockBean) beanFactory.getBean("test", MockBean.class);
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

		// Record delegating functionality
		this.recordReturn(DEPENDENCY, DEPENDENCY.getClientInfo("ONE"), "A");

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
		ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();
		user.setObjectRegistry(objectRegistry);
		CoordinatingManagedObject<Indexed> managedObject = (CoordinatingManagedObject<Indexed>) user
				.sourceManagedObject(managedObjectSource);
		BeanFactory beanFactory = (BeanFactory) managedObject.getObject();

		// Ensure able to obtain dependency
		Connection dependency = (Connection) beanFactory.getBean("testFactory");

		// Ensure delegate to actual dependency
		assertEquals("Incorrect after delegation", "A",
				dependency.getClientInfo("ONE"));

		this.verifyMockObjects();
	}

	/**
	 * Obtains the path to the configuration for the {@link BeanFactory}.
	 * 
	 * @return Path to the configuration for the {@link BeanFactory}.
	 */
	private String getBeanFactoryConfigurationPath() {
		return this.getFileLocation(MockBean.class, "Test.beans.xml");
	}
}