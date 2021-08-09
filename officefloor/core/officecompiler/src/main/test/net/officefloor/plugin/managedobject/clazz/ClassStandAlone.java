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

package net.officefloor.plugin.managedobject.clazz;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectFlowType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.util.InvokedProcessServicer;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.plugin.clazz.InvalidConfigurationError;
import net.officefloor.plugin.clazz.dependency.ClassDependenciesManager;

/**
 * <p>
 * Loads {@link Class} via {@link ClassManagedObjectSource} for stand alone use.
 * <p>
 * This is typically for unit testing of the {@link Class} with mock injections.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassStandAlone {

	/**
	 * {@link RegisteredDependency} instances.
	 */
	private final List<RegisteredDependency> registeredDependencies = new LinkedList<>();

	/**
	 * {@link RegisteredFlow} instances.
	 */
	private final List<RegisteredFlow> registeredFlows = new LinkedList<>();

	/**
	 * Registers an unqualified dependency for concrete type.
	 * 
	 * @param dependency Dependency.
	 */
	public void registerDependency(Object dependency) {
		this.registerDependency((String) null, dependency);
	}

	/**
	 * Registers a qualified dependency for concrete type.
	 * 
	 * @param qualifier  Qualifier.
	 * @param dependency Dependency.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void registerDependency(String qualifier, Object dependency) {
		this.registerDependency(qualifier, (Class) dependency.getClass(), dependency);
	}

	/**
	 * Registers an unqualified dependency for a dependency type.
	 * 
	 * @param <T>        Dependency type.
	 * @param <I>        Implementation type.
	 * @param type       Dependency type.
	 * @param dependency Implementing dependency.
	 */
	public <T, I extends T> void registerDependency(Class<T> type, I dependency) {
		this.registerDependency(null, type, dependency);
	}

	/**
	 * Registers a dependency for a dependency type.
	 * 
	 * @param <T>        Dependency type.
	 * @param <I>        Implementation type.
	 * @param qualifier  Qualifier.
	 * @param type       Dependency type.
	 * @param dependency Implementing dependency.
	 */
	public <T, I extends T> void registerDependency(String qualifier, Class<T> type, I dependency) {
		this.registeredDependencies.add(new RegisteredDependency(qualifier, type, dependency));
	}

	/**
	 * Registers an invoked {@link Flow} (process).
	 * 
	 * @param flowName Name of {@link Flow}.
	 * @param servicer {@link InvokedProcessServicer}.
	 */
	public void registerFlow(String flowName, InvokedProcessServicer servicer) {
		this.registeredFlows.add(new RegisteredFlow(flowName, servicer));
	}

	/**
	 * Instantiates the objects and injects the dependencies.
	 * 
	 * @param <T>   Object type.
	 * @param clazz Object {@link Class}.
	 * @return Instantiated object with dependencies injected.
	 * @throws Throwable If fails to create.
	 */
	@SuppressWarnings("unchecked")
	public <T> T create(Class<T> clazz) throws Throwable {

		// Load the managed object type
		ManagedObjectType<Indexed> moType = ManagedObjectLoaderUtil.loadManagedObjectType(
				ClassManagedObjectSource.class, ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, clazz.getName());

		// Create the source
		ManagedObjectSourceStandAlone standAlone = new ManagedObjectSourceStandAlone();

		// Create the indexing of the flows
		ManagedObjectFlowType<?>[] flowTypes = moType.getFlowTypes();
		FOUND_FLOW: for (int i = 0; i < flowTypes.length; i++) {
			ManagedObjectFlowType<?> flowType = flowTypes[i];

			// Obtain the flow name
			String flowName = flowType.getFlowName();

			// Find the corresponding flow
			for (RegisteredFlow registered : this.registeredFlows) {
				if (flowName.equals(registered.name)) {

					// Register the flow servicer
					standAlone.registerInvokeProcessServicer(i, registered.servicer);
					continue FOUND_FLOW;
				}
			}

			// As here, flow not found
			throw new InvalidConfigurationError("No flow registered for " + flowName);
		}

		// Load the managed object source
		standAlone.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, clazz.getName());
		ClassManagedObjectSource mos = standAlone.loadManagedObjectSource(ClassManagedObjectSource.class);

		// Create the user
		ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();

		// Create the indexing of the dependencies
		ManagedObjectDependencyType<Indexed>[] dependencyTypes = moType.getDependencyTypes();
		FOUND_DEPENDENCY: for (int i = 0; i < dependencyTypes.length; i++) {
			ManagedObjectDependencyType<Indexed> dependencyType = dependencyTypes[i];

			// Obtain the required details
			String qualifier = dependencyType.getTypeQualifier();
			Class<?> objectType = dependencyType.getDependencyType();

			// Find the corresponding dependency
			for (RegisteredDependency registered : this.registeredDependencies) {
				if (ClassDependenciesManager.isSameQualifier(qualifier, registered.qualifier)
						&& ClassDependenciesManager.isSameObjectType(objectType, registered.objectType)) {

					// Register the dependency
					user.mapDependency(i, registered.dependency);
					continue FOUND_DEPENDENCY;
				}
			}

			// As here, dependency not found
			throw new InvalidConfigurationError("No dependency registered for "
					+ (qualifier != null ? qualifier + "-" : "") + objectType.getName());
		}

		// Source the managed object
		ManagedObject mo = user.sourceManagedObject(mos);

		// Return the created object
		return (T) mo.getObject();
	}

	/**
	 * Registered dependency.
	 */
	private static class RegisteredDependency {

		/**
		 * Qualifier.
		 */
		private final String qualifier;

		/**
		 * Object type.
		 */
		private final Class<?> objectType;

		/**
		 * Dependency.
		 */
		private final Object dependency;

		/**
		 * Instantiate.
		 * 
		 * @param qualifier  Qualifier.
		 * @param objectType Object type.
		 * @param dependency Dependency.
		 */
		private RegisteredDependency(String qualifier, Class<?> objectType, Object dependency) {
			this.qualifier = qualifier;
			this.objectType = objectType;
			this.dependency = dependency;
		}
	}

	/**
	 * Registered flow.
	 */
	private static class RegisteredFlow {

		/**
		 * Name of {@link Flow}.
		 */
		private final String name;

		/**
		 * {@link InvokedProcessServicer}.
		 */
		private final InvokedProcessServicer servicer;

		/**
		 * Instantiate.
		 * 
		 * @param name     Name of {@link Flow}.
		 * @param servicer {@link InvokedProcessServicer}.
		 */
		private RegisteredFlow(String name, InvokedProcessServicer servicer) {
			this.name = name;
			this.servicer = servicer;
		}
	}

}
