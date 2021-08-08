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

package net.officefloor.plugin.xml.unmarshall.load;

import java.lang.reflect.Method;

import net.officefloor.plugin.xml.XmlMarshallException;
import net.officefloor.plugin.xml.unmarshall.translate.Translator;
import net.officefloor.plugin.xml.unmarshall.translate.TranslatorRegistry;

/**
 * Loads string values onto a target object.
 * 
 * @author Daniel Sagenschneider
 */
public class ValueLoaderFactory {

	/**
	 * Registry of {@link Translator} objects.
	 */
	protected final TranslatorRegistry translatorRegistry;

	/**
	 * Class of the target object.
	 */
	protected final Class<?> targetObjectType;

	/**
	 * Initiate the value loader.
	 * 
	 * @param translatorRegistry
	 *            Registry of translators.
	 * @param targetObjectType
	 *            Class of the target object.
	 */
	public ValueLoaderFactory(TranslatorRegistry translatorRegistry,
			Class<?> targetObjectType) {
		// Initiate state
		this.translatorRegistry = translatorRegistry;
		this.targetObjectType = targetObjectType;
	}

	/**
	 * Creates the {@link DynamicValueLoader}for the load method of the target
	 * object.
	 * 
	 * @param loadMethodName
	 *            Name of method to use to load the value.
	 * @return {@link DynamicValueLoader}to load the value to the target object.
	 * @throws XmlMarshallException
	 *             Should there be a failure to create the
	 *             {@link DynamicValueLoader}.
	 */
	public DynamicValueLoader createDynamicValueLoader(String loadMethodName)
			throws XmlMarshallException {

		// Obtain the method to load the value
		Method loadMethod = this.findMethod(this.targetObjectType,
				loadMethodName);

		// Obtain the parameter class (will always have one parameter)
		Class<?> parameterType = loadMethod.getParameterTypes()[0];

		// Obtain the translator for parameter type
		Translator translator = (Translator) this.translatorRegistry
				.getTranslator(parameterType);

		// Return the value loader
		return new DynamicValueLoader(loadMethod, translator);
	}

	/**
	 * Creates the {@link StaticValueLoader} for the load method of the target
	 * object.
	 * 
	 * @param loadMethodName
	 *            Name of method to use to load the static value.
	 * @param value
	 *            Static value to load to the target object.
	 * @return {@link StaticValueLoader} to load the static value onto the
	 *         target object.
	 * @throws XmlMarshallException
	 *             Should there be a failure to create the
	 *             {@link StaticValueLoader}.
	 */
	public StaticValueLoader createStaticValueLoader(String loadMethodName,
			String value) throws XmlMarshallException {

		// Obtain the method to load the value
		Method loadMethod = this.findMethod(this.targetObjectType,
				loadMethodName);

		// Obtain the parameter class (will always have one parameter)
		Class<?> parameterType = loadMethod.getParameterTypes()[0];

		// Obtain the translator for parameter type
		Translator translator = (Translator) this.translatorRegistry
				.getTranslator(parameterType);

		// Translate the value
		Object translatedValue = translator.translate(value);

		// Return the value loader
		return new StaticValueLoader(loadMethod, translatedValue);
	}

	/**
	 * Obtains the method of the input class by the input method name.
	 * 
	 * @param objectClass
	 *            Class to identify the method from.
	 * @param loadMethodName
	 *            Name of method to find.
	 * @return Method of class.
	 * @throws XmlMarshallException
	 *             If failed to find method to load.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Method findMethod(Class objectClass, String loadMethodName)
			throws XmlMarshallException {

		// Try with String parameter first
		try {
			return objectClass.getMethod(loadMethodName,
					new Class[] { String.class });
		} catch (SecurityException e) {
			// Ignore as not able to reach
		} catch (NoSuchMethodException e) {
			// No method
		}

		// Not found with default of String thus find first matching
		Method[] methods = objectClass.getMethods();
		for (int i = 0; i < methods.length; i++) {
			// Obtain the current method
			Method currentMethod = methods[i];

			// Check if matching method name
			if (loadMethodName.equals(currentMethod.getName())) {
				// Ensure that there is only one parameter to method
				Class[] parameterTypes = currentMethod.getParameterTypes();
				if ((parameterTypes != null) && (parameterTypes.length == 1)) {
					// Found matching method
					return currentMethod;
				}
			}
		}

		// Method not found
		throw new XmlMarshallException("Can not find method '" + loadMethodName
				+ "' on class '" + objectClass.getName() + "'.");
	}

}
