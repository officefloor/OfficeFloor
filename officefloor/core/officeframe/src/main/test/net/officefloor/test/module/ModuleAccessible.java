package net.officefloor.test.module;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import net.officefloor.test.JUnitAgnosticAssert;

/**
 * Checks whether a {@link Package} is accessible.
 * 
 * @author Daniel Sagenschneider
 */
public class ModuleAccessible {

	/**
	 * Indicates if the {@link Field} is available.
	 * 
	 * @param clazz     {@link Class} containing the {@link Field}.
	 * @param fieldName Name of the {@link Field}.
	 * @return <code>true</code> if {@link Field} is available.
	 */
	public static boolean isFieldAvailable(Class<?> clazz, String fieldName) {
		return getField(clazz, fieldName, false) != null;
	}

	/**
	 * Specifies the {@link Field} value.
	 * 
	 * @param object    Object containing the {@link Field}.
	 * @param fieldName Name of the {@link Field}.
	 * @param value     Value to set on the {@link Field}.
	 * @param message   Message indicating what requires setting the {@link Field}
	 *                  value.
	 */
	public static void setFieldValue(Object object, String fieldName, Object value, String message) {
		Field field = getField(object.getClass(), fieldName, true);
		setFieldValue(object, field, value, message);
	}

	/**
	 * Specifies the {@link Field} value.
	 * 
	 * @param object  Object containing the {@link Field}.
	 * @param field   {@link Field}.
	 * @param value   Value to set on the {@link Field}.
	 * @param message Message indicating what requires setting the {@link Field}
	 *                value.
	 */
	public static void setFieldValue(Object object, Field field, Object value, String message) {
		ensureAccessible(field, message);
		try {
			field.set(object, value);
		} catch (IllegalArgumentException | IllegalAccessException ex) {
			JUnitAgnosticAssert.fail("Unable to set field " + field.getDeclaringClass().getSimpleName() + "."
					+ field.getName() + " with value " + value, ex);
		}
	}

	/**
	 * Obtains the {@link Field} value.
	 * 
	 * @param object  Object containing the {@link Field}.
	 * @param field   Name of the {@link Field}.
	 * @param message Message indicating what requires the {@link Field} value.
	 * @return {@link Field} value.
	 */
	public static Object getFieldValue(Object object, String fieldName, String message) {
		return getFieldValue(object, object.getClass(), fieldName, message);
	}

	/**
	 * Obtains the {@link Field} value.
	 * 
	 * @param object    Object containing the {@link Field}. May be
	 *                  <code>null</code> for static {@link Field}.
	 * @param clazz     {@link Class} containing the field.
	 * @param fieldName Name of the {@link Field}.
	 * @param message   Message indicating what requires the {@link Field} value.
	 * @return {@link Field} value.
	 */
	public static Object getFieldValue(Object object, Class<?> clazz, String fieldName, String message) {
		Field field = getField(clazz, fieldName, true);
		return getFieldValue(object, field, message);
	}

	/**
	 * Obtains the {@link Field} value.
	 * 
	 * @param object  Object containing the {@link Field}.
	 * @param field   {@link Field}.
	 * @param message Message indicating what requires the {@link Field} value.
	 * @return {@link Field} value.
	 */
	public static Object getFieldValue(Object object, Field field, String message) {
		ensureAccessible(field, message);
		try {
			return field.get(object);
		} catch (IllegalArgumentException | IllegalAccessException ex) {
			return JUnitAgnosticAssert.fail(
					"Unable to get field value " + field.getDeclaringClass().getSimpleName() + "." + field.getName(),
					ex);
		}
	}

	/**
	 * Invokes the {@link Method}.
	 * 
	 * @param object     Object containing the {@link Method}. May be
	 *                   <code>null</code> for static {@link Method}.
	 * @param method     {@link Method}.
	 * @param message    Messaging indicating what requires invoking the
	 *                   {@link Method}.
	 * @param parameters Parameters to the {@link Method} invocation.
	 * @return {@link Method} return.
	 */
	public static Object invokeMethod(Object object, Method method, String message, Object... parameters) {
		ensureAccessible(method, message);
		try {
			return method.invoke(object, parameters);
		} catch (InvocationTargetException ex) {
			return JUnitAgnosticAssert.fail(ex.getCause());
		} catch (IllegalAccessException | IllegalArgumentException ex) {
			return JUnitAgnosticAssert.fail(ex);
		}
	}

	/**
	 * Obtains the {@link Field}.
	 * 
	 * @param clazz     {@link Class} containing the {@link Field}.
	 * @param fieldName Name of the {@link Field}.
	 * @param isError   Indicates if should error (otherwise returns
	 *                  <code>null</code>).
	 * @return {@link Field}.
	 */
	private static Field getField(Class<?> clazz, String fieldName, boolean isError) {
		try {
			Class<?> declaringClazz = clazz;
			do {
				// Attempt to obtain field
				try {
					Field field = declaringClazz.getDeclaredField(fieldName);
					if (field != null) {
						return field;
					}
				} catch (NoSuchFieldException ex) {
					// Attempt in super class
				}

				// Attempt to find in parent class
				declaringClazz = declaringClazz.getSuperclass();
			} while (declaringClazz != null);

		} catch (SecurityException ex) {
			return JUnitAgnosticAssert.fail("Failed to obtain field " + fieldName + " from class " + clazz.getName(),
					ex);
		}

		// No field
		return isError ? JUnitAgnosticAssert.fail("No field " + fieldName + " on class " + clazz.getName()) : null;
	}

	/**
	 * Ensures the {@link Member} is able to be made accessible.
	 * 
	 * @param member  {@link Member} to check can be made accessible.
	 * @param message Message indicating what requires accessibility.
	 */
	private static void ensureAccessible(Member member, String message) {

		// Ensure the package of the module is open
		Class<?> fieldClass = member.getDeclaringClass();
		String fieldPackageName = fieldClass.getPackageName();
		Module fieldModule = fieldClass.getModule();
		if (!fieldModule.isOpen(fieldPackageName, ModuleAccessible.class.getModule())) {
			String log = "\n" + message
					+ "\n\nThe following must be added to the JVM arguments to run test:\n\n--add-opens "
					+ fieldModule.getName() + "/" + fieldPackageName
					+ "=ALL-UNNAMED\n\nNote: only use for testing. Do NOT use in production.\n";
			System.err.println(log);
			JUnitAgnosticAssert.fail("\n" + log);
		}
	}

	/**
	 * All access via static methods.
	 */
	private ModuleAccessible() {
	}

}