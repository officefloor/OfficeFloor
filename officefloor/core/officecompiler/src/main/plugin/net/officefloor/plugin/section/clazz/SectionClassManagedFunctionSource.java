/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.section.clazz;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionObjectTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.frame.api.source.PrivateSource;
import net.officefloor.plugin.clazz.ClassFlowMethodMetaData;
import net.officefloor.plugin.clazz.FlowInterface;
import net.officefloor.plugin.managedfunction.clazz.ClassManagedFunctionSource;
import net.officefloor.plugin.managedfunction.method.AbstractFunctionManagedFunctionSource;
import net.officefloor.plugin.managedfunction.method.MethodFunction;
import net.officefloor.plugin.managedfunction.method.MethodManagedFunctionBuilder;
import net.officefloor.plugin.managedfunction.method.parameter.ManagedFunctionFlowParameterFactory;
import net.officefloor.plugin.managedfunction.method.parameter.ManagedFunctionParameterFactory;

/**
 * {@link ManagedFunctionSource} implementation to provide the
 * {@link ManagedFunction} instances for the {@link ClassSectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
@PrivateSource
public class SectionClassManagedFunctionSource extends AbstractFunctionManagedFunctionSource {

	/*
	 * =================== ClassManagedFunctionSource ==========================
	 */

	@Override
	protected MethodManagedFunctionBuilder createMethodManagedFunctionBuilder(FunctionNamespaceBuilder namespaceBuilder,
			ManagedFunctionSourceContext context) throws Exception {
		return new SectionMethodManagedFunctionBuilder();
	}

	/**
	 * {@link MethodManagedFunctionBuilder} for the {@link SectionSource}.
	 */
	protected class SectionMethodManagedFunctionBuilder extends MethodManagedFunctionBuilder {

		/*
		 * ===================== MethodManagedFunctionBuilder ======================
		 */

		@Override
		protected void loadParameterManufacturers(List<ParameterManufacturer> manufacturers) {
			manufacturers.add(new FlowParameterManufacturer<SectionInterface>(SectionInterface.class));
		}

		@Override
		protected ManagedFunctionFactory<Indexed, Indexed> createManagedFunctionFactory(
				MethodManagedFunctionFactoryContext context) throws Exception {
			boolean isStatic = (context.getMethodObjectInstanceFactory() == null);
			return new SectionManagedFunctionFactory(context.getMethod(), isStatic, context.getParameters());
		}

		@Override
		protected ManagedFunctionTypeBuilder<Indexed, Indexed> addManagedFunctionType(
				MethodManagedFunctionTypeContext context) throws Exception {

			// Include method as function in type definition
			ManagedFunctionTypeBuilder<Indexed, Indexed> functionTypeBuilder = context.getNamespaceBuilder()
					.addManagedFunctionType(context.getFunctionName(), context.getFunctionFactory(), Indexed.class,
							Indexed.class);

			// Add the section object always as first dependency
			functionTypeBuilder.addObject(context.getInstanceClass()).setLabel("OBJECT");
			context.nextObjectIndex(); // index for section object

			// Return the function type builder
			return functionTypeBuilder;
		}

		@Override
		protected void enrichManagedFunctionType(EnrichManagedFunctionTypeContext context) {

			// Obtain the parameter for the function
			Class<?> parameterType = null;
			Class<?>[] parameters = context.getMethod().getParameterTypes();
			Annotation[][] parametersAnnotations = context.getMethod().getParameterAnnotations();
			int objectIndex = 0;
			for (int i = 0; i < parameters.length; i++) {
				Class<?> parameter = parameters[i];
				Annotation[] parameterAnnotations = parametersAnnotations[i];

				// Determine if flow or section interface (not object)
				if ((parameter.getAnnotation(FlowInterface.class) != null)
						|| (parameter.getAnnotation(SectionInterface.class) != null)) {
					continue; // ignore flow and section interfaces
				}

				// Determine if the parameter
				boolean isParameter = false;
				for (Annotation annotation : parameterAnnotations) {
					if (Parameter.class.equals(annotation.annotationType())) {
						isParameter = true;
					}
				}

				// Register as parameter
				if (isParameter) {

					// Ensure only one parameter
					if (parameterType != null) {
						throw new IllegalStateException("Method " + context.getFunctionName()
								+ " may only have one parameter annotated with " + Parameter.class.getSimpleName());
					}
					parameterType = parameter;

					// Add the parameter annotation
					context.getManagedFunctionTypeBuilder()
							.addAnnotation(new ParameterAnnotation(parameterType, objectIndex));
				}

				// Increment to next object index
				objectIndex++;
			}

			// Obtain the next
			Supplier<Class<?>> nextArgumentType = () -> {

				// Obtain the argument type for the function
				Class<?> returnType = context.getMethod().getReturnType();
				Class<?> argumentType = ((returnType == null) || (void.class.equals(returnType))
						|| (Void.TYPE.equals(returnType))) ? null : returnType;

				// Return the argument type
				return argumentType;
			};
			Next next = context.getMethod().getAnnotation(Next.class);
			if (next != null) {
				context.getManagedFunctionTypeBuilder().addAnnotation(new NextAnnotation(next, nextArgumentType.get()));
			}

			// TODO: deprecated, but still handle NextFunction
			@SuppressWarnings("deprecation")
			NextFunction nextFunction = context.getMethod().getAnnotation(NextFunction.class);
			if (nextFunction != null) {
				context.getManagedFunctionTypeBuilder()
						.addAnnotation(new NextAnnotation(nextFunction, nextArgumentType.get()));
			}

			// Obtain the flow meta-data for the function
			List<FlowAnnotation> flowAnnotations = new LinkedList<>();
			List<SectionInterfaceAnnotation> sectionAnnotations = new LinkedList<>();
			ManagedFunctionParameterFactory[] parameterFactories = context.getParameters();
			for (ManagedFunctionParameterFactory factory : parameterFactories) {

				// Ignore if not flow parameter factory
				if (!(factory instanceof ManagedFunctionFlowParameterFactory)) {
					continue; // ignore as not flow parameter factory
				}
				ManagedFunctionFlowParameterFactory flowParameterFactory = (ManagedFunctionFlowParameterFactory) factory;

				// Add the flow meta-data
				for (ClassFlowMethodMetaData metaData : flowParameterFactory.getFlowMethodMetaData()) {

					// Determine if sub section
					Class<?> flowType = metaData.getFlowType();
					SectionInterface sectionInterface = flowType.getAnnotation(SectionInterface.class);
					if (sectionInterface != null) {

						// Add the section
						String sectionName = flowType.getSimpleName();
						SectionInterfaceAnnotation sectionInterfaceAnnotation = new SectionInterfaceAnnotation(
								metaData.getMethod().getName(), metaData.getFlowIndex(), metaData.isSpawn(),
								metaData.getParameterType(), metaData.isFlowCallback(), sectionName, sectionInterface);
						sectionAnnotations.add(sectionInterfaceAnnotation);

					} else {
						// Load flow annotation
						flowAnnotations.add(new FlowAnnotation(metaData.getMethod().getName(), metaData.getFlowIndex(),
								metaData.isSpawn(), metaData.getParameterType(), metaData.isFlowCallback()));
					}
				}
			}
			if (flowAnnotations.size() > 0) {
				context.getManagedFunctionTypeBuilder()
						.addAnnotation(flowAnnotations.toArray(new FlowAnnotation[flowAnnotations.size()]));
			}
			if (sectionAnnotations.size() > 0) {
				context.getManagedFunctionTypeBuilder().addAnnotation(
						sectionAnnotations.toArray(new SectionInterfaceAnnotation[sectionAnnotations.size()]));
			}
		}

		@Override
		protected void enrichManagedFunctionObjectType(Class<?> objectType, Type genericType, Annotation[] annotations,
				ManagedFunctionObjectTypeBuilder<Indexed> functionObjectType) {
			// Nothing to enrich
		}

	}

	/**
	 * {@link ManagedFunctionFactory} for overriding
	 * {@link ClassManagedFunctionSource} behaviour.
	 */
	public static class SectionManagedFunctionFactory extends StaticManagedFunction<Indexed, Indexed> {

		/**
		 * {@link Method} for the {@link ManagedFunction}.
		 */
		private final Method method;

		/**
		 * Indicates if the {@link Method} is static.
		 */
		private final boolean isStatic;

		/**
		 * {@link ManagedFunctionParameterFactory} instances for the parameters of the
		 * {@link Method}.
		 */
		private final ManagedFunctionParameterFactory[] parameters;

		/**
		 * Initiate.
		 * 
		 * @param method     {@link Method} for the {@link ManagedFunction}.
		 * @param isStatic   Indicates if the {@link Method} is static.
		 * @param parameters {@link ManagedFunctionParameterFactory} instances for the
		 *                   parameters of the {@link Method}.
		 */
		public SectionManagedFunctionFactory(Method method, boolean isStatic,
				ManagedFunctionParameterFactory[] parameters) {
			this.method = method;
			this.isStatic = isStatic;
			this.parameters = parameters;
		}

		/**
		 * Obtains the {@link Method}.
		 * 
		 * @return {@link Method}.
		 */
		public Method getMethod() {
			return this.method;
		}

		/**
		 * Obtains the {@link ManagedFunctionParameterFactory} instances.
		 * 
		 * @return {@link ManagedFunctionParameterFactory} instances.
		 */
		public ManagedFunctionParameterFactory[] getParameterFactories() {
			return this.parameters;
		}

		/*
		 * ===================== ManagedFunction ===========================
		 */

		@Override
		public Object execute(ManagedFunctionContext<Indexed, Indexed> context) throws Throwable {

			// Obtain the section object
			Object sectionObject = context.getObject(0);

			// Obtain the instance to invoke the method on
			Object instance = (this.isStatic ? null : sectionObject);

			// Create the listing of parameters
			Object[] params = new Object[this.parameters.length];
			for (int i = 0; i < params.length; i++) {
				params[i] = this.parameters[i].createParameter(context);
			}

			// Invoke the method as the task
			return MethodFunction.invokeMethod(instance, this.method, params);
		}
	}

}