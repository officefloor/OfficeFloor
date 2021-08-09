/*-
 * #%L
 * [bundle] OfficeFloor Configurer
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
