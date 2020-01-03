package net.officefloor.plugin.section.clazz;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.source.PrivateSource;
import net.officefloor.plugin.clazz.ClassFlowMethodMetaData;
import net.officefloor.plugin.clazz.FlowInterface;
import net.officefloor.plugin.managedfunction.method.AbstractFunctionManagedFunctionSource;
import net.officefloor.plugin.managedfunction.method.MethodManagedFunctionBuilder;
import net.officefloor.plugin.managedfunction.method.MethodManagedFunctionBuilder.EnrichManagedFunctionTypeContext;
import net.officefloor.plugin.managedfunction.method.MethodObjectInstanceManufacturer;
import net.officefloor.plugin.managedfunction.method.MethodParameterFactory;
import net.officefloor.plugin.managedfunction.method.parameter.FlowInterfaceParameterFactory;

/**
 * {@link ManagedFunctionSource} implementation to provide the
 * {@link ManagedFunction} instances for the {@link ClassSectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
@PrivateSource
public class SectionClassManagedFunctionSource extends AbstractFunctionManagedFunctionSource {

	/**
	 * Enriches the {@link ManagedFunctionTypeBuilder} with the
	 * {@link ParameterAnnotation}.
	 * 
	 * @param context {@link EnrichManagedFunctionTypeContext}.
	 */
	public static void enrichWithParameterAnnotation(EnrichManagedFunctionTypeContext context) {

		// Obtain the parameter for the function
		Class<?> parameterType = null;
		Method method = context.getMethod();
		Class<?>[] parameters = method.getParameterTypes();
		Annotation[][] parametersAnnotations = method.getParameterAnnotations();
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
	}

	/**
	 * Enriches the {@link ManagedFunctionTypeBuilder} with the
	 * {@link FlowAnnotation} instances.
	 * 
	 * @param context {@link EnrichManagedFunctionTypeContext}.
	 */
	public static void enrichWithFlowAnnotations(EnrichManagedFunctionTypeContext context) {

		// Obtain the flow meta-data for the function
		List<FlowAnnotation> flowAnnotations = new LinkedList<>();
		List<SectionInterfaceAnnotation> sectionAnnotations = new LinkedList<>();
		MethodParameterFactory[] parameterFactories = context.getParameters();
		for (MethodParameterFactory factory : parameterFactories) {

			// Ignore if not flow parameter factory
			if (!(factory instanceof FlowInterfaceParameterFactory)) {
				continue; // ignore as not flow parameter factory
			}
			FlowInterfaceParameterFactory flowParameterFactory = (FlowInterfaceParameterFactory) factory;

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

	/*
	 * =================== ClassManagedFunctionSource ==========================
	 */

	@Override
	protected MethodObjectInstanceManufacturer createMethodObjectInstanceManufacturer(Class<?> clazz) throws Exception {
		return () -> (context) -> context.getObject(0); // obtain the section object
	}

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
		protected ManagedFunctionTypeBuilder<Indexed, Indexed> addManagedFunctionType(
				MethodManagedFunctionTypeContext context) throws Exception {

			// Include method as function in type definition
			ManagedFunctionTypeBuilder<Indexed, Indexed> functionTypeBuilder = context.getNamespaceBuilder()
					.addManagedFunctionType(context.getFunctionName(), context.getFunctionFactory(), Indexed.class,
							Indexed.class);

			// Add the section object always as first dependency
			Class<?> objectClass = context.getMethod().getDeclaringClass();
			functionTypeBuilder.addObject(objectClass).setLabel("OBJECT");
			context.nextObjectIndex(); // index for section object

			// Return the function type builder
			return functionTypeBuilder;
		}

		@Override
		protected void enrichManagedFunctionType(EnrichManagedFunctionTypeContext context) {

			// Obtain the parameter for the function
			enrichWithParameterAnnotation(context);

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
			enrichWithFlowAnnotations(context);
		}
	}

}