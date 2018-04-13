/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.eclipse.configurer;

import javafx.beans.property.ReadOnlyProperty;

/**
 * Validates the value.
 * 
 * @author Daniel Sagenschneider
 */
public interface ValueValidator<V> {

	/**
	 * Convenience {@link ValueValidator} for ensuring not an empty {@link String}.
	 * 
	 * @param errorMessage
	 *            Error message if empty {@link String}.
	 * @return {@link ValueValidator} to validate not an empty {@link String}.
	 */
	public static ValueValidator<String> notEmptyString(String errorMessage) {
		return (context) -> {
			String value = context.getValue().getValue();
			if ((value == null) || (value.trim().length() == 0)) {
				context.setError(errorMessage);
			}
		};
	}

	/**
	 * Undertakes the validation.
	 * 
	 * @param context
	 *            {@link ValueValidatorContext}.
	 * @throws Exception
	 *             If failure in validation. Message of {@link Exception} is used as
	 *             error.
	 */
	void validate(ValueValidatorContext<V> context) throws Exception;

	/**
	 * Context for the {@link ValueValidator}.
	 */
	public interface ValueValidatorContext<V> {

		/**
		 * Obtains the value.
		 * 
		 * @return Value.
		 */
		ReadOnlyProperty<V> getValue();

		/**
		 * Specifies an error.
		 * 
		 * @param message
		 *            Message.
		 */
		void setError(String message);
	}

}