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

package net.officefloor.plugin.clazz.factory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.plugin.clazz.constructor.ClassConstructorInterrogatorServiceFactory;
import net.officefloor.plugin.clazz.dependency.ClassDependencies;
import net.officefloor.plugin.clazz.dependency.ClassDependencyFactory;
import net.officefloor.plugin.clazz.interrogate.ClassInjections;
import net.officefloor.plugin.clazz.qualifier.TypeQualifierInterrogation;
import net.officefloor.plugin.clazz.state.StatePoint;
import net.officefloor.plugin.managedobject.clazz.ClassDependencyInjector;
import net.officefloor.plugin.managedobject.clazz.FieldClassDependencyInjector;
import net.officefloor.plugin.managedobject.clazz.MethodClassDependencyInjector;

/**
 * Manufactures a {@link ClassObjectFactory} from {@link Class}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassObjectManufacturer {

	/**
	 * {@link ClassDependencies}.
	 */
	private final ClassDependencies dependencies;

	/**
	 * {@link SourceContext}.
	 */
	private final SourceContext sourceContext;

	/**
	 * Instantiate.
	 * 
	 * @param dependencies  {@link ClassDependencies}.
	 * @param sourceContext {@link SourceContext}.
	 */
	public ClassObjectManufacturer(ClassDependencies dependencies, SourceContext sourceContext) {
		this.dependencies = dependencies;
		this.sourceContext = sourceContext;
	}

	/**
	 * Constructs the {@link ClassObjectFactory}.
	 * 
	 * @param objectClass {@link Class} of object for {@link ClassObjectFactory}.
	 * @return {@link ClassObjectFactory}.
	 * @throws Exception If fails to construct {@link ClassObjectFactory}.
	 */
	public ClassObjectFactory constructClassObjectFactory(Class<?> objectClass) throws Exception {

		// Obtain the constructor
		Constructor<?> objectConstructor = ClassConstructorInterrogatorServiceFactory.extractConstructor(objectClass,
				this.sourceContext);

		// Create the type qualification
		TypeQualifierInterrogation qualifierInterrogation = new TypeQualifierInterrogation(this.sourceContext);

		// Obtain the constructor dependency factories
		int constructorParameterCount = objectConstructor.getParameterCount();
		ClassDependencyFactory[] constructorDependencyFactories = new ClassDependencyFactory[constructorParameterCount];
		for (int i = 0; i < constructorParameterCount; i++) {

			// Determine the qualifier
			String qualifier = qualifierInterrogation.extractTypeQualifier(StatePoint.of(objectConstructor, i));
			
			// Obtain the parameter factories to construct object
			constructorDependencyFactories[i] = this.dependencies.createClassDependencyFactory(objectConstructor, i,
					qualifier);
		}

		// Interrogate dependency injection fields and methods
		ClassInjections injections = new ClassInjections(objectClass, this.sourceContext);

		// Listing of injectors
		List<ClassDependencyInjector> injectors = new LinkedList<>();

		// Load the fields
		for (Field field : injections.getInjectionFields()) {

			// Determine the qualifier
			String qualifier = qualifierInterrogation.extractTypeQualifier(StatePoint.of(field));

			// Create the dependency factory
			ClassDependencyFactory factory = this.dependencies.createClassDependencyFactory(field, qualifier);

			// Add the field injector
			injectors.add(new FieldClassDependencyInjector(field, factory));
		}

		// Provide loading methods
		MethodsLoader methodsLoader = (methods) -> {

			// Load the methods
			for (Method method : methods) {

				// Obtain the method dependency factories
				int methodParameterCount = method.getParameterCount();
				ClassDependencyFactory[] parameterFactories = new ClassDependencyFactory[methodParameterCount];
				for (int i = 0; i < methodParameterCount; i++) {

					// Determine the qualifier
					String qualifier = qualifierInterrogation.extractTypeQualifier(StatePoint.of(method, i));

					// Obtain the parameter factory to invoke method
					parameterFactories[i] = this.dependencies.createClassDependencyFactory(method, i, qualifier);
				}

				// Add the method injector
				injectors.add(new MethodClassDependencyInjector(method, parameterFactories));
			}
		};

		// Load the dependency injection methods
		methodsLoader.loadMethods(injections.getInjectionMethods());

		// Provide the initialisation
		methodsLoader.loadMethods(injections.getPostConstructMethods());

		// Capture the dependency injectors
		ClassDependencyInjector[] dependencyInjectors = injectors
				.toArray(new ClassDependencyInjector[injectors.size()]);

		// Create and return factory
		return new ClassObjectFactory(objectConstructor, constructorDependencyFactories, dependencyInjectors);
	}

	/**
	 * Loads the {@link Method}.
	 */
	@FunctionalInterface
	private static interface MethodsLoader {
		void loadMethods(List<Method> methods) throws Exception;
	}

}
