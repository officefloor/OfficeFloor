package net.officefloor.frame.test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

/**
 * {@link Extension} for {@link TestSupport}.
 * 
 * @author Daniel Sagenschneider
 */
public class TestSupportExtension implements TestInstancePostProcessor, BeforeEachCallback, BeforeTestExecutionCallback,
		AfterTestExecutionCallback, AfterEachCallback {

	/**
	 * {@link Namespace} for {@link TestSupportExtension}.
	 */
	private static final Namespace NAMESPACE = Namespace.create(TestSupportExtension.class);

	/**
	 * Obtains the particular {@link TestSupport}.
	 * 
	 * @param <T>             {@link TestSupport} type.
	 * @param testSupportType {@link TestSupport} type.
	 * @param context         {@link ExtensionContext}.
	 * @return Particular {@link TestSupport}.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends TestSupport> T getTestSupport(Class<T> testSupportType, ExtensionContext context) {

		// Obtain the test supports
		List<TestSupport> testSupports = getTestSupports(context);

		// Attempt to find the particular existing test support
		for (TestSupport testSupport : testSupports) {
			if (testSupportType.equals(testSupport.getClass())) {
				return (T) testSupport;
			}
		}

		// Not found, so create instance
		Function<Throwable, T> fail = (ex) -> Assertions.fail("Failed to instantiate "
				+ TestSupport.class.getSimpleName() + " " + testSupportType.getName() + " by default constructor", ex);
		try {

			// Create the instance
			T testSupport = testSupportType.getConstructor().newInstance();

			// Register for further look ups
			testSupports.add(testSupport);

			// Return the instance
			return testSupport;

		} catch (InvocationTargetException ex) {
			return fail.apply(ex.getCause());
		} catch (Exception ex) {
			return fail.apply(ex);
		}
	}

	/**
	 * Obtains the listing of {@link TestSupport} instances.
	 * 
	 * @param context {@link ExtensionContext}.
	 * @return Listing of {@link TestSupport} instances.
	 */
	@SuppressWarnings("unchecked")
	private static List<TestSupport> getTestSupports(ExtensionContext context) {
		// Obtain the list of test supports
		Store store = context.getStore(NAMESPACE);
		return (List<TestSupport>) store.get(context.getRequiredTestClass());
	}

	/**
	 * Action on the particular {@link Extension} type.
	 */
	@FunctionalInterface
	private static interface ExtensionAction<E extends Extension> {
		void action(E extension) throws Exception;
	}

	/**
	 * Actions the particular {@link Extension} type.
	 * 
	 * @param <E>           {@link Extension} type.
	 * @param context       {@link ExtensionContext}.
	 * @param extensionType {@link Extension} type.
	 * @param action        {@link ExtensionAction}.
	 * @throws Exception If fails action.
	 */
	@SuppressWarnings("unchecked")
	private <E extends Extension> void action(ExtensionContext context, Class<E> extensionType,
			ExtensionAction<E> action) throws Exception {

		// Obtain the list of extensions
		List<? extends TestSupport> testSupports = getTestSupports(context);

		// Action each if supports extension
		for (TestSupport testSupport : testSupports) {
			if (extensionType.isAssignableFrom(testSupport.getClass())) {
				E extension = (E) testSupport;
				action.action(extension);
			}
		}
	}

	/*
	 * ==================== TestInstancePostProcessor ==========================
	 */

	@Override
	public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {

		// Capture all the test support
		final List<TestSupport> testSupports = new LinkedList<>();

		// Interrogate for test support instances
		Class<?> testClass = testInstance.getClass();
		do {
			for (Field field : testClass.getDeclaredFields()) {
				if (TestSupport.class.isAssignableFrom(field.getType())) {

					// Obtain the test support
					TestSupport testSupport;
					try {
						field.setAccessible(true);
						testSupport = (TestSupport) field.get(testInstance);
					} catch (Exception ex) {
						Assertions.fail("Failed to extract test " + TestSupport.class.getSimpleName() + " field "
								+ testClass.getName() + "#" + field.getName(), ex);
						return;
					}

					// Initialise the test support (if available)
					if (testSupport != null) {
						testSupport.init(context);
						testSupports.add(testSupport);
					}
				}
			}

			// Check super class
			testClass = testClass.getSuperclass();
		} while (testClass != null);

		// Store the test supports
		Store store = context.getStore(NAMESPACE);
		store.put(context.getRequiredTestClass(), testSupports);
	}

	/**
	 * ========================= Extension Lifecycle ===============================
	 */

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		this.action(context, BeforeEachCallback.class, action -> action.beforeEach(context));
	}

	@Override
	public void beforeTestExecution(ExtensionContext context) throws Exception {
		this.action(context, BeforeTestExecutionCallback.class, action -> action.beforeTestExecution(context));
	}

	@Override
	public void afterTestExecution(ExtensionContext context) throws Exception {
		this.action(context, AfterTestExecutionCallback.class, action -> action.afterTestExecution(context));
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {
		this.action(context, AfterEachCallback.class, action -> action.afterEach(context));
	}

}