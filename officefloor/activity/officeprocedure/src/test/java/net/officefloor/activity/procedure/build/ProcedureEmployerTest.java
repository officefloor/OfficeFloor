package net.officefloor.activity.procedure.build;

import static org.junit.Assert.assertArrayEquals;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import net.officefloor.activity.procedure.Procedure;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link ProcedureEmployer}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcedureEmployerTest extends OfficeFrameTestCase {

	/**
	 * Ensure {@link Object} {@link Method} instances are excluded by default.
	 */
	public void testObjectListing() {
		assertListProcedures(Object.class, null);
	}

	/**
	 * Ensure list all {@link Procedure} names.
	 */
	public void testListAll() {
		assertListProcedures(MockProcedures.class, null, "one", "two", "three", "four");
	}

	/**
	 * Ensure can exclude {@link Method} from {@link Procedure} names.
	 */
	public void testFilterList() {
		assertListProcedures(MockProcedures.class, (method) -> "one".equals(method.getName()), "two", "three", "four");
	}

	public static class MockProcedures {

		public void one() {
			// Test method
		}

		public static void two() {
			// Test method
		}

		public void three(String parameter) {
			// Test method
		}

		public void four() throws Exception {
			// Test method
		}
	}

	/**
	 * Asserts the {@link Procedure} names from {@link ProcedureEmployer}
	 * convenience method.
	 * 
	 * @param clazz                  {@link Class}.
	 * @param exclude                {@link Predicate} to exclude {@link Method}
	 *                               instances. May be <code>null</code>.
	 * @param expectedProcedureNames Expected {@link Procedure} names.
	 */
	public static void assertListProcedures(Class<?> clazz, Predicate<Method> exclude,
			String... expectedProcedureNames) {

		// Obtain the procedure names
		List<String> methodNames = new LinkedList<>();
		ProcedureEmployer.listMethods(clazz, exclude, (method) -> methodNames.add(method.getName()));
		String[] procedureNames = methodNames.toArray(new String[methodNames.size()]);

		// Ensure correct procedures
		Arrays.sort(expectedProcedureNames);
		Arrays.sort(procedureNames);
		assertArrayEquals("Incorrect procedures", expectedProcedureNames, procedureNames);
	}

}