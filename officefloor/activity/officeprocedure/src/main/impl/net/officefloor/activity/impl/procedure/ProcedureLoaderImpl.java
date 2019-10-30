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
package net.officefloor.activity.impl.procedure;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import net.officefloor.activity.procedure.Procedure;
import net.officefloor.activity.procedure.ProcedureEscalationType;
import net.officefloor.activity.procedure.ProcedureFlowType;
import net.officefloor.activity.procedure.ProcedureLoader;
import net.officefloor.activity.procedure.ProcedureObjectType;
import net.officefloor.activity.procedure.ProcedureProperty;
import net.officefloor.activity.procedure.ProcedureType;
import net.officefloor.activity.procedure.ProcedureVariableType;
import net.officefloor.activity.procedure.section.ProcedureManagedFunctionSource;
import net.officefloor.activity.procedure.spi.ProcedureListContext;
import net.officefloor.activity.procedure.spi.ProcedureSource;
import net.officefloor.activity.procedure.spi.ProcedureSourceServiceFactory;
import net.officefloor.activity.procedure.spi.ProcedureSpecification;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.issues.CompileError;
import net.officefloor.compile.issues.CompilerIssue;
import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.managedfunction.ManagedFunctionEscalationType;
import net.officefloor.compile.managedfunction.ManagedFunctionFlowType;
import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.plugin.section.clazz.ParameterAnnotation;
import net.officefloor.plugin.variable.VariableAnnotation;

