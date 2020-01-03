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