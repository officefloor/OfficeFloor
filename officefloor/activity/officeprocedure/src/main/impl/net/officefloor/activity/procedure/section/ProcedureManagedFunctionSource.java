/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
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
package net.officefloor.activity.procedure.section;

import java.lang.reflect.Method;

import net.officefloor.activity.impl.procedure.ClassProcedureService;
import net.officefloor.activity.procedure.Procedure;
import net.officefloor.activity.procedure.spi.ManagedFunctionProcedureService;
import net.officefloor.activity.procedure.spi.ProcedureManagedFunctionContext;
import net.officefloor.activity.procedure.spi.ProcedureMethodContext;
import net.officefloor.activity.procedure.spi.ProcedureService;
import net.officefloor.activity.procedure.spi.ProcedureServiceFactory;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.impl.AbstractManagedFunctionSource;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.plugin.managedfunction.method.DefaultConstructorMethodObjectInstanceFactory;
import net.officefloor.plugin.managedfunction.method.MethodManagedFunctionBuilder;
import net.officefloor.plugin.managedfunction.method.MethodObjectInstanceFactory;
import net.officefloor.plugin.section.clazz.SectionClassManagedFunctionSource;

/**
 * {@link ManagedFunctionSource} for first-class procedure.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcedureManagedFunctionSource extends AbstractManagedFunctionSource {

	/**
	 * {@link Property} name providing the {@link Class} name.
	 */
	public static final String RESOURCE_NAME_PROPERTY_NAME = "class.name";

	/**
	 * {@link Property} name providing the service to create the procedure.
	 */
	public static final String SERVICE_NAME_PROPERTY_NAME = "service.name";

	/**
	 * {@link Property} name identifying the procedure name.
	 */
	public static final String PROCEDURE_PROPERTY_NAME = "procedure";

	/*
	 * ================= ManagedFunctionSource ==================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(RESOURCE_NAME_PROPERTY_NAME, "Class");
		context.addProperty(SERVICE_NAME_PROPERTY_NAME, "Service");
		context.addProperty(PROCEDURE_PROPERTY_NAME, "Procedure");
	}

	@Override
	public void sourceManagedFunctions(FunctionNamespaceBuilder functionNamespaceTypeBuilder,
			ManagedFunctionSourceContext context) throws Exception {

		// Obtain the procedure details
		String resource = context.getProperty(RESOURCE_NAME_PROPERTY_NAME);
		String serviceName = context.getProperty(SERVICE_NAME_PROPERTY_NAME);
		String procedureName = context.getProperty(PROCEDURE_PROPERTY_NAME);

		// Find the service
		ProcedureService procedureService = null;
		if (ClassProcedureService.SERVICE_NAME.equals(serviceName)) {
			// Use default service
			procedureService = new ClassProcedureService();

		} else {
			// Search for service
			FOUND_SERVICE: for (ProcedureService service : context
					.loadOptionalServices(ProcedureServiceFactory.class)) {
				if (serviceName.equals(service.getServiceName())) {
					procedureService = service;
					break FOUND_SERVICE;
				}
			}
		}
		if (procedureService == null) {
			// Can not find procedure service
			throw new Exception("Can not find " + ProcedureService.class.getSimpleName() + " " + serviceName);
		}

		// Determine if non-method managed function
		if (procedureService instanceof ManagedFunctionProcedureService) {
			ManagedFunctionProcedureService managedFunctionProcedureService = (ManagedFunctionProcedureService) procedureService;

			// Load the managed function
			ProcedureManagedFunctionContextImpl procedureContext = new ProcedureManagedFunctionContextImpl();
			managedFunctionProcedureService.loadManagedFunction(procedureContext);

		} else {
			// Load the method for the procedure service
			ProcedureMethodContextImpl procedureContext = new ProcedureMethodContextImpl(resource, procedureName,
					context);
			Method method = procedureService.loadMethod(procedureContext);

			// Ensure have method
			if (method == null) {
				throw new Exception("No " + Method.class.getSimpleName() + " provided by service " + serviceName
						+ " for procedure " + procedureName + " from resource " + resource);
			}

			// Obtain the object instance factory
			MethodObjectInstanceFactory factory = procedureContext.methodObjectInstanceFactory;
			if ((factory == null) && (!procedureContext.isStatic)) {
				Class<?> resourceClass = context.loadClass(resource);
				factory = new DefaultConstructorMethodObjectInstanceFactory(resourceClass);
			}
			MethodObjectInstanceFactory finalFactory = factory;

			// Load the managed function
			MethodManagedFunctionBuilder builder = new MethodManagedFunctionBuilder() {
				@Override
				protected void enrichManagedFunctionType(EnrichManagedFunctionTypeContext context) {
					SectionClassManagedFunctionSource.enrichWithParameterAnnotation(context);
					SectionClassManagedFunctionSource.enrichWithFlowAnnotations(context);
				}
			};
			builder.buildMethod(method, () -> finalFactory, functionNamespaceTypeBuilder, context);
		}
	}

	/**
	 * {@link ProcedureManagedFunctionContext} implementation.
	 */
	private static class ProcedureManagedFunctionContextImpl implements ProcedureManagedFunctionContext {

		/*
		 * =================== ProcedureManagedFunctionContext ======================
		 */

		@Override
		public String getResource() {
			// TODO implement ProcedureManagedFunctionContext.getResource
			throw new UnsupportedOperationException("TODO implement ProcedureManagedFunctionContext.getResource");
		}

		@Override
		public String getProcedureName() {
			// TODO implement ProcedureManagedFunctionContext.getProcedureName
			throw new UnsupportedOperationException("TODO implement ProcedureManagedFunctionContext.getProcedureName");
		}

		@Override
		public SourceContext getSourceContext() {
			// TODO implement ProcedureManagedFunctionContext.getSourceContext
			throw new UnsupportedOperationException("TODO implement ProcedureManagedFunctionContext.getSourceContext");
		}

		@Override
		public <M extends Enum<M>, F extends Enum<F>> ManagedFunctionTypeBuilder<M, F> setManagedFunction(
				ManagedFunctionFactory<M, F> functionFactory, Class<M> objectKeysClass, Class<F> flowKeysClass) {
			// TODO implement ProcedureManagedFunctionContext.setManagedFunction
			throw new UnsupportedOperationException(
					"TODO implement ProcedureManagedFunctionContext.setManagedFunction");
		}
	}

	/**
	 * {@link ProcedureMethodContext} implementation.
	 */
	private static class ProcedureMethodContextImpl implements ProcedureMethodContext {

		/**
		 * Resource.
		 */
		private final String resource;

		/**
		 * {@link Procedure} name.
		 */
		private final String procedureName;

		/**
		 * {@link SourceContext}.
		 */
		private final SourceContext sourceContext;

		/**
		 * Indicates if static. In other words, no {@link MethodObjectInstanceFactory}.
		 */
		private boolean isStatic = false;

		/**
		 * {@link MethodObjectInstanceFactory}.
		 */
		private MethodObjectInstanceFactory methodObjectInstanceFactory = null;

		/**
		 * Instantiate.
		 * 
		 * @param resource      Resource.
		 * @param procedureName {@link Procedure} name.
		 * @param sourceContext {@link SourceContext}.
		 * @throws Exception If fails to create default
		 *                   {@link MethodObjectInstanceFactory}.
		 */
		private ProcedureMethodContextImpl(String resource, String procedureName, SourceContext sourceContext)
				throws Exception {
			this.resource = resource;
			this.procedureName = procedureName;
			this.sourceContext = sourceContext;
		}

		/*
		 * =================== ProcedureServiceContext =====================
		 */

		@Override
		public String getResource() {
			return this.resource;
		}

		@Override
		public String getProcedureName() {
			return this.procedureName;
		}

		@Override
		public void setMethodObjectInstanceFactory(MethodObjectInstanceFactory factory) {
			this.methodObjectInstanceFactory = factory;
			this.isStatic = (factory == null);
		}

		@Override
		public SourceContext getSourceContext() {
			return this.sourceContext;
		}
	}

}