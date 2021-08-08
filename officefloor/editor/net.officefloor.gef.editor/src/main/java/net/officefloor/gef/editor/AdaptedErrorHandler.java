/*-
 * #%L
 * [bundle] OfficeFloor Editor
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

package net.officefloor.gef.editor;

import java.util.function.Function;

/**
 * Error handler that displays the error to the user.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdaptedErrorHandler {

	/**
	 * Shows the error.
	 * 
	 * @param message Error message to show.
	 */
	void showError(String message);

	/**
	 * Shows the error.
	 * 
	 * @param error {@link Throwable} error to show.
	 */
	void showError(Throwable error);

	/**
	 * Runs an {@link UncertainOperation}.
	 * 
	 * <pre>
	 * UncertainOperation operation = () -&gt; { ... };
	 * if (handler.isError(operation) {
	 * 	  return; // failure in operation
	 * }
	 * </pre>
	 * 
	 * @param operation {@link UncertainOperation}.
	 * @return <code>true</code> if {@link UncertainOperation} threw an
	 *         {@link Exception}. The {@link Exception} will displayed visually to
	 *         the user.
	 */
	boolean isError(UncertainOperation operation);

	/**
	 * {@link Function} interface for an uncertain operation that may fail.
	 */
	public interface UncertainOperation {

		/**
		 * Uncertain logic.
		 * 
		 * @throws Throwable Failure in the uncertain logic.
		 */
		void run() throws Throwable;
	}

	/**
	 * Message only {@link Exception}.
	 */
	public static class MessageOnlyException extends RuntimeException {

		/**
		 * Serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Instantiate.
		 * 
		 * @param message Message.
		 */
		public MessageOnlyException(String message) {
			super(message);
		}
	}

}
