/*-
 * #%L
 * Model Generator
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

package net.officefloor.model.generate;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import net.officefloor.model.generate.model.ModelMetaData;
import net.officefloor.plugin.xml.XmlUnmarshaller;
import net.officefloor.plugin.xml.unmarshall.tree.TreeXmlUnmarshallerFactory;

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
	 * @param text
	 *            Text to be transformed to camel case.
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
	 * {@link XmlUnmarshaller} of the {@link ModelMetaData}.
	 */
	private static XmlUnmarshaller modelMetaDataXmlUnmarshaller;

	/**
	 * Lazily obtains the {@link XmlUnmarshaller} for the {@link ModelMetaData}.
	 *
	 * @return {@link XmlUnmarshaller} for the Model.
	 * @throws Exception
	 *             If fails to obtain the {@link XmlUnmarshaller}.
	 */
	public static XmlUnmarshaller getModelMetaDataXmlUnmarshaller()
			throws Exception {

		// Lazily create the model meta data unmarshaller
		if (modelMetaDataXmlUnmarshaller == null) {

			// Obtain the model meta data class
			Class<ModelMetaData> modelMetaDataClass = ModelMetaData.class;

			// Obtain input stream to configuration of unmarshaller
			InputStream unmarshallConfig = modelMetaDataClass
					.getResourceAsStream("UnmarshallConfiguration.xml");
			if (unmarshallConfig == null) {
				throw new IllegalStateException(
						"Unable to configure retrieving the model type "
								+ modelMetaDataClass.getName());
			}

			// Create the unmarshaller
			modelMetaDataXmlUnmarshaller = TreeXmlUnmarshallerFactory
					.getInstance().createUnmarshaller(unmarshallConfig);
		}

		// Return the unmarshaller
		return modelMetaDataXmlUnmarshaller;
	}

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
	 * @throws Exception
	 *             If fails to obtain the {@link ModelMetaData}.
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
	 * @throws Exception
	 *             If fails to obtain {@link ModelMetaData}.
	 */
	public ModelMetaData getModelMetaData(File modelFile) throws Exception {

		// Ensure is a model configuration file
		if (!modelFile.getName().endsWith(MODEL_EXTENSION)) {
			return null;
		}

		// Obtain the name
		String name = modelFile.getName();
		name = name.replace(MODEL_EXTENSION, "");

		// Obtain access to the configuration of the model
		InputStream modelFileContents = new FileInputStream(modelFile);

		// Unmarshal the model
		ModelMetaData model = new ModelMetaData();
		getModelMetaDataXmlUnmarshaller().unmarshall(modelFileContents, model);
		model.setName(name);
		model.setPackageName(this.getPackageName());

		// Return the model
		return model;
	}

}
