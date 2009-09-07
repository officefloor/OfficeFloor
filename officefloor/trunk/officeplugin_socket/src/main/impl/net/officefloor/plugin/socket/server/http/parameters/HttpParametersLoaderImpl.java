/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.plugin.socket.server.http.parameters;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.stream.BufferStream;
import net.officefloor.plugin.stream.InputBufferStream;

/**
 * {@link HttpParametersLoader} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class HttpParametersLoaderImpl<T> implements HttpParametersLoader<T> {

	/**
	 * <p>
	 * Mapping of specific {@link Class} to the {@link Method} instances to load
	 * values onto the object.
	 * <p>
	 * This is necessary as the concrete object may vary but still be of the
	 * type and as such will have varying {@link Method} instances to load the
	 * values.
	 * <p>
	 * Size is 2 as typically will require type and concrete object.
	 */
	private final Map<Class<?>, Method[]> typeToMethods = new HashMap<Class<?>, Method[]>(
			2);

	/**
	 * Type of object to be loaded.
	 */
	private Class<?> type;

	/**
	 * Indicates if matching on parameter names is case sensitive.
	 */
	private boolean isCaseSensitive;

	/**
	 * Mapping of the parameter name to the index of the {@link Method} array to
	 * load the value onto the Object.
	 */
	private Map<String, Integer> parameterNameToMethodIndex;

	/**
	 * Names of the methods on the type to load values. The indexes correspond
	 * to the indexes of the parameter name mappings.
	 */
	private String[] methodNames;

	/**
	 * Obtains the methods for the type.
	 *
	 * @param type
	 *            Type to extract the load methods.
	 * @return Load methods.
	 * @throws HttpParametersLoadException
	 *             If fails to obtain the {@link Method} array for the type.
	 */
	private Method[] getMethods(Class<?> type)
			throws HttpParametersLoadException {

		// Ensure thread-safe in lazy loading
		synchronized (this.typeToMethods) {

			// Lazy load the methods for the type
			Method[] methods = this.typeToMethods.get(type);
			if (methods == null) {
				// Obtain the methods
				methods = new Method[this.methodNames.length];
				for (int i = 0; i < methods.length; i++) {
					String methodName = this.methodNames[i];
					try {
						methods[i] = type.getMethod(methodName, String.class);
					} catch (Exception ex) {
						// Should be same type but likely now not, so check
						if (!this.type.isAssignableFrom(type)) {
							// Incorrect object type
							throw new HttpParametersLoadException(
									"Object being loaded (type "
											+ type.getName()
											+ ") is not compatible to mapping type "
											+ this.type.getName());
						} else {
							// Unknown failure, just propagate
							throw new HttpParametersLoadException(ex);
						}
					}
				}

				// Register the methods
				this.typeToMethods.put(type, methods);
			}

			// Return the methods
			return methods;
		}
	}

	/**
	 * Loads the parameters to the Object.
	 *
	 * @param object
	 *            Object to have the parameters loaded.
	 * @param contents
	 *            Contents containing the parameter name/values to be parsed.
	 * @param methods
	 *            {@link Method} array to load the parameters to the Object.
	 * @param tempBuffer
	 *            {@link TempBuffer}.
	 * @throws HttpParametersLoadException
	 *             If fails to load the parameters.
	 */
	private <O extends T> void loadParameters(O object, String contents,
			Method[] methods, TempBuffer tempBuffer)
			throws HttpParametersLoadException {

		// The implementation of this method reduces character array creations
		// and copying by using sub strings. This should both improve parsing
		// performance and reduce memory.

		// Values to aid in parsing
		boolean isPath = false;
		int nameBegin = 0; // start of contents
		int nameEnd = -1;
		int valueBegin = -1;
		int valueEnd = -1;
		boolean isRequireTranslate = false;

		// Iterate over the contents, loading the parameters
		for (int i = 0; i < contents.length(); i++) {
			char character = contents.charAt(i);

			// Handle based on character
			switch (character) {

			case '?':
				// If not processing path then just include
				if (!isPath) {
					// No longer processing path
					isPath = true;
					nameBegin = i + 1; // after '?'
				}
				break;

			case '=':
				// Flag to now obtain value
				nameEnd = i; // before '='
				valueBegin = i + 1; // after '='
				break;

			case '+': // space
			case '%': // escaping
				// Requires translating
				isRequireTranslate = true;
				break;

			case '&':
			case ';':
				// Have parameter name/value, so load
				valueEnd = i; // before terminator
				this.loadParameter(object, contents, nameBegin, nameEnd,
						valueBegin, valueEnd, isRequireTranslate, methods,
						tempBuffer);

				// Reset for next parameter name/value
				nameBegin = i + 1; // after terminator
				nameEnd = -1;
				valueBegin = -1;
				valueEnd = -1;
				isRequireTranslate = false;
				break;

			case '#':
				// At end of parameters as have fragment
				valueEnd = i; // before terminator
				if (valueBegin > 0) {
					// Have name/value before fragment so load
					this.loadParameter(object, contents, nameBegin, nameEnd,
							valueBegin, valueEnd, isRequireTranslate, methods,
							tempBuffer);
				}
				return; // stop parsing
			}
		}

		// Determine if final parameter to load (not terminated)
		if (valueBegin > 0) {
			// Load the final parameter
			valueEnd = contents.length();
			this.loadParameter(object, contents, nameBegin, nameEnd,
					valueBegin, valueEnd, isRequireTranslate, methods,
					tempBuffer);
		}
	}

	/**
	 * Loads the parameter to the Object.
	 *
	 * @param object
	 *            Object to have the parameter loaded.
	 * @param contents
	 *            Contents being parsed that contains the parameter name/values.
	 * @param nameBegin
	 *            Beginning index of name in contents.
	 * @param nameEnd
	 *            Ending index of name in contents.
	 * @param valueBegin
	 *            Beginning index of value in contents.
	 * @param valueEnd
	 *            Ending index of value in contents.
	 * @param isRequireTranslate
	 *            Indicates if a translation is required. {@link Method} array
	 *            to load the parameters to the Object.
	 * @param tempBuffer
	 *            {@link TempBuffer}.
	 * @throws HttpParametersLoadException
	 *             If fails to load the parameter.
	 */
	private <O extends T> void loadParameter(O object, String contents,
			int nameBegin, int nameEnd, int valueBegin, int valueEnd,
			boolean isRequireTranslate, Method[] methods, TempBuffer tempBuffer)
			throws HttpParametersLoadException {

		// Ensure valid
		if ((nameEnd < 0) || (valueBegin < 0) || (valueEnd < 0)) {
			throw new HttpParametersLoadException(
					"Invalid HTTP contents (name " + nameBegin + "," + nameEnd
							+ "  value " + valueBegin + "," + valueEnd + "): "
							+ contents);
		}

		// Obtain the raw name and value
		String rawName = contents.substring(nameBegin, nameEnd);
		String rawValue = contents.substring(valueBegin, valueEnd);

		// Obtain the name and value
		String name = (isRequireTranslate ? this.translate(rawName, tempBuffer)
				: rawName);
		String value = (isRequireTranslate ? this.translate(rawValue,
				tempBuffer) : rawValue);

		// Obtain the method for the name
		String compareParameterName = this.getComparisonParameterName(name);
		Integer methodIndex = this.parameterNameToMethodIndex
				.get(compareParameterName);
		if (methodIndex != null) {
			// Parameter to be loaded, therefore load it
			Method method = methods[methodIndex.intValue()];
			try {
				method.invoke(object, value);
			} catch (Exception ex) {
				// Propagate failure to load value
				throw new HttpParametersLoadException(ex);
			}
		}
	}

	/**
	 * Enum providing the escape state for translating.
	 */
	private static enum EscapeState {
		NONE, HIGH, LOW
	}

	/**
	 * Translates the parameter text.
	 *
	 * @param parameterText
	 *            Text to be translated.
	 * @param tempBuffer
	 *            {@link TempBuffer}.
	 * @return Translated text.
	 * @throws HttpParametersLoadException
	 *             If fails to translate.
	 */
	private String translate(String parameterText, TempBuffer tempBuffer)
			throws HttpParametersLoadException {

		// Obtain the temporary buffer
		char[] buffer = tempBuffer.buffer;

		// Ensure temporary buffer large enough
		if ((buffer == null) || (buffer.length < parameterText.length())) {
			// Increase buffer size (translation should not be bigger)
			buffer = new char[parameterText.length()];

			// Make available for further translations
			tempBuffer.buffer = buffer;
		}

		// Iterate over parameter text translating
		int charIndex = 0;
		EscapeState escape = EscapeState.NONE;
		byte highBits = 0;
		for (int i = 0; i < parameterText.length(); i++) {
			char character = parameterText.charAt(i);

			// Handle on whether escaping
			switch (escape) {
			case NONE:
				// Not escaped so handle character
				switch (character) {
				case '+':
					// Translate to space
					buffer[charIndex++] = ' ';
					break;

				case '%':
					// Escaping
					escape = EscapeState.HIGH;
					break;

				default:
					// No translation needed of character
					buffer[charIndex++] = character;
					break;
				}
				break;

			case HIGH:
				// Obtain the high bits for escaping
				highBits = this.translateEscapedCharToBits(character);
				escape = EscapeState.LOW;
				break;

			case LOW:
				// Have low bits, so obtain escaped character
				byte lowBits = this.translateEscapedCharToBits(character);
				character = (char) ((highBits << 4) | lowBits);

				// Load the character and no longer escaped
				buffer[charIndex++] = character;
				escape = EscapeState.NONE;
				break;
			}
		}

		// Should always be in non-escape state after translating
		if (escape != EscapeState.NONE) {
			throw new HttpParametersLoadException(
					"Invalid parameter text as escaping not complete: '"
							+ parameterText + "'");
		}

		// Return the translated text
		return new String(buffer, 0, charIndex);
	}

	/**
	 * Translates the character to the 4 bits as per escaping of HTTP.
	 *
	 * @param character
	 *            Character to translate.
	 * @return Corresponding 4 bits for character.
	 * @throws HttpParametersLoadException
	 *             If invalid character for escaping.
	 */
	private byte translateEscapedCharToBits(char character)
			throws HttpParametersLoadException {

		// Obtain the bits for the character
		int bits;
		if (('0' <= character) && (character <= '9')) {
			bits = character - '0';
		} else if (('A' <= character) && (character <= 'F')) {
			bits = (character - 'A') + 0xA;
		} else if (('a' <= character) && (character <= 'f')) {
			bits = (character - 'a') + 0xA;
		} else {
			// Invalid character for escaping
			throw new HttpParametersLoadException(
					"Invalid character for escaping: " + character);
		}

		// Return the bits
		return (byte) bits;
	}

	/**
	 * Obtains the parameter name for comparison.
	 *
	 * @param rawParameterName
	 *            Raw parameter name.
	 * @return Parameter name.
	 */
	private String getComparisonParameterName(String rawParameterName) {
		return (this.isCaseSensitive ? rawParameterName : rawParameterName
				.toLowerCase());
	}

	/*
	 * ==================== HttpParametersLoader =========================
	 */

	@Override
	public void init(Class<T> type, Map<String, String> aliasMappings,
			boolean isCaseSensitive) throws Exception {
		this.type = type;

		// Provide empty alias mappings if null
		if (aliasMappings == null) {
			aliasMappings = Collections.emptyMap();
		}

		// Extract the listing of method and parameter names
		List<String> methodNames = new LinkedList<String>();
		List<String> parameterNames = new LinkedList<String>();
		for (Method method : this.type.getMethods()) {

			// Ensure a public void method
			if (!Modifier.isPublic(method.getModifiers())) {
				continue;
			}
			if ((method.getReturnType() != null)
					&& (method.getReturnType() != Void.TYPE)) {
				continue;
			}

			// Ensure has only a String parameter
			Class<?>[] parameterTypes = method.getParameterTypes();
			if (parameterTypes.length != 1) {
				continue;
			}
			Class<?> parameterType = parameterTypes[0];
			if (!String.class.isAssignableFrom(parameterType)) {
				continue;
			}

			// Ensure the method begins with 'set'
			String methodName = method.getName();
			final String SETTER_PREFIX = "set";
			if (!methodName.startsWith(SETTER_PREFIX)) {
				continue;
			}

			// Ensure there is a parameter name
			String parameterName = methodName.substring(SETTER_PREFIX.length());
			if ((parameterName == null) || (parameterName.length() == 0)) {
				continue;
			}

			// Register the method and parameter names
			methodNames.add(methodName);
			parameterNames.add(parameterName);
		}

		// All methods extracted
		this.methodNames = methodNames.toArray(new String[0]);

		// Create the mapping of parameter to method index
		this.parameterNameToMethodIndex = new HashMap<String, Integer>(
				parameterNames.size() + aliasMappings.size());
		for (int i = 0; i < parameterNames.size(); i++) {
			String parameterName = parameterNames.get(i);

			// Parameters and method indexes align
			parameterName = this.getComparisonParameterName(parameterName);
			this.parameterNameToMethodIndex.put(parameterName, new Integer(i));
		}

		// Load any aliases
		for (String alias : aliasMappings.keySet()) {

			// Obtain the parameter name for alias
			String parameterName = aliasMappings.get(alias);
			parameterName = this.getComparisonParameterName(parameterName);

			// Obtain the method index for the alias
			Integer methodIndex = this.parameterNameToMethodIndex
					.get(parameterName);
			if (methodIndex == null) {
				// Unknown parameter for alias
				throw new Exception("Parameter '" + parameterName
						+ "' for alias '" + alias
						+ "' can not be found on type " + this.type.getName());
			}

			// Register the alias
			alias = this.getComparisonParameterName(alias);
			this.parameterNameToMethodIndex.put(alias, methodIndex);
		}
	}

	@Override
	public <O extends T> void loadParameters(HttpRequest httpRequest, O object)
			throws HttpParametersLoadException {

		// Obtain the load methods for the object
		Method[] methods = this.getMethods(object.getClass());

		// Create the temporary buffer (aids reducing object creation)
		TempBuffer tempBuffer = new TempBuffer();

		// Always load the parameters from the request URI
		String requestUri = httpRequest.getRequestURI();
		this.loadParameters(object, requestUri, methods, tempBuffer);

		// Only load parameters of body if a POST
		if ("POST".equalsIgnoreCase(httpRequest.getMethod())) {

			// Obtain the content encoding of the body
			// TODO handle content encoding

			// Obtain the content type of the body
			// TODO handle content type
			Charset charset = Charset.forName("UTF-8"); // default for now

			// Obtain the body data
			InputBufferStream body = httpRequest.getBody();
			int bodySize = (int) body.available();
			byte[] data = new byte[bodySize < 0 ? 0 : bodySize];
			try {
				InputStream browseStream = body.getBrowseStream();
				int index = 0;
				for (int value = browseStream.read(); value != BufferStream.END_OF_STREAM; value = browseStream
						.read()) {
					data[index++] = (byte) value;
				}
			} catch (IOException ex) {
				// Propagate failure
				throw new HttpParametersLoadException(ex);
			}

			// Obtain the body data as string
			String bodyText = new String(data, charset);

			// Load the parameters from the body
			this.loadParameters(object, bodyText, methods, tempBuffer);
		}
	}

	/**
	 * Temporary buffer.
	 */
	private static class TempBuffer {

		/**
		 * Buffer.
		 */
		public char[] buffer = null;
	}

}