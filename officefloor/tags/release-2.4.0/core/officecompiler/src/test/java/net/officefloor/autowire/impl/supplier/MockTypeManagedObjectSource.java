/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

package net.officefloor.autowire.impl.supplier;

import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;
import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.ManagedObjectSourceWirer;
import net.officefloor.autowire.spi.supplier.source.SupplierSourceContext;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.spi.office.ManagedObjectTeam;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.spi.TestSource;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.extension.ExtensionInterfaceFactory;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectTaskBuilder;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectWorkBuilder;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;

/**
 * Provides the ability to mock the {@link ManagedObjectType}.
 * 
 * @author Daniel Sagenschneider
 */
@TestSource
public class MockTypeManagedObjectSource extends
		AbstractManagedObjectSource<Indexed, Indexed> implements
		WorkFactory<Work>, TaskFactory<Work, Indexed, Indexed>,
		ExtensionInterfaceFactory<Object> {

	/**
	 * Object type.
	 */
	private final Class<?> objectType;

	/**
	 * {@link MockTypeDependency} instances.
	 */
	private final List<MockTypeDependency> dependencies = new LinkedList<MockTypeDependency>();

	/**
	 * {@link MockTypeFlow} instances.
	 */
	private final List<MockTypeFlow> flows = new LinkedList<MockTypeFlow>();

	/**
	 * {@link ManagedObjectTeam} instances.
	 */
	private final List<String> teams = new LinkedList<String>();

	/**
	 * Extension interfaces.
	 */
	private final List<Class<?>> extensionInterfaces = new LinkedList<Class<?>>();

	/**
	 * Initiate.
	 * 
	 * @param objectType
	 *            Object type.
	 */
	public MockTypeManagedObjectSource(Class<?> objectType) {
		this.objectType = objectType;
	}

	/**
	 * Adds this {@link ManagedObjectSource} to the
	 * {@link SupplierSourceContext}.
	 * 
	 * @param context
	 *            {@link SupplierSourceContext}.
	 * @param wirer
	 *            {@link ManagedObjectSourceWirer}. May be <code>null</code> if
	 *            no wiring necessary.
	 */
	public void addAsManagedObject(SupplierSourceContext context,
			ManagedObjectSourceWirer wirer) {
		context.addManagedObject(this, wirer, this.getAutoWire());
	}

	/**
	 * Obtains the {@link AutoWire} based on object type.
	 * 
	 * @return {@link AutoWire} based on object type.
	 */
	public AutoWire getAutoWire() {
		return new AutoWire(this.objectType);
	}

	/**
	 * Adds a dependency.
	 * 
	 * @param name
	 *            Name of dependency.
	 * @param type
	 *            Type of dependency.
	 * @param qualifier
	 *            Qualifier of dependency type.
	 */
	public void addDependency(String name, Class<?> type, String qualifier) {
		this.dependencies.add(new MockTypeDependency(name, type, qualifier));
	}

	/**
	 * Adds a flow.
	 * 
	 * @param name
	 *            Name of flow.
	 * @param argumentType
	 *            Argument type for flow.
	 */
	public void addFlow(String name, Class<?> argumentType) {
		this.flows.add(new MockTypeFlow(name, argumentType));
	}

	/**
	 * Adds a team.
	 * 
	 * @param name
	 *            Name of team.
	 */
	public void addTeam(String name) {
		this.teams.add(name);
	}

	/**
	 * Adds an extension interface.
	 * 
	 * @param extensionInterface
	 *            Extension interface.
	 */
	public void addExtensionInterface(Class<?> extensionInterface) {
		this.extensionInterfaces.add(extensionInterface);
	}

	/*
	 * ================== ManagedObjectSource ===================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void loadMetaData(MetaDataContext<Indexed, Indexed> context)
			throws Exception {

		// Specify the meta-data
		context.setObjectClass(this.objectType);

		// Configure the dependencies
		for (MockTypeDependency dependency : this.dependencies) {
			DependencyLabeller labeller = context
					.addDependency(dependency.type);
			labeller.setLabel(dependency.name);
			if (dependency.qualifier != null) {
				labeller.setTypeQualifier(dependency.qualifier);
			}
		}

		// Configure the flows
		for (MockTypeFlow flow : this.flows) {
			Labeller labeller = context.addFlow(flow.argumentType);
			labeller.setLabel(flow.name);
		}

		// Configure the required teams
		for (String teamName : this.teams) {
			ManagedObjectSourceContext<Indexed> mosContext = context
					.getManagedObjectSourceContext();
			ManagedObjectWorkBuilder<Work> work = mosContext.addWork("WORK-"
					+ teamName, this);
			ManagedObjectTaskBuilder<Indexed, Indexed> task = work.addTask(
					"TASK-" + teamName, this);
			task.setTeam(teamName);
		}

		// Configure the extension interfaces
		for (Class extensionInterface : this.extensionInterfaces) {
			context.addManagedObjectExtensionInterface(extensionInterface, this);
		}
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		TestCase.fail("Should not require obtaining the Managed Object for type testing");
		return null;
	}

	/*
	 * ========================== WorkFactory ===========================
	 */

	@Override
	public Work createWork() {
		TestCase.fail("Should not require creating work for type testing");
		return null;
	}

	/*
	 * ========================== TaskFactory ===========================
	 */

	@Override
	public Task<Work, Indexed, Indexed> createTask(Work work) {
		TestCase.fail("Should not require creating a task for type testing");
		return null;
	}

	/*
	 * ===================== ExtensionInterfaceFactory ======================
	 */

	@Override
	public Object createExtensionInterface(ManagedObject managedObject) {
		TestCase.fail("Should not require creating an extension for type testing");
		return null;
	}

	/**
	 * {@link MockTypeManagedObjectSource} dependency.
	 */
	private static class MockTypeDependency {

		/**
		 * Name of dependency.
		 */
		public final String name;

		/**
		 * Type of dependency.
		 */
		public final Class<?> type;

		/**
		 * Qualifier of dependency type.
		 */
		public final String qualifier;

		/**
		 * Initiate.
		 * 
		 * @param name
		 *            Name of dependency.
		 * @param type
		 *            Type of dependency.
		 * @param qualifier
		 *            Qualifier of dependency type.
		 */
		public MockTypeDependency(String name, Class<?> type, String qualifier) {
			this.name = name;
			this.type = type;
			this.qualifier = qualifier;
		}
	}

	/**
	 * {@link MockTypeManagedObjectSource} flow.
	 */
	private static class MockTypeFlow {

		/**
		 * Name of flow.
		 */
		public final String name;

		/**
		 * Argument type for flow.
		 */
		public final Class<?> argumentType;

		/**
		 * Initiate.
		 * 
		 * @param name
		 *            Name of flow.
		 * @param argumentType
		 *            Argument type for flow.
		 */
		public MockTypeFlow(String name, Class<?> argumentType) {
			this.name = name;
			this.argumentType = argumentType;
		}
	}

}