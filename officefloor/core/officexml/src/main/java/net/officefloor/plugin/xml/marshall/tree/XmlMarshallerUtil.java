/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.xml.marshall.tree;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.officefloor.plugin.xml.XmlMarshallException;
import net.officefloor.plugin.xml.XmlOutput;

/**
 * Provides utility methods to write XML.
 * 
 * @author Daniel Sagenschneider
 */
public class XmlMarshallerUtil {

	/**
	 * Indiates no parameters in attempting to obtain the Method.
	 */
	private static final Class<?>[] NO_PARAMETERS = new Class[0];

	/**
	 * Writes the xml to the output.
	 * 
	 * @param xmlSnippet
	 *            XML snippet to write.
	 * @param output
	 *            Output to send XML snippet.
	 * @throws XmlMarshallException
	 *             If fails to write XML.
	 */
	protected static void writeXml(String xmlSnippet, XmlOutput output)
			throws XmlMarshallException {
		try {
			output.write(xmlSnippet);
		} catch (IOException ex) {
			// Propagate failure
			throw new XmlMarshallException("XML write failure: "
					+ ex.getMessage(), ex);
		}
	}

	/**
	 * Transforms the input value for XML.
	 * 
	 * @param value
	 *            Value to transformed.
	 * @return Value transformed to be utilised in XML.
	 */
	public static String transformValueForXml(String value) {

		// Ensure have value
		if (value == null) {
			return "";
		}

		// Transform for xml
		value = value.replaceAll("&", "&amp;");
		value = value.replaceAll("\"", "&quot;");
		value = value.replaceAll("'", "&apos;");
		value = value.replaceAll("<", "&lt;");
		value = value.replaceAll(">", "&gt;");

		// Return transformed value
		return value;
	}

	/**
	 * Obtains the {@link Class} from its class name.
	 * 
	 * @param className
	 *            Name of the class.
	 * @return {@link Class} by the input class name.
	 * @throws XmlMarshallException
	 *             If fails to obtain the {@link Class}.
	 */
	protected static Class<?> obtainClass(String className)
			throws XmlMarshallException {
		try {
			return Class.forName(className);
		} catch (ClassNotFoundException ex) {
			// Propagate failure
			throw new XmlMarshallException("Class '" + className
					+ "' was not found.", ex);
		}
	}

	/**
	 * Obtains the Method by its name from the input class.
	 * 
	 * @param object
	 *            Class of the object to obtain the method.
	 * @param methodName
	 *            Name of the method to obtain.
	 * @return Method on the input class by the name.
	 * @throws XmlMarshallException
	 *             If unable to obtain method.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected static Method obtainMethod(Class object, String methodName)
			throws XmlMarshallException {
		try {
			return object.getMethod(methodName, NO_PARAMETERS);
		} catch (SecurityException ex) {
			// Propagate failure
			throw new XmlMarshallException("Method '" + methodName
					+ "()' is not accessible on class " + object.getName(), ex);
		} catch (NoSuchMethodException ex) {
			// Propagate failure
			throw new XmlMarshallException("Can not find method '" + methodName
					+ "' on class " + object.getName(), ex);
		}
	}

	/**
	 * Obtains the return value from the input method on the source object.
	 * 
	 * @param source
	 *            Object to source the return value.
	 * @param getMethod
	 *            Method to utilise to obtain the value.
	 * @return Return value of method on source object.
	 * @throws XmlMarshallException
	 *             If fails to obtain the return value.
	 */
	protected static Object getReturnValue(Object source, Method getMethod)
			throws XmlMarshallException {
		try {
			// Load value onto target object
			return getMethod.invoke(source, (Object[]) null);
		} catch (IllegalArgumentException ex) {
			// Propagate failure
			throw new XmlMarshallException(
					"Parameters required but accessing method '"
							+ getMethod.getName() + "' without parameters", ex);
		} catch (IllegalAccessException ex) {
			// Propagate failure
			throw new XmlMarshallException("Illegal access to method '"
					+ getMethod.getName() + "'", ex);
		} catch (InvocationTargetException ex) {
			// Propagate failure
			throw new XmlMarshallException("Invoked get method failed.",
					ex.getCause());
		}
	}
}
