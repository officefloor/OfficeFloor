/*-
 * #%L
 * OfficeXml
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

package net.officefloor.plugin.xml.unmarshall.translate;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.officefloor.plugin.xml.XmlMarshallException;

/**
 * Registry of the
 * 
 * @author Daniel Sagenschneider
 */
public class TranslatorRegistry {

	/**
	 * Map of the translators.
	 */
	protected final Map<Class<?>, Translator> translators;

	/**
	 * Default constructor.
	 */
	public TranslatorRegistry() {
		// Create the internal registry
		this.translators = new HashMap<Class<?>, Translator>();

		// Initiate the default translators
		translators.put(String.class, new StringTranslator(null));
		translators.put(boolean.class, new BooleanTranslator(Boolean.FALSE));
		translators.put(Boolean.class, new BooleanTranslator(null));
		translators.put(char.class, new CharacterTranslator(Character.valueOf(' ')));
		translators.put(Character.class, new CharacterTranslator(null));
		translators.put(byte.class, new ByteTranslator(Byte.valueOf((byte) 0)));
		translators.put(Byte.class, new ByteTranslator(null));
		translators.put(int.class, new IntegerTranslator(Integer.valueOf(0)));
		translators.put(Integer.class, new IntegerTranslator(null));
		translators.put(long.class, new LongTranslator(Long.valueOf(0)));
		translators.put(Long.class, new LongTranslator(null));
		translators.put(float.class, new FloatTranslator(Float.valueOf(0)));
		translators.put(Float.class, new FloatTranslator(null));
		translators.put(double.class, new DoubleTranslator(Double.valueOf(0)));
		translators.put(Double.class, new DoubleTranslator(null));
		translators.put(Date.class, new DateTranslator(null));
	}

	/**
	 * Registers a {@link Translator}.
	 * 
	 * @param type       Type the {@link Translator} translates.
	 * @param translator {@link Translator}.
	 */
	public void registerTranslator(Class<?> type, Translator translator) {
		this.translators.put(type, translator);
	}

	/**
	 * Obtains the {@link Translator}to the specific type.
	 * 
	 * @param translateType Type of {@link Translator}.
	 * @return {@link Translator}to the specific type input.
	 * @throws XmlMarshallException Should a {@link Translator}not be registered for
	 *                              the translateType.
	 */
	public Translator getTranslator(Class<?> translateType) throws XmlMarshallException {
		// Return translator to specific type
		Translator translator = this.translators.get(translateType);

		// Ensure have translator
		if (translator == null) {
			throw new XmlMarshallException(
					"No " + Translator.class.getName() + " loaded for parameter type of " + translateType.getName());
		}

		// Return translator
		return translator;
	}

}

/**
 * Abstract {@link Translator}which handles object or primitive translation for
 * nulls.
 */
abstract class AbstractTranslator implements Translator {

	/**
	 * Indicates if primitive translation.
	 */
	protected final boolean isPrimitive;

	/**
	 * Default value if a primitive value.
	 */
	protected final Object defaultValue;

	/**
	 * Initiate for translation.
	 * 
	 * @param defaultValue Null indicates object translation while a value
	 *                     indiciates primitive translation.
	 */
	public AbstractTranslator(Object defaultValue) {
		// Initiate for primitive translation
		this.isPrimitive = (defaultValue != null);
		this.defaultValue = defaultValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.plugin.xml.translate.Translator#translate(java.lang.String)
	 */
	public Object translate(String value) throws XmlMarshallException {
		// Check if null value
		if ((value == null) || (value.trim().length() == 0)) {
			// Value is null, thus return appropriate null value
			return (this.isPrimitive ? this.defaultValue : null);
		} else {
			// Value is not null, thus return its translation
			return this.translateNotNullValue(value);
		}
	}

	/**
	 * Translates value to specific value.
	 * 
	 * @param value Non-null value.
	 * @return Specific value.
	 * @throws XmlMarshallException Indicating failure to translate value.
	 */
	protected abstract Object translateNotNullValue(String value) throws XmlMarshallException;

}

/**
 * Translate to Boolean.
 */
class BooleanTranslator extends AbstractTranslator {
	public BooleanTranslator(Object defaultValue) {
		super(defaultValue);
	}

	public Object translateNotNullValue(String value) throws XmlMarshallException {
		return Boolean.valueOf(value);
	}
}

/**
 * Translate to Character.
 */
class CharacterTranslator extends AbstractTranslator {
	public CharacterTranslator(Object defaultValue) {
		super(defaultValue);
	}

	public Object translateNotNullValue(String value) throws XmlMarshallException {
		// Obtain the first character
		return Character.valueOf(value.charAt(0));
	}
}

/**
 * Abstract implementation of {@link Translator}to translate to a number.
 */
abstract class AbstractNumericTranslator extends AbstractTranslator {
	public AbstractNumericTranslator(Object defaultValue) {
		super(defaultValue);
	}

	/**
	 * Translate to numeric value.
	 */
	public Object translateNotNullValue(String value) throws XmlMarshallException {
		try {
			return this.translateToNumber(value);
		} catch (NumberFormatException ex) {
			// Propagate failure
			throw new XmlMarshallException("Failed to format value '" + value + "' to number.", ex);
		}
	}

	/**
	 * Translates to a numeric value.
	 * 
	 * @param value Value to be translated to a numeric value.
	 * @return Numeric value.
	 * @throws NumberFormatException If failed to translate to a number.
	 */
	abstract Object translateToNumber(String value) throws NumberFormatException;

}

/**
 * Translate to Byte.
 */
class ByteTranslator extends AbstractNumericTranslator {
	public ByteTranslator(Object defaultValue) {
		super(defaultValue);
	}

	public Object translateToNumber(String value) {
		return Byte.valueOf(value);
	}
}

/**
 * Translate to Integer.
 */
class IntegerTranslator extends AbstractNumericTranslator {
	public IntegerTranslator(Object defaultValue) {
		super(defaultValue);
	}

	public Object translateToNumber(String value) throws NumberFormatException {
		return Integer.valueOf(value);
	}
}

/**
 * Translate to Long.
 */
class LongTranslator extends AbstractNumericTranslator {
	public LongTranslator(Object defaultValue) {
		super(defaultValue);
	}

	public Object translateToNumber(String value) throws NumberFormatException {
		return Long.valueOf(value);
	}
}

/**
 * Translate to Float.
 */
class FloatTranslator extends AbstractNumericTranslator {
	public FloatTranslator(Object defaultValue) {
		super(defaultValue);
	}

	public Object translateToNumber(String value) throws NumberFormatException {
		return Float.valueOf(value);
	}
}

/**
 * Translate to Double.
 */
class DoubleTranslator extends AbstractNumericTranslator {
	public DoubleTranslator(Object defaultValue) {
		super(defaultValue);
	}

	public Object translateToNumber(String value) throws NumberFormatException {
		return Double.valueOf(value);
	}
}

/**
 * Translate to String.
 */
class StringTranslator extends AbstractTranslator {
	public StringTranslator(Object defaultValue) {
		super(defaultValue);
	}

	public Object translateNotNullValue(String value) {
		return value;
	}
};

/**
 * Translate to Date.
 */
class DateTranslator extends AbstractTranslator {
	public DateTranslator(Object defaultValue) {
		super(defaultValue);
	}

	@SuppressWarnings("deprecation")
	public Object translateNotNullValue(String value) throws XmlMarshallException {
		try {
			return new Date(value);
		} catch (IllegalArgumentException ex) {
			// Propagate failure
			throw new XmlMarshallException("Failed to translate value '" + value + "' to a date", ex);
		}
	}
}
