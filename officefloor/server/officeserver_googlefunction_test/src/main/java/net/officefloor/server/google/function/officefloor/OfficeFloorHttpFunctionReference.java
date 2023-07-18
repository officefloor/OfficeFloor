package net.officefloor.server.google.function.officefloor;

import com.google.cloud.functions.HttpFunction;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.test.JUnitAgnosticAssert;

/**
 * Reference to the {@link OfficeFloor} {@link HttpFunction} implementation.
 */
public class OfficeFloorHttpFunctionReference {

	/**
	 * {@link Class} name of the {@link OfficeFloor} implementing
	 * {@link HttpFunction}.
	 */
	public static final String OFFICEFLOOR_HTTP_FUNCTION_CLASS_NAME = "net.officefloor.server.google.function.OfficeFloorHttpFunction";

	/**
	 * Obtains the {@link OfficeFloor} implementing {@link HttpFunction}.
	 * 
	 * @return {@link OfficeFloor} implementing {@link HttpFunction}.
	 */
	public static Class<?> getOfficeFloorHttpFunctionClass() {
		try {

			// Attempt to load the default OfficeFloor HttpFunction
			return OfficeFloorHttpFunctionReference.class.getClassLoader()
					.loadClass(OFFICEFLOOR_HTTP_FUNCTION_CLASS_NAME);

		} catch (Exception ex) {
			JUnitAgnosticAssert.fail(ex);
			throw new IllegalStateException("fail should propagate the failure");
		}
	}

}
