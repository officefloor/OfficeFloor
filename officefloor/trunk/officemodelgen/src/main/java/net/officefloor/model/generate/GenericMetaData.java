/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.model.generate;

/**
 * Provides generic meta-data for generating a Model.
 * 
 * @author Daniel
 */
public class GenericMetaData {

	/**
	 * Returns the input text capitalised.
	 * 
	 * @param text
	 *            Text.
	 * @return Capitalised text.
	 */
	public static String capitalise(String text) {
		return transform(text, "\\s+", new TransformToken() {
			public String transform(String token) {
				return token.toUpperCase();
			}
		}, "_");
	}

	/**
	 * Returns the input text in camel case.
	 * 
	 * @return Camel case text.
	 */
	public static String camelCase(String text) {
		return transform(text, "\\s+", new TransformToken() {
			public String transform(String token) {
				return (token.substring(0, 1).toUpperCase())
						+ (token.substring(1).toLowerCase());
			}
		}, "");
	}

	/**
	 * Returns the input text in case ready for properties.
	 * 
	 * @param text
	 *            Text.
	 * @return Property text.
	 */
	public static String propertyCase(String text) {
		String camelCase = camelCase(text);
		return (camelCase.substring(0, 1).toLowerCase())
				+ (camelCase.substring(1));
	}

	/**
	 * Returns the input text in sentence case.
	 * 
	 * @param text
	 *            Text.
	 * @return Sentence case text.
	 */
	public static String titleCase(String text) {
		return (text.substring(0, 1).toUpperCase() + text.substring(1)
				.toLowerCase());
	}

	/**
	 * Transforms the text.
	 * 
	 * @param text
	 *            Text to transform.
	 * @param splitReg
	 *            Regular expression to split to tokens.
	 * @param transform
	 *            Transforms the tokens.
	 * @param joinText
	 *            Text to join tokens.
	 * @return Transformed text.
	 */
	static String transform(String text, String splitReg,
			TransformToken transform, String joinText) {

		// Empty string if null
		if (text == null) {
			return "";
		}

		// Obtain the tokens
		String[] tokens = text.split(splitReg);

		// Transform the tokens
		for (int i = 0; i < tokens.length; i++) {
			tokens[i] = transform.transform(tokens[i]);
		}

		// Join tokens back together
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < tokens.length; i++) {
			builder.append(tokens[i]);
			if (i != (tokens.length - 1)) {
				builder.append(joinText);
			}
		}

		// Return the result
		return builder.toString();
	}

	/**
	 * Transforms a token.
	 */
	private static interface TransformToken {

		/**
		 * Transforms the token.
		 * 
		 * @param token
		 *            Token to be transformed.
		 * @return Transforme token.
		 */
		String transform(String token);
	}

	/**
	 * Default constructor.
	 */
	public GenericMetaData() {
	}

	/**
	 * Convenience constructor.
	 * 
	 * @param license
	 *            License.
	 */
	public GenericMetaData(String license) {
		this.license = license;
	}

	/**
	 * License text.
	 */
	private String license;

	public String getLicense() {
		return this.license;
	}

	public void setLicense(String license) {
		this.license = license;
	}

}
