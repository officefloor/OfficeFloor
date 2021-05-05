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

/**
 * Builds the configuration.
 * 
 * @author Daniel Sagenschneider
 */
public interface ConfigurationBuilder<M> extends InputBuilder<M> {

	/**
	 * Specifies the label for this configuration.
	 * 
	 * @param title Title for this configuration.
	 * @return <code>this</code>.
	 */
	ConfigurationBuilder<M> title(String title);

	/**
	 * Validates the model.
	 * 
	 * @param validator {@link ValueValidator}.
	 */
	void validate(ValueValidator<M, M> validator);

	/**
	 * Specifies the {@link ErrorListener}.
	 * 
	 * @param errorListener {@link ErrorListener}.
	 */
	void error(ErrorListener errorListener);

	/**
	 * Applier.
	 */
	@FunctionalInterface
	interface Applier<M> {

		/**
		 * Applies the configuration.
		 * 
		 * @param model Model.
		 * @throws Throwable Possible failure in applying the change.
		 */
		void apply(M model) throws Throwable;
	}

	/**
	 * Thrown from {@link Applier} to indicate message only error. If thrown the
	 * stack trace will not be displayed (nor any causes).
	 */
	public class MessageOnlyApplyException extends Exception {

		/**
		 * Serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Instantiate.
		 * 
		 * @param message Message.
		 */
		public MessageOnlyApplyException(String message) {
			super(message);
		}
	}

	/**
	 * Configures a {@link Applier} to apply the configured model.
	 * 
	 * @param label   Label for the applying {@link Actioner}.
	 * @param applier {@link Applier} to apply the configured model.
	 */
	void apply(String label, Applier<M> applier);

	/**
	 * Specifies the {@link CloseListener}.
	 * 
	 * @param closeListener {@link CloseListener}.
	 */
	void close(CloseListener closeListener);

}
