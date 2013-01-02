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

import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.plugin.spring.bean.BeanManagedObjectSource;
import net.officefloor.plugin.spring.bean.BeanManagedObjectSource.BeanDependency;
import net.officefloor.plugin.spring.beans.MockBean;

import org.springframework.beans.factory.BeanFactory;

/**
 * Tests the {@link BeanManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class BeanManagedObjectSourceTest extends OfficeFrameTestCase {

	/**
	 * Validates the specification.
	 */
	public void testSpecification() {
		ManagedObjectLoaderUtil.validateSpecification(
				BeanManagedObjectSource.class,
				BeanManagedObjectSource.BEAN_NAME_PROPERTY_NAME, "Bean",
				BeanManagedObjectSource.BEAN_FACTORY_PATH_PROPERTY_NAME,
				"BeanFactory configuration");
	}

	/**
	 * Validates the {@link ManagedObjectType}.
	 */
	public void testManagedObjectType() throws Throwable {

		// Create the expected type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil
				.createManagedObjectTypeBuilder();
		type.setObjectClass(MockBean.class);
		type.addDependency("BeanFactory", BeanFactory.class, null, 0,
				BeanDependency.BEAN_FACTORY);

		// Validate the managed object type
		ManagedObjectLoaderUtil.validateManagedObjectType(type,
				BeanManagedObjectSource.class,
				BeanManagedObjectSource.BEAN_NAME_PROPERTY_NAME, "test",
				BeanManagedObjectSource.BEAN_FACTORY_PATH_PROPERTY_NAME,
				this.getBeanFactoryConfigurationPath());
	}

	/**
	 * Ensure able to obtain the bean.
	 */
	@SuppressWarnings("unchecked")
	public void testObtainBean() throws Throwable {

		final String BEAN_NAME = "test";
		final ObjectRegistry<BeanDependency> objectRegistry = this
				.createMock(ObjectRegistry.class);
		final BeanFactory beanFactory = this.createMock(BeanFactory.class);
		final Object BEAN = "Bean";

		// Record obtaining the bean
		this.recordReturn(objectRegistry,
				objectRegistry.getObject(BeanDependency.BEAN_FACTORY),
				beanFactory);
		this.recordReturn(beanFactory,
				beanFactory.getBean(BEAN_NAME, MockBean.class), BEAN);

		// Ensure able to obtain the bean factory
		this.replayMockObjects();
		ManagedObjectSourceStandAlone standAlone = new ManagedObjectSourceStandAlone();
		standAlone.addProperty(BeanManagedObjectSource.BEAN_NAME_PROPERTY_NAME,
				BEAN_NAME);
		standAlone.addProperty(
				BeanManagedObjectSource.BEAN_FACTORY_PATH_PROPERTY_NAME,
				this.getBeanFactoryConfigurationPath());
		BeanManagedObjectSource managedObjectSource = standAlone
				.loadManagedObjectSource(BeanManagedObjectSource.class);
		ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();
		user.setObjectRegistry(objectRegistry);
		CoordinatingManagedObject<BeanDependency> managedObject = (CoordinatingManagedObject<BeanDependency>) user
				.sourceManagedObject(managedObjectSource);
		Object object = managedObject.getObject();
		assertEquals("Incorrect bean", BEAN, object);
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