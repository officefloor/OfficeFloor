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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.officefloor.activity.procedure.build.ProcedureArchitectEmployer;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.test.issues.FailTestCompilerIssues;
import net.officefloor.compile.test.managedfunction.ManagedFunctionLoaderUtil;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunction;

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
	 * Loads the {@link ManagedFunctionType} for the {@link Procedure}.
	 * 
	 * @param clazz               {@link Class}.
	 * @param procedureName       Name of {@link Procedure}.
	 * @param serviceFactoryClass {@link ProcedureServiceFactory} {@link Class}.
	 * @return {@link ManagedFunctionType}.
	 */
	public static ManagedFunctionType<Indexed, Indexed> loadProcedureType(Class<?> clazz, String procedureName,
			Class<? extends ProcedureServiceFactory> serviceFactoryClass) {
		return loadProcedureType(clazz, procedureName, serviceFactoryClass, officeFloorCompiler(null));
	}

	/**
	 * Loads the {@link ManagedFunctionType} for the {@link Procedure}.
	 * 
	 * @param clazz               {@link Class}.
	 * @param procedureName       Name of {@link Procedure}.
	 * @param serviceFactoryClass {@link ProcedureServiceFactory} {@link Class}.
	 * @param compiler            {@link OfficeFloorCompiler}.
	 * @return {@link ManagedFunctionType}.
	 */
	public static ManagedFunctionType<Indexed, Indexed> loadProcedureType(Class<?> clazz, String procedureName,
			Class<? extends ProcedureServiceFactory> serviceFactoryClass, OfficeFloorCompiler compiler) {

		// Create the procedure loader
		ProcedureLoader loader = newProcedureLoader(compiler);

		// Load the service name
		ProcedureService service = loadProcedureService(serviceFactoryClass, compiler);

		// Load the managed function type
		return loader.loadProcedureType(clazz, procedureName, service.getServiceName());
	}

	/**
	 * Validates the {@link Procedure} type, with convenience of function name
	 * matching {@link Procedure} name.
	 * 
	 * @param clazz               {@link Class}.
	 * @param procedureName       Name of {@link Procedure}.
	 * @param serviceFactoryClass {@link ProcedureServiceFactory} {@link Class}.
	 * @param typeBuilder         Builds the expected
	 *                            {@link ManagedFunctionTypeBuilder}.
	 * @return {@link ManagedFunctionType}.
	 */
	public static ManagedFunctionType<Indexed, Indexed> validateProcedureType(Class<?> clazz, String procedureName,
			Class<? extends ProcedureServiceFactory> serviceFactoryClass,
			Consumer<ManagedFunctionTypeBuilder<Indexed, Indexed>> typeBuilder) {
		return validateProcedureType(clazz, procedureName, serviceFactoryClass, procedureName, typeBuilder);
	}

	/**
	 * Validates the {@link Procedure} type.
	 * 
	 * @param clazz               {@link Class}.
	 * @param procedureName       Name of {@link Procedure}.
	 * @param serviceFactoryClass {@link ProcedureServiceFactory} {@link Class}.
	 * @param functionName        Name of {@link ManagedFunction}.
	 * @param typeBuilder         Builds the expected
	 *                            {@link ManagedFunctionTypeBuilder}.
	 * @return {@link ManagedFunctionType}.
	 */
	public static ManagedFunctionType<Indexed, Indexed> validateProcedureType(Class<?> clazz, String procedureName,
			Class<? extends ProcedureServiceFactory> serviceFactoryClass, String functionName,
			Consumer<ManagedFunctionTypeBuilder<Indexed, Indexed>> typeBuilder) {

		// Load the procedure type
		ManagedFunctionType<Indexed, Indexed> type = loadProcedureType(clazz, procedureName, serviceFactoryClass);

		// Create expected type
		FunctionNamespaceBuilder namespace = ManagedFunctionLoaderUtil.createManagedFunctionTypeBuilder();
		ManagedFunctionTypeBuilder<Indexed, Indexed> procedureBuilder = namespace.addManagedFunctionType(functionName,
				type.getManagedFunctionFactory(), Indexed.class, Indexed.class);
		if (typeBuilder != null) {
			typeBuilder.accept(procedureBuilder);
		}

		// Validate the managed function type
		ManagedFunctionLoaderUtil.validateManagedFunctionType(procedureBuilder, type);

		// Return the type
		return type;
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
			return ProcedureArchitectEmployer.employProcedureLoader(officeFloorCompiler(compiler));
		} catch (Exception ex) {
			throw new IllegalStateException("Failed to create " + ProcedureLoader.class.getSimpleName(), ex);
		}
	}

	/**
	 * Loads the {@link ProcedureService} from {@link ProcedureServiceFactory}
	 * {@link Class}.
	 * 
	 * @param serviceFactoryClass {@link ProcedureServiceFactory} {@link Class}.
	 * @param compiler            {@link OfficeFloorCompiler}.
	 * @return Loaded {@link ProcedureService}.
	 */
	private static ProcedureService loadProcedureService(Class<? extends ProcedureServiceFactory> serviceFactoryClass,
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

}