/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.model.generate;

import java.io.File;

import net.officefloor.model.generate.model.ModelMetaData;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.impl.repository.filesystem.FileSystemConfigurationItem;

/**
 * Provides generic meta-data for generating a Model.
 * 
 * @author Daniel Sagenschneider
 */
public class GraphNodeMetaData {

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
		 * @return Transform token.
		 */
		String transform(String token);
	}

	/**
	 * Extension for a {@link ModelMetaData} file.
	 */
	private static final String MODEL_EXTENSION = ".model.xml";

	/**
	 * {@link ModelRepositoryImpl}.
	 */
	private static final ModelRepositoryImpl repository = new ModelRepositoryImpl();

	/**
	 * License text.
	 */
	private final String license;

	/**
	 * Root directory containing the raw models.
	 */
	private final File rawRootDir;

	/**
	 * Relative path for this {@link GraphNodeMetaData}.
	 */
	private final String relativePath;

	/**
	 * Top level constructor.
	 * 
	 * @param license
	 *            License.
	 * @param rawRootDir
	 *            Root directory containing the raw models.
	 */
	public GraphNodeMetaData(String license, File rawRootDir) {
		this.license = license;
		this.rawRootDir = rawRootDir;
		this.relativePath = "."; // current directory
	}

	/**
	 * Child constructor for relative path.
	 * 
	 * @param parent
	 *            Parent {@link GraphNodeMetaData}.
	 * @param childPath
	 *            Path for this child.
	 */
	public GraphNodeMetaData(GraphNodeMetaData parent, String childPath) {
		this.license = parent.license;
		this.rawRootDir = parent.rawRootDir;
		this.relativePath = (parent.relativePath.equals(".") ? childPath
				: parent.relativePath + "/" + childPath);
	}

	/**
	 * Obtains the license text.
	 * 
	 * @return License text.
	 */
	public String getLicense() {
		return this.license;
	}

	/**
	 * Obtains the package name for this {@link GraphNodeMetaData} instance.
	 * 
	 * @return Package name for this {@link GraphNodeMetaData} instance.
	 */
	public String getPackageName() {
		if (this.relativePath.equals(".")) {
			// No package
			return "";
		} else {
			// Translate relative path to package
			return this.relativePath.replace('/', '.');
		}
	}

	/**
	 * Obtains the {@link ModelMetaData} for the input type.
	 * 
	 * @param typeName
	 *            Name of the type.
	 * @return {@link ModelMetaData} for the type or <code>null</code> if not
	 *         model generation type.
	 */
	public ModelMetaData getModelMetaData(String typeName) throws Exception {

		// Ensure a model type
		if (!typeName.endsWith("Model")) {
			return null;
		}

		// Transform type name to its file name
		typeName = typeName.replace('.', '/');
		typeName = typeName.substring(0, typeName.length() - "Model".length());
		typeName += MODEL_EXTENSION;

		// Obtain the file containing the type's configuration
		File modelFile;
		if (typeName.contains("/")) {
			// Fully qualified type
			modelFile = new File(this.rawRootDir, typeName);
		} else {
			// Within current package
			modelFile = new File(new File(this.rawRootDir, this.relativePath),
					typeName);
		}

		// Ensure the model file exists
		if (!modelFile.exists()) {
			return null;
		}

		// Obtain the model from file
		return this.getModelMetaData(modelFile);
	}

	/**
	 * Obtains the {@link ModelMetaData} from the input file.
	 * 
	 * @param modelFile
	 *            {@link File} containing the configuration of the
	 *            {@link ModelMetaData}.
	 * @return {@link ModelMetaData} or <code>null</code> if file is not a
	 *         {@link ModelMetaData} configuration file.
	 */
	public ModelMetaData getModelMetaData(File modelFile) throws Exception {

		// Ensure is a model configuration file
		if (!modelFile.getName().endsWith(MODEL_EXTENSION)) {
			return null;
		}

		// Obtain the name
		String name = modelFile.getName();
		name = name.replace(MODEL_EXTENSION, "");

		// Unmarshal the model
		ModelMetaData model = repository.retrieve(new ModelMetaData(),
				new FileSystemConfigurationItem(modelFile, null));
		model.setName(name);
		model.setPackageName(this.getPackageName());

		// Return the model
		return model;
	}

}
