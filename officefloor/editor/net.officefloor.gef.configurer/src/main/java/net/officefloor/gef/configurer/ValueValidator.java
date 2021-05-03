/*-
 * #%L
 * [bundle] OfficeFloor Configurer
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

package net.officefloor.gef.configurer;

import javafx.beans.property.ReadOnlyProperty;

/**
 * Validates the value.
 * 
 * @author Daniel Sagenschneider
 */
public interface ValueValidator<M, V> {

	/**
	 * Convenience {@link ValueValidator} for ensuring not <code>null</code>.
	 * 
	 * @param <M>          Model type.
	 * @param <V>          Value type.
	 * @param errorMessage Error message if <code>null</code>.
	 * @return {@link ValueValidator} to validate not <code>null</code>.
	 */
	public static <M, V> ValueValidator<M, V> notNull(String errorMessage) {
		return (context) -> {
			Object value = context.getValue().getValue();
			notNull(value, errorMessage, context);
		};
	}

	/**
	 * Convenience method to provide error if value is <code>null</code>.
	 * 
	 * @param value        Value to check for <code>null</code>.
	 * @param errorMessage Error message if empty string.
	 * @param context      {@link ValueValidatorContext}.
	 */
	public static void notNull(Object value, String errorMessage, ValueValidatorContext<?, ?> context) {
		if (value == null) {
			context.setError(errorMessage);
		}
	}

	/**
	 * Convenience {@link ValueValidator} for ensuring not an empty {@link String}.
	 *
	 * @param <M>          Model type.
	 * @param errorMessage Error message if empty {@link String}.
	 * @return {@link ValueValidator} to validate not an empty {@link String}.
	 */
	public static <M> ValueValidator<M, String> notEmptyString(String errorMessage) {
		return (context) -> {
			String value = context.getValue().getValue();
			notEmptyString(value, errorMessage, context);
		};
	}

	/**
	 * Convenience method to provide error if value is empty string.
	 * 
	 * @param value        Value to check for empty string.
	 * @param errorMessage Error message if empty string.
	 * @param context      {@link ValueValidatorContext}.
	 */
	public static void notEmptyString(String value, String errorMessage, ValueValidatorContext<?, ?> context) {
		if ((value == null) || (value.trim().length() == 0)) {
			context.setError(errorMessage);
		}
	}

	/**
	 * Undertakes the validation.
	 * 
	 * @param context {@link ValueValidatorContext}.
	 * @throws Exception If failure in validation. Message of {@link Exception} is
	 *                   used as error.
	 */
	void validate(ValueValidatorContext<? extends M, V> context) throws Exception;

	/**
	 * Context for the {@link ValueValidator}.
	 */
	public interface ValueValidatorContext<M, V> {

		/**
		 * Obtains the model.
		 * 
		 * @return Model.
		 */
		M getModel();

		/**
		 * Obtains the value.
		 * 
		 * @return Value.
		 */
		ReadOnlyProperty<V> getValue();

		/**
		 * Specifies an error.
		 * 
		 * @param message Message.
		 */
		void setError(String message);

		/**
		 * <p>
		 * Triggers reloading the value from the model for the particular
		 * {@link Builder}.
		 * <p>
		 * This allows validation to update the model and reload values from the model.
		 * 
		 * @param builder {@link Builder} to identify the value to reload.
		 */
		void reload(Builder<?, ?, ?> builder);
	}

}
