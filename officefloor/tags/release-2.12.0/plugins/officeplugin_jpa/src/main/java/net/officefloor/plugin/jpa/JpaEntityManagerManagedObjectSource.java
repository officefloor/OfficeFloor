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
package net.officefloor.plugin.jpa;

import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.extension.ExtensionInterfaceFactory;
import net.officefloor.frame.spi.managedobject.recycle.RecycleManagedObjectParameter;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectTaskBuilder;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectWorkBuilder;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.plugin.jpa.CloseEntityManagerTask.CloseEntityManagerDependencies;

/**
 * {@link ManagedObjectSource} to provide a JPA {@link EntityManager}.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: type
public class JpaEntityManagerManagedObjectSource extends
		AbstractManagedObjectSource<None, None> implements
		ExtensionInterfaceFactory<EntityTransaction> {

	/**
	 * Name of property providing the persistence unit name.
	 */
	public static final String PROPERTY_PERSISTENCE_UNIT_NAME = "persistence.unit.name";

	/**
	 * Name of {@link Team} to close the {@link EntityManager}.
	 */
	public static final String TEAM_CLOSE = "CLOSE";

	/**
	 * Persistence unit name.
	 */
	private String persistenceUnitName;

	/**
	 * {@link Properties} to configure the {@link EntityManagerFactory}. Enables
	 * providing differing {@link Properties} values between environments.
	 */
	private Properties properties;

	/**
	 * {@link EntityManagerFactory}.
	 */
	private EntityManagerFactory entityManagerFactory;

	/*
	 * ================ ManagedObjectSource ========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_PERSISTENCE_UNIT_NAME, "Persistence Unit");
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, None> context)
			throws Exception {
		ManagedObjectSourceContext<None> mosContext = context
				.getManagedObjectSourceContext();

		// Obtain the persistence unit name
		this.persistenceUnitName = mosContext
				.getProperty(PROPERTY_PERSISTENCE_UNIT_NAME);

		// Obtain the additional properties for the entity manager factory
		this.properties = mosContext.getProperties();

		// Specify meta-data
		context.setObjectClass(EntityManager.class);

		// Provide close entity manager
		CloseEntityManagerTask closeTask = new CloseEntityManagerTask();
		ManagedObjectWorkBuilder<CloseEntityManagerTask> recycleWork = mosContext
				.getRecycleWork(closeTask);
		ManagedObjectTaskBuilder<CloseEntityManagerDependencies, None> recycleTask = recycleWork
				.addTask("CLOSE", closeTask);
		recycleTask.linkParameter(
				CloseEntityManagerDependencies.MANAGED_OBJECT,
				RecycleManagedObjectParameter.class);
		recycleTask.setTeam(TEAM_CLOSE);

		// Extension interface
		context.addManagedObjectExtensionInterface(EntityTransaction.class,
				this);
	}
	// END SNIPPET: type

	// START SNIPPET: tutorial

	@Override
	public void start(ManagedObjectExecuteContext<None> context)
			throws Exception {
		this.entityManagerFactory = Persistence.createEntityManagerFactory(
				this.persistenceUnitName, this.properties);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {

		// Create the entity manager
		EntityManager entityManager = this.entityManagerFactory
				.createEntityManager();

		// Create and return the managed object
		return new JpaEntityManagerManagedObject(entityManager);
	}

	/*
	 * ======================= ExtensionInterfaceFactory =======================
	 */

	@Override
	public EntityTransaction createExtensionInterface(
			ManagedObject managedObject) {
		JpaEntityManagerManagedObject jpaEntityManagerMo = (JpaEntityManagerManagedObject) managedObject;
		return jpaEntityManagerMo.getEntityTransaction();
	}

}
// END SNIPPET: tutorial