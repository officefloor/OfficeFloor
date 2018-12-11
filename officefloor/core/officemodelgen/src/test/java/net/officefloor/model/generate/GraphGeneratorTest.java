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
