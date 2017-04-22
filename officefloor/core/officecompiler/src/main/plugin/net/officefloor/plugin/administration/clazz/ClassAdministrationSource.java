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
package net.officefloor.plugin.administration.clazz;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import net.officefloor.compile.AdministrationSourceService;
import net.officefloor.compile.spi.administration.source.AdministrationSource;
import net.officefloor.compile.spi.administration.source.AdministrationSourceContext;
import net.officefloor.compile.spi.administration.source.impl.AbstractAdministratorSource;
import net.officefloor.frame.api.administration.AdministrationContext;
import net.officefloor.frame.api.administration.GovernanceManager;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.plugin.clazz.ClassFlowBuilder;
import net.officefloor.plugin.clazz.ClassFlowParameterFactory;
import net.officefloor.plugin.clazz.ClassFlowRegistry;
import net.officefloor.plugin.clazz.Sequence;
import net.officefloor.plugin.managedfunction.clazz.FlowInterface;

/**
 * {@link AdministrationSource} that delegates to {@link Object}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassAdministrationSource extends AbstractAdministratorSource<Object, Indexed, Indexed>
		implements AdministrationSourceService<Object, Indexed, Indexed, ClassAdministrationSource> {

	/**
	 * Property name providing the {@link Class} name.
	 */
	public static final String CLASS_NAME_PROPERTY_NAME = "class.name";

	/**
	 * {@link ParameterManufacturer} instances.
	 */
	private final List<ParameterManufacturer> manufacturers = new LinkedList<ParameterManufacturer>();

	/**
	 * Initiate.
	 */
	public ClassAdministrationSource() {
		// Add the default manufacturers
		this.manufacturers.add(new AdministrationContextParameterManufacturer());
		this.manufacturers.add(new ExtensionParameterManufacturer());
		this.manufacturers.add(new FlowParameterManufacturer<FlowInterface>(FlowInterface.class));
		this.manufacturers.add(new GovernanceManagerParameterManufacturer());

		// Load any additional manufacturers
		this.loadParameterManufacturers(this.manufacturers);
	}

	/**
	 * Override to add additional {@link ParameterManufacturer} instances.
	 * 
	 * @param manufacturers
	 *            List of {@link ParameterManufacturer} instances to use.
	 */
	protected void loadParameterManufacturers(List<ParameterManufacturer> manufacturers) {
		// By default adds no further manufacturers
	}

	/*
	 * =================== AdministrationSourceService ===================
	 */

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
		Arrays.sort(methods, new Comparator<Method>() {
			@Override
			public int compare(Method a, Method b) {
				return a.getName().compareTo(b.getName());
			}
		});

		// Interrogate for administration methods and extension interface
		Method adminMethod = null;
		AdministrationParameterFactory[] adminParameterFactories = null;
		NEXT_METHOD: for (Method method : methods) {

			// Obtain the method name
			String methodName = method.getName();

			// Obtain the parameter types
			Class<?>[] paramTypes = method.getParameterTypes();
			if (paramTypes.length == 0) {
				continue NEXT_METHOD; // must have at least one parameter
			}

			// Means to specify the extension interface
			Class[] extensionInterface = new Class[1];
			Consumer<Class<?>> extensionInterfaceConsumer = (extension) -> {
				extensionInterface[0] = extension;
			};

			// Sequencer for the flows and governance
			Sequence flowSequence = new Sequence();
			Sequence governanceSequence = new Sequence();

			// Load the parameter factories for each parameter
			AdministrationParameterFactory[] parameterFactories = new AdministrationParameterFactory[paramTypes.length];
			for (int i = 0; i < paramTypes.length; i++) {
				Class<?> paramType = paramTypes[i];

				// Find the parameter factory
				AdministrationParameterFactory parameterFactory = null;
				FIND_FACTORY: for (ParameterManufacturer manufacturer : this.manufacturers) {
					parameterFactory = manufacturer.createParameterFactory(methodName, paramType, context, flowSequence,
							governanceSequence, extensionInterfaceConsumer);
					if (parameterFactory != null) {
						break FIND_FACTORY;
					}
				}
				if (parameterFactory == null) {
					continue NEXT_METHOD; // must have factory
				}

				// Load the factory
				parameterFactories[i] = parameterFactory;
			}

			// Ensure only the one administration method
			if (adminMethod != null) {
				throw new Exception("Only one method on class " + objectClass.getName() + " should be administration ("
						+ method.getName() + ", " + adminMethod.getName() + ")");
			}

			// Use the method
			adminMethod = method;
			adminParameterFactories = parameterFactories;

			// Provide the extension interface
			context.setExtensionInterface(extensionInterface[0]);

			// Load the escalations
			for (Class<?> escalationType : adminMethod.getExceptionTypes()) {
				context.addEscalation((Class<? extends Throwable>) escalationType);
			}
		}
		if (adminMethod == null) {
			throw new Exception("No administration method on class " + objectClass.getName());
		}

		// Provide the administration factory
		boolean isStatic = Modifier.isStatic(adminMethod.getModifiers());
		Constructor<?> constructor = isStatic ? null : objectClass.getConstructor(new Class<?>[0]);
		context.setAdministrationFactory(new ClassAdministration(constructor, adminMethod, adminParameterFactories));
	}

	/**
	 * Manufactures the {@link AdministrationParameterFactory}.
	 */
	protected static interface ParameterManufacturer {

		/**
		 * Creates the {@link AdministrationParameterFactory}.
		 * 
		 * @param methodName
		 *            Name of the {@link Method}.
		 * @param parameterType
		 *            Parameter type.
		 * @param context
		 *            {@link MetaDataContext}.
		 * @param flowSequence
		 *            {@link Sequence} for the {@link Flow}.
		 * @param governanceSequence
		 *            {@link Sequence} for the {@link Governance}.
		 * @param extensionInterfaceConsumer
		 *            {@link Consumer} to optionally be provided the extension
		 *            type.
		 * @return {@link AdministrationParameterFactory} or <code>null</code>
		 *         if not appropriate for this to manufacture a
		 *         {@link AdministrationParameterFactory}.
		 * @throws Exception
		 *             If fails to create the
		 *             {@link AdministrationParameterFactory}.
		 */
		AdministrationParameterFactory createParameterFactory(String functionName, Class<?> parameterType,
				MetaDataContext<Object, Indexed, Indexed> context, Sequence flowSequence, Sequence governanceSequence,
				Consumer<Class<?>> extensionInterfaceConsumer) throws Exception;
	}

	/**
	 * {@link ParameterManufacturer} for the {@link AdministrationContext}.
	 */
	protected static class AdministrationContextParameterManufacturer implements ParameterManufacturer {

		@Override
		public AdministrationParameterFactory createParameterFactory(String functionName, Class<?> parameterType,
				MetaDataContext<Object, Indexed, Indexed> context, Sequence flowSequence, Sequence governanceSequence,
				Consumer<Class<?>> extensionInterfaceConsumer) throws Exception {

			// Determine if administration context
			if (!AdministrationContext.class.isAssignableFrom(parameterType)) {
				return null; // not administration context
			}

			// Return the context parameter factory
			return new AdministrationContextParameterFactory();
		}
	}

	/**
	 * {@link ParameterManufacturer} for the extensions.
	 */
	protected static class ExtensionParameterManufacturer implements ParameterManufacturer {

		@Override
		public AdministrationParameterFactory createParameterFactory(String functionName, Class<?> parameterType,
				MetaDataContext<Object, Indexed, Indexed> context, Sequence flowSequence, Sequence governanceSequence,
				Consumer<Class<?>> extensionInterfaceConsumer) throws Exception {

			// Must be an array of extensions
			if (!(parameterType.isArray())) {
				return null; // must be an array of extensions
			}

			// Extension interface is component type for the array
			Class<?> extensionInterface = parameterType.getComponentType();

			// Provide the extension interface
			extensionInterfaceConsumer.accept(extensionInterface);

			// Return the extension interface factory
			return new AdministrationExtensionParameterFactory();
		}
	}

	/**
	 * {@link ParameterManufacturer} for the {@link FlowInterface}.
	 */
	protected static class FlowParameterManufacturer<A extends Annotation> implements ParameterManufacturer {

		/**
		 * {@link Annotation} {@link Class}.
		 */
		private final Class<A> annotationClass;

		/**
		 * Instantiate.
		 * 
		 * @param annotationClass
		 *            {@link Class} of the {@link Annotation}.
		 */
		public FlowParameterManufacturer(Class<A> annotationClass) {
			this.annotationClass = annotationClass;
		}

		@Override
		public AdministrationParameterFactory createParameterFactory(String functionName, Class<?> parameterType,
				MetaDataContext<Object, Indexed, Indexed> context, Sequence flowSequence, Sequence governanceSequence,
				Consumer<Class<?>> extensionInterfaceConsumer) throws Exception {

			// Obtain flow interface details
			ClassFlowRegistry flowRegistry = (label, flowParameterType) -> {
				// Register the flow
				context.addFlow(flowParameterType).setLabel(label);
			};
			ClassLoader classLoader = context.getAdministrationSourceContext().getClassLoader();

			// Build the flow parameter factory
			ClassFlowParameterFactory flowParameterFactory = new ClassFlowBuilder<A>(this.annotationClass)
					.buildFlowParameterFactory(functionName, parameterType, flowSequence, flowRegistry, classLoader);
			if (flowParameterFactory == null) {
				return null; // not flow interface
			}

			// Return the flow parameter factory
			return new AdministrationFlowParameterFactory(flowParameterFactory);
		}
	}

	/**
	 * {@link ParameterManufacturer} for the {@link GovernanceManager}.
	 */
	protected static class GovernanceManagerParameterManufacturer implements ParameterManufacturer {

		@Override
		public AdministrationParameterFactory createParameterFactory(String functionName, Class<?> parameterType,
				MetaDataContext<Object, Indexed, Indexed> context, Sequence flowSequence, Sequence governanceSequence,
				Consumer<Class<?>> extensionInterfaceConsumer) throws Exception {

			// Determine if governance manager
			if (!GovernanceManager.class.isAssignableFrom(parameterType)) {
				return null; // not governance manager
			}

			// Return the governance parameter factory
			int governanceIndex = governanceSequence.nextIndex();
			context.addGovernance();
			return new AdministrationGovernanceParameterFactory(governanceIndex);
		}
	}

}