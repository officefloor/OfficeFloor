/*-
 * #%L
 * Procedure
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

package net.officefloor.activity.procedure;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.activity.impl.procedure.ClassProcedureSource;
import net.officefloor.activity.impl.procedure.ProcedureEscalationTypeImpl;
import net.officefloor.activity.impl.procedure.ProcedureFlowTypeImpl;
import net.officefloor.activity.impl.procedure.ProcedureImpl;
import net.officefloor.activity.impl.procedure.ProcedureObjectTypeImpl;
import net.officefloor.activity.impl.procedure.ProcedurePropertyImpl;
import net.officefloor.activity.impl.procedure.ProcedureVariableTypeImpl;
import net.officefloor.activity.procedure.build.ProcedureEmployer;
import net.officefloor.activity.procedure.spi.ProcedureSource;
import net.officefloor.activity.procedure.spi.ProcedureSourceServiceFactory;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.test.issues.FailTestCompilerIssues;
import net.officefloor.compile.test.util.LoaderUtil;
import net.officefloor.plugin.section.clazz.Parameter;

/**
 * Utility to test the {@link ProcedureLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcedureLoaderUtil {

	/**
	 * List {@link Procedure} instances for a {@link Class}.
	 * 
	 * @param clazz {@link Class}.
	 * @return {@link Procedure} instances for {@link Class}.
	 * @throws Exception If fails to load {@link Procedure} instances.
	 */
	public static Procedure[] listProcedures(Class<?> clazz) {
		return listProcedures(clazz.getName());
	}

	/**
	 * List {@link Procedure} instances for a resource.
	 * 
	 * @param resource Resource.
	 * @return {@link Procedure} instances for {@link Class}.
	 * @throws Exception If fails to load {@link Procedure} instances.
	 */
	public static Procedure[] listProcedures(String resource) {
		return newProcedureLoader().listProcedures(resource);
	}

	/**
	 * Convenience creation of {@link Procedure} for testing.
	 * 
	 * @param procedureName       Name of {@link Procedure}.
	 * @param serviceFactoryClass {@link ProcedureSourceServiceFactory}.
	 * @param properties          {@link ProcedureProperty} instances.
	 * @return {@link Procedure}.
	 */
	public static Procedure procedure(String procedureName,
			Class<? extends ProcedureSourceServiceFactory> serviceFactoryClass, ProcedureProperty... properties) {
		return procedure(procedureName, serviceFactoryClass, officeFloorCompiler(null), properties);
	}

	/**
	 * Convenience creation of {@link Procedure} for testing.
	 * 
	 * @param procedureName       Name of the {@link Procedure}.
	 * @param serviceFactoryClass {@link Class} of
	 *                            {@link ProcedureSourceServiceFactory}.
	 * @param compiler            {@link OfficeFloorCompiler}.
	 * @param properties          {@link ProcedureProperty} instances.
	 * @return {@link Procedure}.
	 */
	public static Procedure procedure(String procedureName,
			Class<? extends ProcedureSourceServiceFactory> serviceFactoryClass, OfficeFloorCompiler compiler,
			ProcedureProperty... properties) {

		// Load the service
		ProcedureSource service = loadProcedureSource(serviceFactoryClass, officeFloorCompiler(compiler));

		// Return the procedure
		return procedure(procedureName, service.getSourceName(), properties);
	}

	/**
	 * Convenience creation of default {@link Procedure} for testing.
	 * 
	 * @param procedureName Name of the {@link Procedure}.
	 * @param properties    {@link ProcedureProperty} instances.
	 * @return {@link Procedure}.
	 */
	public static Procedure procedure(String procedureName, ProcedureProperty... properties) {
		return procedure(procedureName, ClassProcedureSource.SOURCE_NAME, properties);
	}

	/**
	 * Convenience creation of {@link Procedure} for testing.
	 * 
	 * @param procedureName Name of the {@link Procedure}.
	 * @param serviceName   Service name.
	 * @param properties    {@link ProcedureProperty} instances.
	 * @return {@link Procedure}.
	 */
	public static Procedure procedure(String procedureName, String serviceName, ProcedureProperty... properties) {
		return new ProcedureImpl(procedureName, serviceName, properties);
	}

	/**
	 * Convenience creation of {@link ProcedureProperty}.
	 * 
	 * @param name Property name.
	 * @return {@link ProcedureProperty}.
	 */
	public static ProcedureProperty property(String name) {
		return new ProcedurePropertyImpl(name, name);
	}

	/**
	 * Convenience creation of {@link ProcedureProperty}.
	 * 
	 * @param name  Property name.
	 * @param label Label for the property.
	 * @return {@link ProcedureProperty}.
	 */
	public static ProcedureProperty property(String name, String label) {
		return new ProcedurePropertyImpl(name, label);
	}

	/**
	 * Validates the {@link Procedure} instances.
	 * 
	 * @param clazz              {@link Class}.
	 * @param expectedProcedures Expected {@link Procedure} instances.
	 * @throws Exception If fails to validate.
	 */
	public static void validateProcedures(Class<?> clazz, Procedure... expectedProcedures) {
		validateProcedures(clazz.getName(), expectedProcedures);
	}

	/**
	 * Validates the {@link Procedure} instances.
	 * 
	 * @param resource           Resource.
	 * @param expectedProcedures Expected {@link Procedure} instances.
	 * @throws Exception If fails to validate.
	 */
	public static void validateProcedures(String resource, Procedure... expectedProcedures) {

		// Obtain the listing of procedures
		Procedure[] actual = listProcedures(resource);

		// Validate the procedures
		validateProcedures(actual, expectedProcedures);
	}

	/**
	 * Validates the listing of {@link Procedure} instances.
	 * 
	 * @param actualProcedures   Actual {@link Procedure} instances.
	 * @param expectedProcedures Expected {@link Procedure} instances.
	 */
	public static void validateProcedures(Procedure[] actualProcedures, Procedure... expectedProcedures) {

		// Ensure correct number of procedures
		LoaderUtil.assertLength("Incorrect number of procedures", expectedProcedures, actualProcedures,
				(procedure) -> procedure.getProcedureName() + " [" + procedure.getServiceName() + "]");

		// Ensure procedures align (in order)
		for (int i = 0; i < expectedProcedures.length; i++) {
			Procedure eProcedure = expectedProcedures[i];
			Procedure aProcedure = actualProcedures[i];
			assertEquals("Incorrect procedure name for procedure " + i, eProcedure.getProcedureName(),
					aProcedure.getProcedureName());
			String suffix = "for procedure " + eProcedure.getProcedureName() + " (index " + i + ")";
			assertEquals("Incorrect service name " + suffix, eProcedure.getServiceName(), aProcedure.getServiceName());

			// Ensure correct properties
			ProcedureProperty[] eProperties = eProcedure.getProperties();
			ProcedureProperty[] aProperties = aProcedure.getProperties();
			LoaderUtil.assertLength("Incorrect number of properties " + suffix, eProperties, aProperties,
					(property) -> property.getName());
			for (int p = 0; p < eProperties.length; p++) {
				ProcedureProperty eProperty = eProperties[p];
				ProcedureProperty aProperty = aProperties[p];
				assertEquals("Incorrect name for property " + p + " " + suffix, eProperty.getName(),
						aProperty.getName());
				assertEquals("Incorrect label for property " + eProperty.getName() + " " + suffix, eProperty.getLabel(),
						aProperty.getLabel());
			}
		}
	}

	/**
	 * Loads the {@link ProcedureType} for the {@link Procedure}.
	 * 
	 * @param resource               Resource.
	 * @param serviceFactoryClass    {@link ProcedureSourceServiceFactory}
	 *                               {@link Class}.
	 * @param procedureName          Name of {@link Procedure}.
	 * @param propertyNameValuePairs Name/value pairs for {@link PropertyList}.
	 * @return {@link ProcedureType}.
	 */
	public static ProcedureType loadProcedureType(String resource,
			Class<? extends ProcedureSourceServiceFactory> serviceFactoryClass, String procedureName,
			String... propertyNameValuePairs) {
		return loadProcedureType(resource, serviceFactoryClass, procedureName, officeFloorCompiler(null),
				propertyNameValuePairs);
	}

	/**
	 * Loads the {@link ProcedureType} for the {@link Procedure}.
	 * 
	 * @param resource               Resource.
	 * @param serviceFactoryClass    {@link ProcedureSourceServiceFactory}
	 *                               {@link Class}.
	 * @param procedureName          Name of {@link Procedure}.
	 * @param compiler               {@link OfficeFloorCompiler}.
	 * @param propertyNameValuePairs Name/value pairs for {@link PropertyList}.
	 * @return {@link ProcedureType}.
	 */
	public static ProcedureType loadProcedureType(String resource,
			Class<? extends ProcedureSourceServiceFactory> serviceFactoryClass, String procedureName,
			OfficeFloorCompiler compiler, String... propertyNameValuePairs) {

		// Create the procedure loader
		ProcedureLoader loader = newProcedureLoader(compiler);

		// Load the service name
		ProcedureSource service = loadProcedureSource(serviceFactoryClass, compiler);

		// Load the procedure type
		return loader.loadProcedureType(resource, service.getSourceName(), procedureName,
				new PropertyListImpl(propertyNameValuePairs));
	}

	/**
	 * Loads the {@link ProcedureType} for the {@link Procedure} using default
	 * {@link ProcedureSource}.
	 * 
	 * @param resource      Resource.
	 * @param procedureName Name of {@link Procedure}.
	 * @return {@link ProcedureType}.
	 */
	public static ProcedureType loadProcedureType(String resource, String procedureName) {

		// Create the procedure loader
		ProcedureLoader loader = newProcedureLoader(officeFloorCompiler(null));

		// Load the procedure type
		return loader.loadProcedureType(resource, ClassProcedureSource.SOURCE_NAME, procedureName,
				new PropertyListImpl());
	}

	/**
	 * Creates the {@link ProcedureTypeBuilder}.
	 * 
	 * @param procedureName Name of the {@link ProcedureType}.
	 * @param parameterType {@link Parameter} type for the {@link ProcedureType}.
	 *                      May be <code>null</code> if no {@link Parameter}.
	 * @return {@link ProcedureTypeBuilder}.
	 */
	public static ProcedureTypeBuilder createProcedureTypeBuilder(String procedureName, Class<?> parameterType) {
		return new ProcedureTypeBuilderImpl(procedureName, parameterType);
	}

	/**
	 * Validates the {@link ProcedureType}.
	 * 
	 * @param expectedProcedureType  Expected {@link ProcedureType} via
	 *                               {@link ProcedureTypeBuilder}.
	 * @param resource               Resource.
	 * @param serviceFactoryClass    {@link ProcedureSourceServiceFactory}
	 *                               {@link Class}.
	 * @param procedureName          Name of {@link Procedure}.
	 * @param propertyNameValuePairs Name/value pairs for {@link PropertyList}.
	 * @return {@link ProcedureType}.
	 */
	public static ProcedureType validateProcedureType(ProcedureTypeBuilder expectedProcedureType, String resource,
			Class<? extends ProcedureSourceServiceFactory> serviceFactoryClass, String procedureName,
			String... propertyNameValuePairs) {

		// Load the procedure type
		ProcedureType actualType = loadProcedureType(resource, serviceFactoryClass, procedureName,
				propertyNameValuePairs);

		// Validate and return
		return validateProcedureType(expectedProcedureType, actualType);
	}

	/**
	 * Validates the {@link ProcedureType} via default {@link ProcedureSource}.
	 * 
	 * @param expectedProcedureType  Expected {@link ProcedureType} via
	 *                               {@link ProcedureTypeBuilder}.
	 * @param resource               Resource.
	 * @param procedureName          Name of {@link Procedure}.
	 * @param propertyNameValuePairs Name/value pairs for {@link PropertyList}.
	 * @return {@link ProcedureType}.
	 */
	public static ProcedureType validateProcedureType(ProcedureTypeBuilder expectedProcedureType, String resource,
			String procedureName, String... propertyNameValuePairs) {

		// Load the procedure type
		ProcedureType actualType = loadProcedureType(resource, procedureName);

		// Validate and return
		return validateProcedureType(expectedProcedureType, actualType);
	}

	/**
	 * Validates the {@link ProcedureType}.
	 * 
	 * @param expectedProcedureType Expected {@link ProcedureType} via
	 *                              {@link ProcedureTypeBuilder}.
	 * @param actualType            Actual {@link ProcedureType}.
	 * @return {@link ProcedureType}.
	 */
	public static ProcedureType validateProcedureType(ProcedureTypeBuilder expectedProcedureType,
			ProcedureType actualType) {

		// Obtain the expected type
		ProcedureType expectedType = expectedProcedureType.build();

		// Verify actual is as expected
		assertEquals("Incorrect procedure name", expectedType.getProcedureName(), actualType.getProcedureName());
		assertEquals("Incorrect parameter type", expectedType.getParameterType(), actualType.getParameterType());

		// Verify object types
		ProcedureObjectType[] eObjects = expectedType.getObjectTypes();
		ProcedureObjectType[] aObjects = actualType.getObjectTypes();
		LoaderUtil.assertLength("Incorrect number of object types", eObjects, aObjects,
				(object) -> object.getObjectName());
		for (int i = 0; i < eObjects.length; i++) {
			ProcedureObjectType eObject = eObjects[i];
			ProcedureObjectType aObject = aObjects[i];
			assertEquals("Incorrect object name (index " + i + ")", eObject.getObjectName(), aObject.getObjectName());
			assertEquals("Incorrect object type (object " + eObject.getObjectName() + ")", eObject.getObjectType(),
					aObject.getObjectType());
			assertEquals("Incorrect object qualifier (object " + eObject.getObjectName() + ")",
					eObject.getTypeQualifier(), aObject.getTypeQualifier());
		}

		// Verify variable types
		ProcedureVariableType[] eVariables = expectedType.getVariableTypes();
		ProcedureVariableType[] aVariables = actualType.getVariableTypes();
		LoaderUtil.assertLength("Incorrect number of variable types", eVariables, aVariables,
				(variable) -> variable.getVariableType());
		for (int i = 0; i < eVariables.length; i++) {
			ProcedureVariableType eVariable = eVariables[i];
			ProcedureVariableType aVariable = aVariables[i];
			assertEquals("Incorrect variable name (index " + i + ")", eVariable.getVariableName(),
					aVariable.getVariableName());
			assertEquals("Incorrect variable type (variable " + eVariable.getVariableName() + ")",
					eVariable.getVariableType(), aVariable.getVariableType());
		}

		// Verify the flow types
		ProcedureFlowType[] eFlows = expectedType.getFlowTypes();
		ProcedureFlowType[] aFlows = actualType.getFlowTypes();
		LoaderUtil.assertLength("Incorrect number of flow tyeps", eFlows, aFlows, (flow) -> flow.getFlowName());
		for (int i = 0; i < eFlows.length; i++) {
			ProcedureFlowType eFlow = eFlows[i];
			ProcedureFlowType aFlow = aFlows[i];
			assertEquals("Incorrect flow name (index " + i + ")", eFlow.getFlowName(), aFlow.getFlowName());
			assertEquals("Incorrect flow argument type (flow " + eFlow.getFlowName() + ")", eFlow.getArgumentType(),
					aFlow.getArgumentType());
		}

		// Verify the escalation types
		ProcedureEscalationType[] eEscalations = expectedType.getEscalationTypes();
		ProcedureEscalationType[] aEscalations = actualType.getEscalationTypes();
		LoaderUtil.assertLength("Incorrect number of escalations", eEscalations, aEscalations,
				(escalation) -> escalation.getEscalationName());
		for (int i = 0; i < eEscalations.length; i++) {
			ProcedureEscalationType eEscalation = eEscalations[i];
			ProcedureEscalationType aEscalation = aEscalations[i];
			assertEquals("Incorrect escalation name (index " + i + ")", eEscalation.getEscalationName(),
					aEscalation.getEscalationName());
			assertEquals("Incorrect escalation type (escalation " + eEscalation.getEscalationName() + ")",
					eEscalation.getEscalationType(), aEscalation.getEscalationType());
		}

		// Verify the next argument type
		assertEquals("Incorrect next argument type", expectedType.getNextArgumentType(),
				actualType.getNextArgumentType());

		// Return the type
		return actualType;
	}

	/**
	 * Creates the {@link ProcedureLoader} for current {@link ClassLoader}.
	 * 
	 * @return {@link ProcedureLoader}.
	 */
	public static ProcedureLoader newProcedureLoader() {
		return newProcedureLoader(officeFloorCompiler(null));
	}

	/**
	 * Creates the {@link ProcedureLoader} for the {@link ClassLoader}.
	 * 
	 * @param compiler {@link OfficeFloorCompiler}.
	 * @return {@link ProcedureLoader}.
	 */
	public static ProcedureLoader newProcedureLoader(OfficeFloorCompiler compiler) {
		try {
			return ProcedureEmployer.employProcedureLoader(officeFloorCompiler(compiler));
		} catch (Exception ex) {
			throw new IllegalStateException("Failed to create " + ProcedureLoader.class.getSimpleName(), ex);
		}
	}

	/**
	 * Loads the {@link ProcedureSource} from {@link ProcedureSourceServiceFactory}
	 * {@link Class}.
	 * 
	 * @param serviceFactoryClass {@link ProcedureSourceServiceFactory}
	 *                            {@link Class}.
	 * @return Loaded {@link ProcedureSource}.
	 */
	public static ProcedureSource loadProcedureSource(
			Class<? extends ProcedureSourceServiceFactory> serviceFactoryClass) {
		return loadProcedureSource(serviceFactoryClass, officeFloorCompiler(null));
	}

	/**
	 * Loads the {@link ProcedureSource} from {@link ProcedureSourceServiceFactory}
	 * {@link Class}.
	 * 
	 * @param serviceFactoryClass {@link ProcedureSourceServiceFactory}
	 *                            {@link Class}.
	 * @param compiler            {@link OfficeFloorCompiler}.
	 * @return Loaded {@link ProcedureSource}.
	 */
	public static ProcedureSource loadProcedureSource(
			Class<? extends ProcedureSourceServiceFactory> serviceFactoryClass, OfficeFloorCompiler compiler) {

		// Load the service factory
		Object instance;
		try {
			instance = serviceFactoryClass.getDeclaredConstructor().newInstance();
		} catch (Exception ex) {
			throw new IllegalStateException(
					"Failed to instantiate " + serviceFactoryClass.getName() + " via default constructor", ex);
		}
		ProcedureSourceServiceFactory serviceFactory = (ProcedureSourceServiceFactory) instance;

		// Create and return the service
		return officeFloorCompiler(compiler).createRootSourceContext().loadService(serviceFactory);
	}

	/**
	 * Ensure have {@link OfficeFloorCompiler}.
	 * 
	 * @param compiler Possible {@link OfficeFloorCompiler}. May be
	 *                 <code>null</code>.
	 * @return {@link OfficeFloorCompiler}.
	 */
	private static OfficeFloorCompiler officeFloorCompiler(OfficeFloorCompiler compiler) {
		if (compiler == null) {
			compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
			compiler.setCompilerIssues(new FailTestCompilerIssues());
		}
		return compiler;
	}

	/**
	 * All access via static methods.
	 */
	private ProcedureLoaderUtil() {
	}

	/**
	 * {@link ProcedureTypeBuilder} to build the expected {@link ProcedureType}.
	 */
	private static class ProcedureTypeBuilderImpl implements ProcedureTypeBuilder, ProcedureType {

		/**
		 * Name of {@link Procedure}.
		 */
		private final String procedureName;

		/**
		 * {@link Parameter} type for {@link Procedure}.
		 */
		private final Class<?> parameterType;

		/**
		 * {@link ProcedureObjectType} instances.
		 */
		private final List<ProcedureObjectType> objectTypes = new LinkedList<>();

		/**
		 * {@link ProcedureVariableType} instances.
		 */
		private final List<ProcedureVariableType> variableTypes = new LinkedList<>();

		/**
		 * {@link ProcedureFlowType} instances.
		 */
		private final List<ProcedureFlowType> flowTypes = new LinkedList<>();

		/**
		 * {@link ProcedureEscalationType} instances.
		 */
		private final List<ProcedureEscalationType> escalationTypes = new LinkedList<>();

		/**
		 * Next argument type.
		 */
		private Class<?> nextArgumentType = null;

		/**
		 * Instantiate.
		 * 
		 * @param procedureName Name of {@link Procedure}.
		 * @param parameterType {@link Parameter} type for {@link Procedure}.
		 */
		private ProcedureTypeBuilderImpl(String procedureName, Class<?> parameterType) {
			this.procedureName = procedureName;
			this.parameterType = parameterType;
		}

		/*
		 * ================= ProcedureTypeBuilder =================
		 */

		@Override
		public void addObjectType(String objectName, Class<?> objectType, String typeQualifier) {
			this.objectTypes.add(new ProcedureObjectTypeImpl(objectName, objectType, typeQualifier));
		}

		@Override
		public void addVariableType(String variableType) {
			this.addVariableType(variableType, variableType);
		}

		@Override
		public void addVariableType(String variableName, Class<?> variableType) {
			this.addVariableType(variableName, variableType.getName());
		}

		@Override
		public void addVariableType(String variableName, String variableType) {
			this.variableTypes.add(new ProcedureVariableTypeImpl(variableName, variableType));
		}

		@Override
		public void addFlowType(String flowName, Class<?> argumentType) {
			this.flowTypes.add(new ProcedureFlowTypeImpl(flowName, argumentType));
		}

		@Override
		public void addEscalationType(String escalationName, Class<? extends Throwable> escalationType) {
			this.escalationTypes.add(new ProcedureEscalationTypeImpl(escalationName, escalationType));
		}

		@Override
		public void addEscalationType(Class<? extends Throwable> escalationType) {
			this.addEscalationType(escalationType.getName(), escalationType);
		}

		@Override
		public void setNextArgumentType(Class<?> nextArgumentType) {
			this.nextArgumentType = nextArgumentType;
		}

		/*
		 * ===================== ProcedureType ===========================
		 */

		@Override
		public String getProcedureName() {
			return this.procedureName;
		}

		@Override
		public Class<?> getParameterType() {
			return this.parameterType;
		}

		@Override
		public ProcedureObjectType[] getObjectTypes() {
			return this.objectTypes.toArray(new ProcedureObjectType[this.objectTypes.size()]);
		}

		@Override
		public ProcedureVariableType[] getVariableTypes() {
			return this.variableTypes.toArray(new ProcedureVariableType[this.variableTypes.size()]);
		}

		@Override
		public ProcedureFlowType[] getFlowTypes() {
			return this.flowTypes.toArray(new ProcedureFlowType[this.flowTypes.size()]);
		}

		@Override
		public ProcedureEscalationType[] getEscalationTypes() {
			return this.escalationTypes.toArray(new ProcedureEscalationType[this.escalationTypes.size()]);
		}

		@Override
		public Class<?> getNextArgumentType() {
			return this.nextArgumentType;
		}

		@Override
		public ProcedureType build() {
			return this;
		}
	}

}
