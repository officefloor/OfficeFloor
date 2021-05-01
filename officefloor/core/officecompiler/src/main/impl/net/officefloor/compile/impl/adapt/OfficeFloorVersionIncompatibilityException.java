/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.compile.impl.adapt;

import java.lang.reflect.InaccessibleObjectException;
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
		populateIncompatibleMessage(msg, implementation, methodName, parameterTypes);

		// Return the exception
		return new OfficeFloorVersionIncompatibilityException(msg.toString());
	}

	/**
	 * Constructs and throws the {@link OfficeFloorVersionIncompatibilityException}.
	 * 
	 * @param cause          {@link InaccessibleObjectException}.
	 * @param implementation Implementation.
	 * @param methodName     Name of the incompatible {@link Method}.
	 * @param parameterTypes Parameter types for the incompatible {@link Method}.
	 * @return OfficeFloorVersionIncompatibilityException The
	 *         {@link OfficeFloorVersionIncompatibilityException} to throw.
	 */
	public static OfficeFloorVersionIncompatibilityException newTypeInaccessibleException(
			InaccessibleObjectException cause, Object implementation, String methodName, Class<?>[] parameterTypes) {

		// Build the message
		StringBuilder msg = new StringBuilder();
		msg.append(OfficeFloor.class.getSimpleName());
		msg.append(" version incompatibility as implementation not accessible ");
		populateIncompatibleMessage(msg, implementation, methodName, parameterTypes);

		// Return the exception
		return new OfficeFloorVersionIncompatibilityException(msg.toString(), cause);
	}

	/**
	 * Populates the incompatible message.
	 * 
	 * @param msg            Receive the incompatible message.
	 * @param implementation Implementation.
	 * @param methodName     Name of the incompatible {@link Method}.
	 * @param parameterTypes Parameter types for the incompatible {@link Method}.
	 */
	private static void populateIncompatibleMessage(StringBuilder msg, Object implementation, String methodName,
			Class<?>[] parameterTypes) {
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
	}

	/**
	 * Initiate.
	 * 
	 * @param message Message.
	 */
	private OfficeFloorVersionIncompatibilityException(String message) {
		super(message);
	}

	/**
	 * Initiate.
	 * 
	 * @param message Message.
	 * @parm
	 */
	private OfficeFloorVersionIncompatibilityException(String message, Throwable cause) {
		super(message, cause);
	}

}
