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
package net.officefloor.activity.procedure;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Assert;

import net.officefloor.activity.impl.procedure.ProcedureEscalationTypeImpl;
import net.officefloor.activity.impl.procedure.ProcedureFlowTypeImpl;
import net.officefloor.activity.impl.procedure.ProcedureObjectTypeImpl;
import net.officefloor.activity.procedure.build.ProcedureEmployer;
import net.officefloor.activity.procedure.spi.ProcedureService;
import net.officefloor.activity.procedure.spi.ProcedureServiceFactory;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
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
	 * List {@link Procedure} instances for {@link Class}.
	 * 
	 * @param clazz {@link Class}.
	 * @return {@link Procedure} instances for {@link Class}.
	 * @throws Exception If fails to load {@link Procedure} instances.
	 */
	public static Procedure[] listProcedures(Class<?> clazz) {
		return newProcedureLoader().listProcedures(clazz);
	}

	/**
	 * Convenience creation of {@link Procedure} for testing.
	 * 
	 * @param procedureName       Name of {@link Procedure}.
	 * @param serviceFactoryClass {@link ProcedureServiceFactory}.
	 * @return {@link Procedure}.
	 */
	public static Procedure procedure(String procedureName,
			Class<? extends ProcedureServiceFactory> serviceFactoryClass) {
		return procedure(procedureName, serviceFactoryClass, officeFloorCompiler(null));
	}

	/**
	 * Convenience creation of {@link Procedure} for testing.
	 * 
	 * @param procedureName       Name of the {@link Procedure}.
	 * @param serviceFactoryClass {@link Class} of {@link ProcedureServiceFactory}.
	 * @param compiler            {@link OfficeFloorCompiler}.
	 * @return {@link Procedure}.
	 */
	public static Procedure procedure(String procedureName,
			Class<? extends ProcedureServiceFactory> serviceFactoryClass, OfficeFloorCompiler compiler) {

		// Load the service
		ProcedureService service = loadProcedureService(serviceFactoryClass, officeFloorCompiler(compiler));

		// Return the procedure
		return new Procedure() {

			@Override
			public String getProcedureName() {
				return procedureName;
			}

			@Override
			public String getServiceName() {
				return service.getServiceName();
			}
		};
	}

	/**
	 * Validates the {@link Procedure} instances.
	 * 
	 * @param clazz              {@link Class}.
	 * @param expectedProcedures Expected {@link Procedure} instances.
	 * @throws Exception If fails to validate.
	 */
	public static void validateProcedures(Class<?> clazz, Procedure... expectedProcedures) {

		// Obtain the listing of procedures
		Procedure[] actual = listProcedures(clazz);

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

		// Create sorted lists for comparison
		Comparator<Procedure> comparator = (a, b) -> (a.getProcedureName() + "-" + a.getServiceName())
				.compareTo(b.getProcedureName() + "-" + b.getServiceName());
		Procedure[] actual = Arrays.copyOf(actualProcedures, actualProcedures.length);
		Arrays.sort(actual, comparator);
		Procedure[] expected = Arrays.copyOf(expectedProcedures, expectedProcedures.length);
		Arrays.sort(expected, comparator);

		// Generate difference message
		Function<Procedure[], String> toString = (procedures) -> String.join(",",
				Arrays.stream(procedures)
						.map((procedure) -> procedure.getProcedureName() + " [" + procedure.getServiceName() + "]")
						.collect(Collectors.toList()));
		String differenceSuffix = "\n\n - Expected : " + toString.apply(expected) + "\n - Actual  : "
				+ toString.apply(actual) + "\n\n";

		// Ensure correct number of procedures
		assertEquals("Incorrect number of procedures" + differenceSuffix, expected.length, actual.length);

		// Ensure procedures align
		for (int i = 0; i < expected.length; i++) {
			Procedure eProc = expected[i];
			Procedure aProc = actual[i];
			assertEquals("Incorrect procedure name for procedure " + i + differenceSuffix, eProc.getProcedureName(),
					aProc.getProcedureName());
			assertEquals("Incorrect service name for procedure " + i + differenceSuffix, eProc.getServiceName(),
					aProc.getServiceName());
		}
	}

	/**
	 * Loads the {@link ProcedureType} for the {@link Procedure}.
	 * 
	 * @param clazz               {@link Class}.
	 * @param serviceFactoryClass {@link ProcedureServiceFactory} {@link Class}.
	 * @param procedureName       Name of {@link Procedure}.
	 * @return {@link ProcedureType}.
	 */
	public static ProcedureType loadProcedureType(Class<?> clazz,
			Class<? extends ProcedureServiceFactory> serviceFactoryClass, String procedureName) {
		return loadProcedureType(clazz, serviceFactoryClass, procedureName, officeFloorCompiler(null));
	}

	/**
	 * Loads the {@link ProcedureType} for the {@link Procedure}.
	 * 
	 * @param clazz               {@link Class}.
	 * @param serviceFactoryClass {@link ProcedureServiceFactory} {@link Class}.
	 * @param procedureName       Name of {@link Procedure}.
	 * @param compiler            {@link OfficeFloorCompiler}.
	 * @return {@link ProcedureType}.
	 */
	public static ProcedureType loadProcedureType(Class<?> clazz,
			Class<? extends ProcedureServiceFactory> serviceFactoryClass, String procedureName,
			OfficeFloorCompiler compiler) {

		// Create the procedure loader
		ProcedureLoader loader = newProcedureLoader(compiler);

		// Load the service name
		ProcedureService service = loadProcedureService(serviceFactoryClass, compiler);

		// Load the managed function type
		return loader.loadProcedureType(clazz, service.getServiceName(), procedureName);
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
	 * @param expectedProcedureType Expected {@link ProcedureType} via
	 *                              {@link ProcedureTypeBuilder}.
	 * @param clazz                 {@link Class}.
	 * @param serviceFactoryClass   {@link ProcedureServiceFactory} {@link Class}.
	 * @param procedureName         Name of {@link Procedure}.
	 * @return {@link ManagedFunctionType}.
	 */
	public static ProcedureType validateProcedureType(ProcedureTypeBuilder expectedProcedureType, Class<?> clazz,
			Class<? extends ProcedureServiceFactory> serviceFactoryClass, String procedureName) {

		// Obtain the expected type
		if (!(expectedProcedureType instanceof ProcedureType)) {
			Assert.fail("expectedProcedureType must be created from createProcedureTypeBuilder");
		}
		ProcedureType eType = (ProcedureType) expectedProcedureType;

		// Load the procedure type
		ProcedureType aType = loadProcedureType(clazz, serviceFactoryClass, procedureName);

		// Verify actual is as expected
		assertEquals("Incorrect procedure name", eType.getProcedureName(), aType.getProcedureName());
		assertEquals("Incorrect parameter type", eType.getParameterType(), aType.getParameterType());

		// Verify object types
		ProcedureObjectType[] eObjects = eType.getObjectTypes();
		ProcedureObjectType[] aObjects = aType.getObjectTypes();
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

		// Verify the flow types
		ProcedureFlowType[] eFlows = eType.getFlowTypes();
		ProcedureFlowType[] aFlows = aType.getFlowTypes();
		LoaderUtil.assertLength("Incorrect number of flow tyeps", eFlows, aFlows, (flow) -> flow.getFlowName());
		for (int i = 0; i < eFlows.length; i++) {
			ProcedureFlowType eFlow = eFlows[i];
			ProcedureFlowType aFlow = aFlows[i];
			assertEquals("Incorrect flow name (index " + i + ")", eFlow.getFlowName(), aFlow.getFlowName());
			assertEquals("Incorrect flow argument type (flow " + eFlow.getFlowName() + ")", eFlow.getArgumentType(),
					aFlow.getArgumentType());
		}

		// Verify the escalation types
		ProcedureEscalationType[] eEscalations = eType.getEscalationTypes();
		ProcedureEscalationType[] aEscalations = aType.getEscalationTypes();
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
		assertEquals("Incorrect next argument type", eType.getNextArgumentType(), aType.getNextArgumentType());

		// Return the type
		return aType;
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
	 * Loads the {@link ProcedureService} from {@link ProcedureServiceFactory}
	 * {@link Class}.
	 * 
	 * @param serviceFactoryClass {@link ProcedureServiceFactory} {@link Class}.
	 * @return Loaded {@link ProcedureService}.
	 */
	public static ProcedureService loadProcedureService(Class<? extends ProcedureServiceFactory> serviceFactoryClass) {
		return loadProcedureService(serviceFactoryClass, officeFloorCompiler(null));
	}

	/**
	 * Loads the {@link ProcedureService} from {@link ProcedureServiceFactory}
	 * {@link Class}.
	 * 
	 * @param serviceFactoryClass {@link ProcedureServiceFactory} {@link Class}.
	 * @param compiler            {@link OfficeFloorCompiler}.
	 * @return Loaded {@link ProcedureService}.
	 */
	public static ProcedureService loadProcedureService(Class<? extends ProcedureServiceFactory> serviceFactoryClass,
			OfficeFloorCompiler compiler) {

		// Load the service factory
		Object instance;
		try {
			instance = serviceFactoryClass.getDeclaredConstructor().newInstance();
		} catch (Exception ex) {
			throw new IllegalStateException(
					"Failed to instantiate " + serviceFactoryClass.getName() + " via default constructor", ex);
		}
		ProcedureServiceFactory serviceFactory = (ProcedureServiceFactory) instance;

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
		public void addFlowType(String flowName, Class<?> argumentType) {
			this.flowTypes.add(new ProcedureFlowTypeImpl(flowName, argumentType));
		}

		@Override
		public void addEscalationType(String escalationName, Class<? extends Throwable> escalationType) {
			this.escalationTypes.add(new ProcedureEscalationTypeImpl(escalationName, escalationType));
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
	}

}