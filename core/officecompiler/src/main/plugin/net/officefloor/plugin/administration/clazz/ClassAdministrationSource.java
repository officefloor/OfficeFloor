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

package net.officefloor.plugin.administration.clazz;

import java.lang.reflect.Method;
import java.util.Arrays;

import net.officefloor.compile.AdministrationSourceService;
import net.officefloor.compile.AdministrationSourceServiceFactory;
import net.officefloor.compile.spi.administration.source.AdministrationSource;
import net.officefloor.compile.spi.administration.source.AdministrationSourceContext;
import net.officefloor.compile.spi.administration.source.impl.AbstractAdministrationSource;
import net.officefloor.frame.api.administration.AdministrationContext;
import net.officefloor.frame.api.administration.GovernanceManager;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.plugin.clazz.dependency.ClassDependenciesFlowContext;
import net.officefloor.plugin.clazz.dependency.ClassDependenciesManager;
import net.officefloor.plugin.clazz.dependency.ClassDependencyFactory;
import net.officefloor.plugin.clazz.dependency.ClassDependencyManufacturer;
import net.officefloor.plugin.clazz.dependency.ClassDependencyManufacturerContext;
import net.officefloor.plugin.clazz.dependency.ClassItemIndex;
import net.officefloor.plugin.clazz.factory.ClassObjectFactory;
import net.officefloor.plugin.clazz.factory.ClassObjectManufacturer;

