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
import java.util.function.Function;
import java.util.stream.Collectors;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.frame.test.OfficeFrameTestCase;

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
	public static Procedure[] listProcedures(Class<?> clazz) throws Exception {
		return listProcedures(clazz.getName());
	}

	/**
	 * List {@link Procedure} instances for {@link Class}.
	 * 
	 * @param className Name of {@link Class}.
	 * @return {@link Procedure} instances for {@link Class}.
	 * @throws Exception If fails to load {@link Procedure} instances.
	 */
	public static Procedure[] listProcedures(String className) throws Exception {
		return newProcedureLoader().listProcedures(className);
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
		return procedure(procedureName, serviceFactoryClass, OfficeFloorCompiler.newOfficeFloorCompiler(null));
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

		// Load the source context
		Object instance;
		try {
			instance = serviceFactoryClass.getDeclaredConstructor().newInstance();
		} catch (Exception ex) {
			throw OfficeFrameTestCase.fail(ex);
		}
		ProcedureServiceFactory serviceFactory = (ProcedureServiceFactory) instance;
		ProcedureService service = compiler.createRootSourceContext().loadService(serviceFactory);

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
	public static void validateProcedures(Class<?> clazz, Procedure... expectedProcedures) throws Exception {

		// Obtain the listing of procedures
		Procedure[] actual = listProcedures(clazz);

		// Validate the procedures
		validateProcedures(actual, expectedProcedures);
	}

	/**
	 * Validates the {@link Procedure} instances.
	 * 
	 * @param className          Name of {@link Class}.
	 * @param expectedProcedures Expected {@link Procedure} instances.
	 * @throws Exception If fails to validate.
	 */
	public static void validateProcedures(String className, Procedure... expectedProcedures) throws Exception {

		// Obtain the listing of procedures
		Procedure[] actual = listProcedures(className);

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
	 * Creates the {@link ProcedureLoader} for current {@link ClassLoader}.
	 * 
	 * @return {@link ProcedureLoader}.
	 */
	private static ProcedureLoader newProcedureLoader() {
		return newProcedureLoader(OfficeFloorCompiler.newOfficeFloorCompiler(null));
	}

	/**
	 * Creates the {@link ProcedureLoader} for the {@link ClassLoader}.
	 * 
	 * @param compiler {@link OfficeFloorCompiler}.
	 * @return {@link ProcedureLoader}.
	 */
	private static ProcedureLoader newProcedureLoader(OfficeFloorCompiler compiler) {
		try {
			return ProcedureLoader.newProcedureLoader(compiler);
		} catch (Exception ex) {
			throw OfficeFrameTestCase.fail(ex);
		}
	}

	/**
	 * All access via static methods.
	 */
	private ProcedureLoaderUtil() {
	}

}