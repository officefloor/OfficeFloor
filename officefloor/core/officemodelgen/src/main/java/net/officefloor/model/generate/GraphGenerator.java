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

import net.officefloor.model.generate.model.ModelMetaData;

/**
 * Generates all the models.
 *
 * @author Daniel Sagenschneider
 */
public class GraphGenerator {

	/**
	 * Generates the models from the configuration in the raw directory and puts
	 * them in the output directory.
	 * 
	 * @param rawDir
	 *            Raw directory containing the model files.
	 * @param outputDir
	 *            Directory to write the resulting models.
	 * @throws Exception
	 *             If fails to generate the models.
	 */
	public void generate(File rawDir, File outputDir) throws Exception {

		// Create the top level graph meta-data
		GraphNodeMetaData graphMetaData = new GraphNodeMetaData("", rawDir);

		// Clear the output directory
		if (outputDir.exists()) {
			// Ensure is a directory
			if (!outputDir.isDirectory()) {
				throw new IllegalArgumentException("Output '"
						+ outputDir.getCanonicalPath()
						+ "' must be a directory");
			}

			// Clear the directory
			for (File child : outputDir.listFiles()) {
				this.deleteFile(child);
			}
		}

		// Generate the objects
		ModelContext context = new FileSystemModelContext(outputDir);
		this.generate(rawDir, context, graphMetaData);
	}

	/**
	 * Generates the models by recursing over files.
	 */
	private void generate(File rawDir, ModelContext context,
			GraphNodeMetaData graphMetaData) throws Exception {

		// Recurse generating the objects
		for (File child : rawDir.listFiles()) {
			if (child.isDirectory()) {

				// Create the generic model for child directory
				GraphNodeMetaData childGraphMetaData = new GraphNodeMetaData(
						graphMetaData, child.getName());

				// Recursively generate for sub directories
				this.generate(child, context, childGraphMetaData);
			} else {
				// File therefore generate the model
				this.generateModel(child, context, graphMetaData);
			}
		}
	}

	/**
	 * Generates the Model.
	 */
	private void generateModel(File configurationFile, ModelContext context,
			GraphNodeMetaData graphMetaData) throws Exception {

		// Obtain the model meta-data
		ModelMetaData model = graphMetaData.getModelMetaData(configurationFile);
		if (model == null) {
			return; // not a model
		}

		// Generate the model
		new ModelGenerator(model, graphMetaData).generateModel(context);
	}

	/**
	 * Clears the input directory.
	 *
	 * @param dir
	 *            Directory to clear.
	 */
	private void deleteFile(File file) throws Exception {

		// Ignore special files (starting with a '.')
		if (file.getName().startsWith(".")) {
			return;
		}

		// Clear the if directory
		if (file.isDirectory()) {
			for (File child : file.listFiles()) {
				deleteFile(child);
			}
		}

		// Remove the file
		file.delete();
	}

}
