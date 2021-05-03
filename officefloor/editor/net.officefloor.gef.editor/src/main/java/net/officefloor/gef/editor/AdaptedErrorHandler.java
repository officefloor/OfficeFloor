/*-
 * #%L
 * [bundle] OfficeFloor Editor
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