/**
 * {@link ProcedureLoader} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcedureLoaderImpl implements ProcedureLoader {

	/**
	 * {@link Loader}.
	 */
	private final Loader loader;

	/**
	 * Instantiate.
	 * 
	 * @param compiler {@link OfficeFloorCompiler}.
	 */
	public ProcedureLoaderImpl(OfficeFloorCompiler compiler) {
		this.loader = new Loader() {

			@Override
			public SourceContext getSourceContext() {
				return compiler.createRootSourceContext();
			}

			@Override
			public ProcedureSource loadDefaultProcedureSource() {
				return new ClassProcedureSource();
			}

			@Override
			public FunctionNamespaceType loadManagedFunctionType(PropertyList properties) {
				return compiler.getManagedFunctionLoader().loadManagedFunctionType(ProcedureManagedFunctionSource.class,
						properties);
			}

			@Override
			public PropertyList createPropertyList() {
				return compiler.createPropertyList();
			}

			@Override
			public CompileError addIssue(String issueDescription, Throwable cause) {
				return compiler.getCompilerIssues().addIssue(compiler, issueDescription, cause);
			}
		};
	}

	/**
	 * Instantiate.
	 * 
	 * @param designer {@link SectionDesigner}.
	 * @param context  {@link SectionSourceContext}.
	 */
	public ProcedureLoaderImpl(SectionDesigner designer, SectionSourceContext context) {
		this.loader = new Loader() {

			@Override
			public SourceContext getSourceContext() {
				return context;
			}

			@Override
			public ProcedureSource loadDefaultProcedureSource() {
				return new ClassProcedureSource();
			}

			@Override
			public FunctionNamespaceType loadManagedFunctionType(PropertyList properties) {
				return context.loadManagedFunctionType("procedure", ProcedureManagedFunctionSource.class.getName(),
						properties);
			}

			@Override
			public PropertyList createPropertyList() {
				return context.createPropertyList();
			}

			@Override
			public CompileError addIssue(String issueDescription, Throwable cause) {
				return designer.addIssue(issueDescription, cause);
			}
		};
	}

	/*
	 * ========================== ProcedureLoader ==========================
	 */

	@Override
	public Procedure[] listProcedures(String resource) {

		// Obtain the source context
		SourceContext sourceContext = this.loader.getSourceContext();

		// Load procedures
		List<Procedure> procedures = new LinkedList<Procedure>();
		Consumer<ProcedureSource> loadProcedures = (service) -> {

			// Obtain the service name
			String serviceName = service.getSourceName();

			// Attempt to list the procedures
			ProcedureListContextImpl listContext = new ProcedureListContextImpl(resource, sourceContext);
			try {
				service.listProcedures(listContext);
			} catch (Exception ex) {
				this.loader.addIssue("Failed to list procedures from service " + serviceName + " ["
						+ service.getClass().getName() + "]", ex);
				listContext.procedures.clear(); // don't load procedures
			}

			// Load the procedures
			for (ProcedureSpecificationImpl specification : listContext.procedures) {
				String procedureName = specification.procedureName != null ? specification.procedureName.trim() : null;
				ProcedureProperty[] properties = specification.properties
						.toArray(new ProcedureProperty[specification.properties.size()]);
				procedures.add(new ProcedureImpl(procedureName, serviceName, properties));
			}
		};

		// Collect the listing of procedures
		for (ProcedureSource service : sourceContext.loadOptionalServices(ProcedureSourceServiceFactory.class)) {
			loadProcedures.accept(service);
		}

		// Determine if fall back to class
		if (procedures.size() == 0) {
			ProcedureSource defaultService = this.loader.loadDefaultProcedureSource();
			loadProcedures.accept(defaultService);
		}

		// Sort the procedures
		Collections.sort(procedures, (a, b) -> {
			int comparison = String.CASE_INSENSITIVE_ORDER.compare(a.getProcedureName(), b.getProcedureName());
			if (comparison == 0) {
				// Same procedure name, so order by service name
				comparison = String.CASE_INSENSITIVE_ORDER.compare(a.getServiceName(), b.getServiceName());
			}
			return comparison;
		});

		// Return the procedures
		return procedures.toArray(new Procedure[procedures.size()]);
	}

	@Override
	public ProcedureType loadProcedureType(String resource, String serviceName, String procedureName,
			PropertyList properties) {

		// Load the managed function type
		PropertyList loadProperties = this.loader.createPropertyList();
		for (Property property : properties) {
			loadProperties.addProperty(property.getName()).setValue(property.getValue());
		}
		loadProperties.addProperty(ProcedureManagedFunctionSource.RESOURCE_NAME_PROPERTY_NAME).setValue(resource);
		loadProperties.addProperty(ProcedureManagedFunctionSource.SERVICE_NAME_PROPERTY_NAME).setValue(serviceName);
		loadProperties.addProperty(ProcedureManagedFunctionSource.PROCEDURE_PROPERTY_NAME).setValue(procedureName);
		FunctionNamespaceType namespace = this.loader.loadManagedFunctionType(loadProperties);

		// Ensure have namespace
		if (namespace == null) {
			return null; // failed to load
		}

		// Obtain the managed function type (should always be just one)
		ManagedFunctionType<?, ?> managedFunctionType = namespace.getManagedFunctionTypes()[0];

		// Obtain parameter, variable and object types
		Class<?> parameterType = null;
		List<ProcedureObjectType> objectTypes = new LinkedList<>();
		List<ProcedureVariableType> variableTypes = new LinkedList<>();
		ParameterAnnotation parameterAnnotation = managedFunctionType.getAnnotation(ParameterAnnotation.class);
		int parameterIndex = (parameterAnnotation != null) ? parameterAnnotation.getParameterIndex() : -1;
		ManagedFunctionObjectType<?>[] functionObjectTypes = managedFunctionType.getObjectTypes();
		NEXT_OBJECT: for (int i = 0; i < functionObjectTypes.length; i++) {
			ManagedFunctionObjectType<?> functionObjectType = functionObjectTypes[i];
			String objectName = functionObjectType.getObjectName();
			Class<?> objectType = functionObjectType.getObjectType();
			String typeQualifier = functionObjectType.getTypeQualifier();

			// Determine if parameter
			if (i == parameterIndex) {
				parameterType = objectType;
				continue NEXT_OBJECT; // parameter
			}

			// Determine if variable
			VariableAnnotation variableAnnotation = functionObjectType.getAnnotation(VariableAnnotation.class);
			if (variableAnnotation != null) {
				variableTypes.add(new ProcedureVariableTypeImpl(variableAnnotation.getVariableName(),
						variableAnnotation.getVariableType()));
				continue NEXT_OBJECT; // not include variable as object
			}

			// Add object type
			objectTypes.add(new ProcedureObjectTypeImpl(objectName, objectType, typeQualifier));
		}

		// Load the flows
		List<ProcedureFlowType> flowTypes = new LinkedList<>();
		for (ManagedFunctionFlowType<?> functionFlowType : managedFunctionType.getFlowTypes()) {
			flowTypes
					.add(new ProcedureFlowTypeImpl(functionFlowType.getFlowName(), functionFlowType.getArgumentType()));
		}

		// Load the escalations
		List<ProcedureEscalationType> escalationTypes = new LinkedList<>();
		for (ManagedFunctionEscalationType functionEscalationType : managedFunctionType.getEscalationTypes()) {
			escalationTypes.add(new ProcedureEscalationTypeImpl(functionEscalationType.getEscalationName(),
					functionEscalationType.getEscalationType()));
		}

		// Obtain the next argument type
		Class<?> nextArgumentType = managedFunctionType.getReturnType();

		// Return the procedure type
		return new ProcedureTypeImpl(procedureName, parameterType,
				objectTypes.toArray(new ProcedureObjectType[objectTypes.size()]),
				variableTypes.toArray(new ProcedureVariableType[variableTypes.size()]),
				flowTypes.toArray(new ProcedureFlowType[flowTypes.size()]),
				escalationTypes.toArray(new ProcedureEscalationType[escalationTypes.size()]), nextArgumentType);
	}

	/**
	 * {@link ProcedureListContext} implementation.
	 */
	private static class ProcedureListContextImpl implements ProcedureListContext {

		/**
		 * Resource.
		 */
		private final String resource;

		/**
		 * {@link SourceContext}.
		 */
		private final SourceContext context;

		/**
		 * {@link ProcedureSpecificationImpl} instances.
		 */
		private final List<ProcedureSpecificationImpl> procedures = new LinkedList<>();

		/**
		 * Instantiate.
		 * 
		 * @param resource Resource.
		 * @param context  {@link SourceContext}.
		 */
		private ProcedureListContextImpl(String resource, SourceContext context) {
			this.resource = resource;
			this.context = context;
		}

		/*
		 * ====================== ProcedureListContext ========================
		 */

		@Override
		public String getResource() {
			return this.resource;
		}

		@Override
		public ProcedureSpecification addProcedure(String procedureName) {
			ProcedureSpecificationImpl procedure = new ProcedureSpecificationImpl(procedureName);
			this.procedures.add(procedure);
			return procedure;
		}

		@Override
		public SourceContext getSourceContext() {
			return this.context;
		}
	}

	/**
	 * {@link ProcedureSpecification} implementation.
	 */
	private static class ProcedureSpecificationImpl implements ProcedureSpecification {

		/**
		 * Name of the {@link Procedure}.
		 */
		private final String procedureName;

		/**
		 * {@link ProcedureProperty} instances.
		 */
		private final List<ProcedureProperty> properties = new LinkedList<>();

		/**
		 * Instantiate.
		 * 
		 * @param procedureName Name of the {@link Procedure}.
		 */
		private ProcedureSpecificationImpl(String procedureName) {
			this.procedureName = procedureName;
		}

		/*
		 * ==================== ProcedureSpecification ========================
		 */

		@Override
		public void addProperty(String name) {
			this.addProperty(name, name);
		}

		@Override
		public void addProperty(String name, String label) {
			this.addProperty(new ProcedurePropertyImpl(name, label));
		}

		@Override
		public void addProperty(ProcedureProperty property) {
			this.properties.add(property);
		}
	}

	/**
	 * Loader.
	 */
	private static interface Loader {

		/**
		 * Obtains the {@link SourceContext} instances.
		 * 
		 * @return {@link SourceContext} instances.
		 */
		SourceContext getSourceContext();

		/**
		 * Loads the default {@link ProcedureSource}.
		 * 
		 * @return Default {@link ProcedureSource}.
		 */
		ProcedureSource loadDefaultProcedureSource();

		/**
		 * Loads the {@link FunctionNamespaceType}.
		 * 
		 * @param properties {@link PropertyList} for the {@link ManagedFunctionType}.
		 * @return {@link FunctionNamespaceType}.
		 */
		FunctionNamespaceType loadManagedFunctionType(PropertyList properties);

		/**
		 * Creates a {@link PropertyList}.
		 * 
		 * @return {@link PropertyList}.
		 */
		PropertyList createPropertyList();

		/**
		 * Adds a {@link CompilerIssue}.
		 * 
		 * @param issueDescription Description of the issue.
		 * @param cause            Cause.
		 * @return {@link CompileError}.
		 */
		CompileError addIssue(String issueDescription, Throwable cause);
	}

}