/**
 * {@link AdministrationSource} that delegates to {@link Object}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassAdministrationSource extends AbstractAdministrationSource<Object, Indexed, Indexed>
		implements AdministrationSourceService<Object, Indexed, Indexed, ClassAdministrationSource>,
		AdministrationSourceServiceFactory {

	/**
	 * Property name providing the {@link Class} name.
	 */
	public static final String CLASS_NAME_PROPERTY_NAME = "class.name";

	/*
	 * =================== AdministrationSourceService ===================
	 */

	@Override
	public AdministrationSourceService<?, ?, ?, ?> createService(ServiceContext context) throws Throwable {
		return this;
	}

	@Override
	public String getAdministrationSourceAlias() {
		return "CLASS";
	}

	@Override
	public Class<ClassAdministrationSource> getAdministrationSourceClass() {
		return ClassAdministrationSource.class;
	}

	/*
	 * =================== AbstractAdministrationSource =================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(CLASS_NAME_PROPERTY_NAME, "Class");
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void loadMetaData(MetaDataContext<Object, Indexed, Indexed> context) throws Exception {
		AdministrationSourceContext adminContext = context.getAdministrationSourceContext();

		// Obtain the administrator class
		String adminClassName = adminContext.getProperty(CLASS_NAME_PROPERTY_NAME);
		Class<?> objectClass = adminContext.getClassLoader().loadClass(adminClassName);

		// Obtain the methods of class in sorted order (maintains indexes)
		Method[] methods = objectClass.getMethods();
		Arrays.sort(methods, (a, b) -> a.getName().compareTo(b.getName()));

		// Interrogate for administration methods and extension interface
		Method adminMethod = null;
		NEXT_METHOD: for (Method method : methods) {

			// Must have one parameter that is array
			Class<?>[] paramTypes = method.getParameterTypes();
			int arrayParameterCount = 0;
			for (Class<?> paramType : paramTypes) {
				if (paramType.isArray()) {
					arrayParameterCount++;
				}
			}
			if (arrayParameterCount != 1) {
				continue NEXT_METHOD; // must have at one array parameter
			}

			// Ensure only the one administration method
			if (adminMethod != null) {
				throw new Exception("Only one method on class " + objectClass.getName() + " should be administration ("
						+ method.getName() + ", " + adminMethod.getName() + ")");
			}

			// Use the method
			adminMethod = method;
		}
		if (adminMethod == null) {
			throw new Exception("No administration method on class " + objectClass.getName());
		}

		// Create manager for dependencies
		ClassDependenciesManager dependencies = ClassDependenciesManager.createNoObjects(objectClass, adminContext,
				new ClassDependenciesFlowContext() {

					@Override
					public ClassItemIndex addFlow(String flowName, Class<?> argumentType, Object[] annotations) {
						int index = context.addFlow(argumentType).setLabel(flowName).getIndex();
						return ClassDependenciesManager.createClassItemIndex(index, null);
					}

					@Override
					public void addEscalation(Class<? extends Throwable> escalationType) {
						context.addEscalation(escalationType);
					}

					@Override
					public void addAnnotation(Object annotation) {
						// No administrator annotations
					}
				});

		// Add the additional dependency extensions
		dependencies.addClassDependencyManufacturer(new AdministrationContextClassDependencyManufacturer());
		dependencies.addClassDependencyManufacturer(new GovernanceManagerClassDependencyManufacturer(context));
		ExtensionClassDependencyManufacturer extensionManufacturer = new ExtensionClassDependencyManufacturer();
		dependencies.addClassDependencyManufacturer(extensionManufacturer);

		// Obtain factory to create the object
		ClassObjectFactory objectFactory = new ClassObjectManufacturer(dependencies, adminContext)
				.constructClassObjectFactory(objectClass);

		// Load the parameter factories for each parameter
		ClassDependencyFactory[] parameterFactories = new ClassDependencyFactory[adminMethod
				.getParameterTypes().length];
		for (int i = 0; i < parameterFactories.length; i++) {
			parameterFactories[i] = dependencies.createClassDependencyFactory(adminMethod, i, null);
		}

		// Provide the extension interface
		context.setExtensionInterface((Class) extensionManufacturer.extensionInterface);

		// Load the escalations
		for (Class<?> escalationType : adminMethod.getExceptionTypes()) {
			dependencies.addEscalation((Class<? extends Throwable>) escalationType);
		}

		// Provide the administration factory
		context.setAdministrationFactory(new ClassAdministration(objectFactory, adminMethod, parameterFactories));
	}

	/**
	 * {@link ClassDependencyManufacturer} for the {@link AdministrationContext}.
	 */
	private static class AdministrationContextClassDependencyManufacturer implements ClassDependencyManufacturer {

		@Override
		public ClassDependencyFactory createParameterFactory(ClassDependencyManufacturerContext context) {

			// Determine if administration context
			Class<?> dependencyType = context.getDependencyClass();
			if (!AdministrationContext.class.isAssignableFrom(dependencyType)) {
				return null; // not administration context
			}

			// Return the context parameter factory
			return new AdministrationContextClassDependencyFactory();
		}
	}

	/**
	 * {@link ClassDependencyManufacturer} for the extensions.
	 */
	private static class ExtensionClassDependencyManufacturer implements ClassDependencyManufacturer {

		/**
		 * Extension interface.
		 */
		private Class<?> extensionInterface = null;

		/*
		 * ================== ClassDependencyManufacturer =========================
		 */

		@Override
		public ClassDependencyFactory createParameterFactory(ClassDependencyManufacturerContext context)
				throws Exception {

			// Must be an array of extensions
			Class<?> dependencyType = context.getDependencyClass();
			if (!(dependencyType.isArray())) {
				return null; // must be an array of extensions
			}

			// Extension interface is component type for the array
			this.extensionInterface = dependencyType.getComponentType();

			// Return the extension interface factory
			return new AdministrationExtensionParameterFactory();
		}
	}

	/**
	 * {@link ClassDependencyManufacturer} for the {@link GovernanceManager}.
	 */
	private static class GovernanceManagerClassDependencyManufacturer implements ClassDependencyManufacturer {

		/**
		 * {@link MetaDataContext}.
		 */
		private final MetaDataContext<?, ?, ?> metaDataContext;

		/**
		 * Instantiate.
		 * 
		 * @param metaDataContext {@link MetaDataContext}.
		 */
		private GovernanceManagerClassDependencyManufacturer(MetaDataContext<?, ?, ?> metaDataContext) {
			this.metaDataContext = metaDataContext;
		}

		/*
		 * ===================== ClassDependencyManufacturer =======================
		 */

		@Override
		public ClassDependencyFactory createParameterFactory(ClassDependencyManufacturerContext context)
				throws Exception {

			// Determine if governance manager
			Class<?> dependencyType = context.getDependencyClass();
			if (!GovernanceManager.class.isAssignableFrom(dependencyType)) {
				return null; // not governance manager
			}

			// Return the governance parameter factory
			int governanceIndex = this.metaDataContext.addGovernance().getIndex();
			return new AdministrationGovernanceParameterFactory(governanceIndex);
		}
	}

}
