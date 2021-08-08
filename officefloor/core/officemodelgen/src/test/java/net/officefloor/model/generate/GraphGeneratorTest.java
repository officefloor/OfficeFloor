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
import java.io.StringReader;

import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link GraphGenerator}.
 * 
 * @author Daniel Sagenschneider
 */
public class GraphGeneratorTest extends OfficeFrameTestCase {

	/**
	 * Tests the generation of the graph of the model.
	 */
	public void testGraphGeneration() throws Exception {

		// Create the graph generator to test
		GraphGenerator graphGenerator = new GraphGenerator();

		// Obtain output directory under temporary directory
		File tempDir = new File(System.getProperty("java.io.tmpdir"));
		File outputDir = new File(tempDir, "officefloor-graph-generation");
		if (!outputDir.exists()) {
			outputDir.mkdir();
		}

		// Obtain the root directory for finding models
		File rawRootDir = this.findFile("root");

		// Generate the graph
		graphGenerator.generate(rawRootDir, outputDir);

		// Obtain directory containing the generated java files
		File javaFileDir = new File(outputDir, "graph/test");

		// Validate the generation correct
		this.assertContents(javaFileDir, "OneModel.java", "Graph_OneExpectedContent.txt");
		this.assertContents(javaFileDir, "TwoModel.java", "Graph_TwoExpectedContent.txt");
		this.assertContents(javaFileDir, "ConnectionModel.java", "Graph_ConnectionExpectedContent.txt");
		this.assertContents(javaFileDir, "CascadeModel.java", "Graph_CascadeExpectedContent.txt");
	}

	/**
	 * Asserts the contents of generation correct.
	 * 
	 * @param javaFileDir             Directory containing the generated java files.
	 * @param javaFileName            Name of the generated java file.
	 * @param expectedContentFileName Name of the file containing the expected
	 *                                content.
	 */
	private void assertContents(File javaFileDir, String javaFileName, String expectedContentFileName)
			throws Exception {

		// Obtain the contents of the generated java file
		String javaFileContents = this.getFileContents(new File(javaFileDir, javaFileName));

		// Obtain the expected file contents
		String expectedContents = this.getFileContents(this.findFile(this.getClass(), expectedContentFileName));
		expectedContents = expectedContents.replace("${GeneratedClassName}",
				GeneratedAnnotationJavaFacet.getGeneratedClassName());

		// Ensure contents match
		assertContents(new StringReader(expectedContents), new StringReader(javaFileContents));
	}

}
