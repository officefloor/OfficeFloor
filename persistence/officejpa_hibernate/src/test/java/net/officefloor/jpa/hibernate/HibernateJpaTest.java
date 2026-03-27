/*-
 * #%L
 * Hibernate JPA Persistence
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.jpa.hibernate;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.hibernate.service.spi.ServiceException;
import org.junit.jupiter.api.BeforeEach;

import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.jpa.JpaManagedObjectSource;
import net.officefloor.jpa.test.AbstractJpaTestCase;
import net.officefloor.jpa.test.IMockEntity;

/**
 * Hibernate {@link AbstractJpaTestCase}.
 * 
 * @author Daniel Sagenschneider
 */
public class HibernateJpaTest extends AbstractJpaTestCase {

	@BeforeEach
	public void initiateLogger() throws Exception {

		// Ignore hibernate logging
		Logger.getLogger("org.hibernate").setLevel(Level.WARNING);
	}

	@Override
	protected Class<? extends JpaManagedObjectSource> getJpaManagedObjectSourceClass() {
		return HibernateJpaManagedObjectSource.class;
	}

	@Override
	protected void loadJpaProperties(PropertyConfigurable jpa) {
		jpa.addProperty(JpaManagedObjectSource.PROPERTY_PERSISTENCE_UNIT, "test");
	}

	@Override
	protected Class<? extends IMockEntity> getMockEntityClass() {
		return MockEntity.class;
	}

	@Override
	protected Class<?> getNoConnectionFactoryExceptionClass() {
		return ServiceException.class;
	}

	@Override
	protected String getNoConnectionFactoryExceptionMessage() {
		return "Unable to create requested service";
	}
}
