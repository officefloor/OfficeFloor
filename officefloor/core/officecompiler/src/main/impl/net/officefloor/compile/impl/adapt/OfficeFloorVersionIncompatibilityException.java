package net.officefloor.compile.impl.adapt;

import java.lang.reflect.Method;

import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Indicates the adapting type is incompatible.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorVersionIncompatibilityException extends RuntimeException {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs and throws the {@link OfficeFloorVersionIncompatibilityException}.
	 * 
	 * @param implementation Implementation.
	 * @param methodName     Name of the incompatible {@link Method}.
	 * @param parameterTypes Parameter types for the incompatible {@link Method}.
	 * @return OfficeFloorVersionIncompatibilityException The
	 *         {@link OfficeFloorVersionIncompatibilityException} to throw.
	 */
	public static OfficeFloorVersionIncompatibilityException newTypeIncompatibilityException(Object implementation,
			String methodName, Class<?>[] parameterTypes) {

		// Build the message
		StringBuilder msg = new StringBuilder();
		msg.append(OfficeFloor.class.getSimpleName());
		msg.append(" version incompatibility as implementation used does not support method ");
		msg.append(implementation.getClass().getSimpleName());
		msg.append(".");
		msg.append(methodName);
		msg.append("(");
		boolean isFirst = true;
		for (Class<?> parameterType : parameterTypes) {
			if (!isFirst) {
				msg.append(", ");
			}
			isFirst = false;
			msg.append(parameterType == null ? "?" : parameterType.getSimpleName());
		}
		msg.append(")");

		// Return the exception
		return new OfficeFloorVersionIncompatibilityException(msg.toString());
	}

	/**
	 * Initiate.
	 * 
	 * @param message Message.
	 */
	private OfficeFloorVersionIncompatibilityException(String message) {
		super(message);
	}

}