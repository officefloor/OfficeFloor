/*-
 * #%L
 * OfficeCompiler
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
