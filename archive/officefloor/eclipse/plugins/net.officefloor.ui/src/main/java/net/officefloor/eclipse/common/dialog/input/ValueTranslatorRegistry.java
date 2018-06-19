/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.eclipse.common.dialog.input;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry of {@link ValueTranslator} instances by type.
 * 
 * @author Daniel Sagenschneider
 */
public class ValueTranslatorRegistry {

	/**
	 * Registry of {@link ValueTranslator} instances by type.
	 */
	private static final Map<Class<?>, ValueTranslator> staticTranslators = new HashMap<Class<?>, ValueTranslator>();

	/**
	 * Populates the static {@link valuet
	 */
	static {
		// Register typed translators
		staticTranslators.put(String.class, new AbstractValueTranslator() {
			public Object translateNotNull(String value) {
				return value;
			}
		});
		ValueTranslator intTranslator = new AbstractValueTranslator() {
			public Object translateNotNull(String value) {
				return Integer.parseInt(value);
			}
		};
		staticTranslators.put(int.class, intTranslator);
		staticTranslators.put(Integer.class, intTranslator);
	}

	/**
	 * Abstract {@link ValueTranslator}.
	 */
	private static abstract class AbstractValueTranslator implements
			ValueTranslator {

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.eclipse.common.dialog.input.ValueTranslator#translate(java.lang.Object)
		 */
		@Override
		public Object translate(Object inputValue) throws InvalidValueException {
			try {
				return (inputValue == null ? null : this
						.translateNotNull(inputValue.toString()));
			} catch (NumberFormatException ex) {
				throw new InvalidValueException("Invalid number");
			}
		}

		/**
		 * Translates value with non-null input.
		 * 
		 * @param inputValue
		 *            Value to translate as String.
		 * @return Translated value.
		 * @throws InvalidValueException
		 *             If invalid value or fails to translate.
		 */
		public abstract Object translateNotNull(String inputValue)
				throws InvalidValueException;

	}

	/**
	 * Registry of {@link ValueTranslator} instances for this registry instance.
	 */
	private final Map<Class<?>, ValueTranslator> translators = new HashMap<Class<?>, ValueTranslator>();

	/**
	 * Initiate.
	 * 
	 * @param classLoader
	 *            {@link ClassLoader}.
	 */
	public ValueTranslatorRegistry(final ClassLoader classLoader) {

		// Copy in the static translators
		this.translators.putAll(staticTranslators);

		// Add instance specific translators
		if (classLoader == null) {
			// Disallow class creation
			this.translators.put(Class.class, new ValueTranslator() {
				@Override
				public Object translate(Object inputValue)
						throws InvalidValueException {
					throw new UnsupportedOperationException(
							"Unable to create classes as no ClassLoader provided to "
									+ ValueTranslatorRegistry.class
											.getSimpleName());
				}
			});
		} else {
			// Allow class creation
			this.translators.put(Class.class, new AbstractValueTranslator() {
				public Object translateNotNull(String value)
						throws InvalidValueException {
					try {
						return classLoader.loadClass(value);
					} catch (ClassNotFoundException ex) {
						throw new InvalidValueException("Can not find class");
					}
				}
			});
		}
	}

	/**
	 * Obtains the {@link ValueTranslator} for the type.
	 * 
	 * @param type
	 *            Type.
	 * @return {@link ValueTranslator} for the type or <code>null</code> if
	 *         one not available.
	 */
	public ValueTranslator getValueTranslator(Class<?> type) {
		return this.translators.get(type);
	}

}
