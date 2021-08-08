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

package net.officefloor.plugin.xml.marshall.translate;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.plugin.xml.XmlMarshallException;

/**
 * Registry of {@link net.officefloor.plugin.xml.marshall.translate.Translator}
 * instances by type they translate.
 * 
 * @author Daniel Sagenschneider
 */
public class TranslatorRegistry {

	/**
	 * Default {@link Translator}.
	 */
	protected final Translator defaultTranslator;

	/**
	 * Map of {@link Translator} instances.
	 */
	protected final Map<Class<?>, Translator> translators;

	/**
	 * Default Constructor.
	 */
	public TranslatorRegistry() {
		// Specify the default translator
		this.defaultTranslator = new DefaultTranslator();

		// Create registry of translators
		this.translators = new HashMap<Class<?>, Translator>();
	}

	/**
	 * Registers a {@link Translator}.
	 * 
	 * @param type
	 *            Type the {@link Translator} translates.
	 * @param translator
	 *            {@link Translator}.
	 */
	public void registerTranslator(Class<?> type, Translator translator) {
		this.translators.put(type, translator);
	}

	/**
	 * Obtains the {@link Translator} for the input type.
	 * 
	 * @param type
	 *            Type requiring a {@link Translator}.
	 * @return {@link Translator} for the type.
	 * @throws XmlMarshallException
	 *             If fails to obtain a {@link Translator}.
	 */
	public Translator getTranslator(Class<?> type) throws XmlMarshallException {
		// Obtain translator for specific type
		Translator translator = this.translators.get(type);

		// Use default if not found for specific type
		if (translator == null) {
			translator = this.defaultTranslator;
		}

		// Return translator
		return translator;
	}
}

/**
 * Default {@link net.officefloor.plugin.xml.marshall.translate.Translator}.
 */
class DefaultTranslator implements Translator {

	public String translate(Object object) throws XmlMarshallException {
		// Return toString
		return (object == null ? "" : object.toString());
	}

}
