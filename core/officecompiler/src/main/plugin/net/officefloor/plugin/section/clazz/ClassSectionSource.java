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

package net.officefloor.plugin.section.clazz;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import net.officefloor.compile.SectionSourceService;
import net.officefloor.compile.SectionSourceServiceFactory;
import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.section.FunctionObject;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.source.PrivateSource;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.plugin.clazz.dependency.ClassDependencyFactory;
import net.officefloor.plugin.clazz.method.AbstractFunctionManagedFunctionSource;
import net.officefloor.plugin.clazz.method.MethodManagedFunctionBuilder;
import net.officefloor.plugin.clazz.method.StaticMethodObjectFactory;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.section.clazz.loader.ClassSectionFunctionNamespace;
import net.officefloor.plugin.section.clazz.loader.ClassSectionLoader;
import net.officefloor.plugin.section.clazz.loader.FunctionClassSectionLoaderContext;
import net.officefloor.plugin.section.clazz.loader.FunctionDecoration;

/**
 * {@link Class} {@link SectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassSectionSource extends AbstractSectionSource
		implements SectionSourceService<ClassSectionSource>, SectionSourceServiceFactory {

	/**
	 * Name of the {@link SectionManagedObject} for the section class.
	 */
	public static final String CLASS_OBJECT_NAME = "OBJECT";

	/**
	 * Loads the {@link Class} {@link SectionFunction} instances.
	 * 
	 * @param sectionClass         Section {@link Class}.
	 * @param sectionManagedObject {@link SectionManagedObject} providing the
	 *                             {@link Class} object.
	 * @param isProvideInputs      Indicates if provide {@link SectionInput}
	 *                             instances for each {@link ManagedFunction}.
	 * @param loader               {@link ClassSectionLoader}.
	 * @param context              {@link SectionSourceContext}.
	 * @return {@link ClassSectionFunctionNamespace}.
	 * @throws Exception If fails to load the {@link SectionFunction} instances.
	 */
	public static ClassSectionFunctionNamespace loadClassFunctions(Class<?> sectionClass,
			SectionManagedObject sectionManagedObject, boolean isProvideInputs, ClassSectionLoader loader,
			SectionSourceContext context) throws Exception {

		// Load the functions
		PropertyList functionProperties = context.createPropertyList();
		functionProperties.addProperty(SectionClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME)
				.setValue(sectionClass.getName());
		return loader.addManagedFunctions("NAMESPACE", SectionClassManagedFunctionSource.class.getName(),
				functionProperties, new ClassSectionFunctionDecoration(sectionManagedObject, isProvideInputs));
	}

	/*
	 * ================ SectionSourceService =======================
	 */

	@Override
	public SectionSourceService<?> createService(ServiceContext context) throws Throwable {
		return this;
	}

	@Override
	public String getSectionSourceAlias() {
		return "CLASS";
	}

	@Override
	public Class<ClassSectionSource> getSectionSourceClass() {
		return ClassSectionSource.class;
	}

	/*
	 * =================== SectionSource ===========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No properties as uses location for class
	}

	@Override
	public void sourceSection(SectionDesigner designer, SectionSourceContext context) throws Exception {

		// Obtain the class name
		String sectionClassName = context.getSectionLocation();
		if ((sectionClassName == null) || (sectionClassName.trim().length() == 0)) {
			designer.addIssue("Must specify section class name within the location");
			return; // not able to load if no section class specified
		}

		// Obtain the class
		Class<?> sectionClass = context.loadClass(sectionClassName);

		// Ensure the section class has functions
		boolean hasFunctionMethod = false;
		HAS_METHOD: for (Method method : sectionClass.getMethods()) {
			if (!(method.getDeclaringClass().equals(Object.class))) {
				// Has non-object method
				hasFunctionMethod = true;
				break HAS_METHOD;
			}
		}
		if (!hasFunctionMethod) {
			throw designer.addIssue("Must have at least one public method on section class " + sectionClassName);
		}

		// Create the loader
		ClassSectionLoader loader = new ClassSectionLoader(designer, context);

		// Load the object for the section
		PropertyList properties = context.createPropertyList();
		properties.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME).setValue(sectionClassName);
		SectionManagedObject sectionObject = loader
				.addManagedObject(CLASS_OBJECT_NAME, ClassManagedObjectSource.class.getName(), properties, null)
				.getManagedObject();

		// Load the functions
		loadClassFunctions(sectionClass, sectionObject, true, loader, context);

		// Load the section configuration
		loader.load();
	}

	/**
	 * {@link FunctionDecoration} for the {@link ClassSectionSource}.
	 */
	private static class ClassSectionFunctionDecoration extends FunctionDecoration {

		/**
		 * {@link SectionManagedObject} containing the {@link Class} object.
		 */
		private final SectionManagedObject sectionObject;

		/**
		 * Indicates to provide {@link SectionInput} for each {@link SectionFunction}.
		 */
		private final boolean isProvideInput;

		/**
		 * Instantiate.
		 * 
		 * @param sectionObject  {@link SectionManagedObject} containing the
		 *                       {@link Class} object.
		 * @param isProvideInput Indicates to provide {@link SectionInput} for each
		 *                       {@link SectionFunction}.
		 */
		private ClassSectionFunctionDecoration(SectionManagedObject sectionObject, boolean isProvideInput) {
			this.sectionObject = sectionObject;
			this.isProvideInput = isProvideInput;
		}

		/*
		 * ===================== FunctionDecoration ============================
		 */

		@Override
		public void decorateSectionFunction(FunctionClassSectionLoaderContext functionContext) {

			// Obtain the details
			SectionFunction function = functionContext.getSectionFunction();
			SectionDesigner designer = functionContext.getSectionDesigner();

			// Link the input (if required)
			if (this.isProvideInput) {

				// Obtain the parameter type name
				Class<?> parameterType = functionContext.getParameterType();
				String parameterTypeName = (parameterType == null ? null : parameterType.getName());

				// Add input for function
				String inputName = function.getSectionFunctionName();
				SectionInput sectionInput = designer.addSectionInput(inputName, parameterTypeName);
				designer.link(sectionInput, function);
			}

			// Determine if static method
			ManagedFunctionType<?, ?> functionType = functionContext.getManagedFunctionType();
			StaticMethodAnnotation staticMethodAnnotation = functionType.getAnnotation(StaticMethodAnnotation.class);
			boolean isStaticMethod = staticMethodAnnotation != null;

			// Non-static methods require the section object as first object
			if (!isStaticMethod) {

				// Link to section object
				final int SECTION_OBJECT_INDEX = 0;
				ManagedFunctionObjectType<?> sectionClassObject = functionType.getObjectTypes()[SECTION_OBJECT_INDEX];
				FunctionObject objectSection = function.getFunctionObject(sectionClassObject.getObjectName());
				designer.link(objectSection, this.sectionObject);

				// Flag function object linked
				functionContext.flagFunctionObjectLinked(SECTION_OBJECT_INDEX);
			}
		}
	}

	/**
	 * {@link ManagedFunctionSource} implementation to provide the
	 * {@link ManagedFunction} instances for the {@link ClassSectionSource}.
	 */
	@PrivateSource
	public static class SectionClassManagedFunctionSource extends AbstractFunctionManagedFunctionSource {

		@Override
		protected ManagedFunctionTypeBuilder<Indexed, Indexed> buildMethod(Class<?> clazz, Method method,
				MethodManagedFunctionBuilder managedFunctionBuilder) throws Exception {

			// Determine if method is static
			boolean isStatic = Modifier.isStatic(method.getModifiers());

			// Build the method (using section object)
			ManagedFunctionTypeBuilder<Indexed, Indexed> function = managedFunctionBuilder.buildMethod(method,
					(context) -> {

						// No object required for static method
						if (isStatic) {
							return new StaticMethodObjectFactory();
						}

						// Create the class dependency factory for section object
						ClassDependencyFactory dependencyFactory = context.getClassDependencies()
								.createClassDependencyFactory(ClassSectionSource.CLASS_OBJECT_NAME, clazz, null);

						// Create factory to return section object
						return (managedFunctionContext) -> dependencyFactory.createDependency(managedFunctionContext);
					});

			// Flag as static
			if (isStatic) {
				function.addAnnotation(new StaticMethodAnnotation());
			}

			// Return the function
			return function;
		}
	}

}
