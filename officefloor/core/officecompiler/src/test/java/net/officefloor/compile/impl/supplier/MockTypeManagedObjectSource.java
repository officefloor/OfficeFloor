/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.compile.impl.supplier;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.junit.Assert;

import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.spi.managedobject.ManagedObjectTeam;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.extension.ExtensionFactory;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFunctionBuilder;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;

/**
 * Provides the ability to mock the {@link ManagedObjectType}.
 * 
 * @author Daniel Sagenschneider
 */
@TestSource
public class MockTypeManagedObjectSource extends AbstractManagedObjectSource<Indexed, Indexed>
		implements ManagedFunctionFactory<Indexed, Indexed>, ExtensionFactory<Object> {

	/**
	 * Object type.
	 */
	private final Class<?> objectType;

	/**
	 * Expected name of {@link Logger}.
	 */
	private final String loggerName;

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
	 * @param objectType Object type.
	 * @param loggerName Expected name of {@link Logger}.
	 */
	public MockTypeManagedObjectSource(Class<?> objectType, String loggerName) {
		this.objectType = objectType;
		this.loggerName = loggerName;
	}

	/**
	 * Obtains the object type.
	 * 
	 * @return Object type.
	 */
	public Class<?> getObjectType() {
		return this.objectType;
	}

	/**
	 * Adds a dependency.
	 * 
	 * @param name      Name of dependency.
	 * @param type      Type of dependency.
	 * @param qualifier Qualifier of dependency type.
	 */
	public void addDependency(String name, Class<?> type, String qualifier) {
		this.dependencies.add(new MockTypeDependency(name, type, qualifier));
	}

	/**
	 * Adds a flow.
	 * 
	 * @param name         Name of flow.
	 * @param argumentType Argument type for flow.
	 */
	public void addFlow(String name, Class<?> argumentType) {
		this.flows.add(new MockTypeFlow(name, argumentType));
	}

	/**
	 * Adds a team.
	 * 
	 * @param name Name of team.
	 */
	public void addTeam(String name) {
		this.teams.add(name);
	}

	/**
	 * Adds an extension interface.
	 * 
	 * @param extensionInterface Extension interface.
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
	protected void loadMetaData(MetaDataContext<Indexed, Indexed> context) throws Exception {
		ManagedObjectSourceContext<Indexed> mosContext = context.getManagedObjectSourceContext();

		// Ensure correct logger name
		assertEquals("Incorrect logger name", this.loggerName, mosContext.getLogger().getName());

		// Specify the meta-data
		context.setObjectClass(this.objectType);

		// Configure the dependencies
		for (MockTypeDependency dependency : this.dependencies) {
			DependencyLabeller labeller = context.addDependency(dependency.type);
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
			ManagedObjectFunctionBuilder<Indexed, Indexed> function = mosContext
					.addManagedFunction("FUNCTION-" + teamName, this);
			function.setResponsibleTeam(teamName);
		}

		// Configure the extension interfaces
		for (Class extensionInterface : this.extensionInterfaces) {
			context.addManagedObjectExtension(extensionInterface, this);
		}
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		Assert.fail("Should not require obtaining the Managed Object for type testing");
		return null;
	}

	/*
	 * ====================== ManagedFunctionFactory ======================
	 */

	@Override
	public ManagedFunction<Indexed, Indexed> createManagedFunction() {
		Assert.fail("Should not require creating a function for type testing");
		return null;
	}

	/*
	 * ===================== ExtensionFactory ======================
	 */

	@Override
	public Object createExtension(ManagedObject managedObject) {
		Assert.fail("Should not require creating an extension for type testing");
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
		 * @param name      Name of dependency.
		 * @param type      Type of dependency.
		 * @param qualifier Qualifier of dependency type.
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
		 * @param name         Name of flow.
		 * @param argumentType Argument type for flow.
		 */
		public MockTypeFlow(String name, Class<?> argumentType) {
			this.name = name;
			this.argumentType = argumentType;
		}
	}

}
