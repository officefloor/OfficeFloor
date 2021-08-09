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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.generate.model.FieldMetaData;
import net.officefloor.model.generate.model.ListMetaData;
import net.officefloor.model.generate.model.ModelMetaData;

/**
 * Tests the {@link net.officefloor.model.generate.ModelGenerator}.
 * 
 * @author Daniel Sagenschneider
 */
public class ModelGeneratorTest extends OfficeFrameTestCase {

	/**
	 * Ensures creates the model.
	 */
	public void testModelGeneration() throws Exception {

		// Create the generic model
		GraphNodeMetaData general = new GraphNodeMetaData("License", null);

		// Create the model meta-data
		ModelMetaData metaData = new ModelMetaData("Class", "net.officefloor", "",
				new String[] { "net.officefloor.test.SpecialType" },
				new String[] { "net.officefloor.test.MarkerInterface" },
				new FieldMetaData[] { new FieldMetaData("field one", "int", "Field one.", null, null),
						new FieldMetaData("field two", "String", null, null, null) },
				new ListMetaData[] { new ListMetaData("list one", "Integer", "List one."),
						new ListMetaData("list two", "SpecialType", null) });

		// Validate conversions
		assertEquals("Incorrect class", "ClassModel", metaData.getClassName());
		assertEquals("Incorrect events", "ClassEvent", metaData.getEventName());

		// Generate the model
		MockModelContext context = new MockModelContext();
		ModelGenerator generator = new ModelGenerator(metaData, general);
		ModelFile modelFile = generator.generateModel(context);

		// Validate file name
		assertEquals("Incorrect file name", "net/officefloor/ClassModel.java", modelFile.getLocation());

		// Validate content
		String content = this.getExpectedFileContents("Model_ModelExpectedContent.txt");
		assertContents(new StringReader(content), new StringReader(context.modelText));
	}

	/**
	 * Ensures can create empty model (no fields or lists).
	 */
	public void testEmptyModelGeneration() throws Exception {

		// Create the generic model
		GraphNodeMetaData general = new GraphNodeMetaData("License", null);

		// Create the model meta-data
		ModelMetaData metaData = new ModelMetaData("Empty", "net.officefloor", "", new String[0], new String[0],
				new FieldMetaData[0], new ListMetaData[0]);

		// Validate conversions
		assertEquals("Incorrect class", "EmptyModel", metaData.getClassName());
		assertEquals("Incorrect events", "EmptyEvent", metaData.getEventName());

		// Generate the model
		MockModelContext context = new MockModelContext();
		ModelGenerator generator = new ModelGenerator(metaData, general);
		ModelFile modelFile = generator.generateModel(context);

		// Validate file name
		assertEquals("Incorrect file name", "net/officefloor/EmptyModel.java", modelFile.getLocation());

		// Validate content
		String content = this.getExpectedFileContents("Model_EmptyModelExpectedContent.txt");
		assertContents(new StringReader(content), new StringReader(context.modelText));
	}

	/**
	 * Ensures creates the connection.
	 */
	public void testConnectionGeneration() throws Exception {

		// Create the generic model
		GraphNodeMetaData general = new GraphNodeMetaData("License", null);

		// Create the model meta-data
		ModelMetaData metaData = new ModelMetaData("Class", "net.officefloor", "", new String[] {}, new String[] {},
				new FieldMetaData[] { new FieldMetaData("field one", "String", "Test field one.", "link one", null),
						new FieldMetaData("field two", "String", "Test field two.", null, "link two") },
				new ListMetaData[] {});

		// Generate the model
		MockModelContext context = new MockModelContext();
		ModelGenerator generator = new ModelGenerator(metaData, general);
		ModelFile modelFile = generator.generateModel(context);

		// Validate file name
		assertEquals("Incorrect file name", "net/officefloor/ClassModel.java", modelFile.getLocation());

		// Validate content
		String content = this.getExpectedFileContents("Model_ConnectionExpectedContent.txt");
		BufferedReader actual = new BufferedReader(new StringReader(content));
		BufferedReader expected = new BufferedReader(new StringReader(context.modelText));
		String actualLine, expectedLine;
		int lineNumber = 1;
		while ((actualLine = actual.readLine()) != null) {
			expectedLine = expected.readLine();
			assertEquals("Incorrect line " + lineNumber, actualLine, expectedLine);
			lineNumber++;
		}
	}

	/**
	 * Obtains the expected file content.
	 * 
	 * @param fileName Name of the expected file.
	 * @return Expected file content.
	 */
	private String getExpectedFileContents(String fileName) throws Exception {
		String content = this.getFileContents(this.findFile(this.getClass(), fileName));
		content = content.replace("${GeneratedClassName}", GeneratedAnnotationJavaFacet.getGeneratedClassName());
		return content;
	}

	/**
	 * Mock {@link ModelContext} implementation for testing.
	 */
	private class MockModelContext implements ModelContext, ModelFile {

		/**
		 * Location of the {@link ModelFile}.
		 */
		protected String location;

		/**
		 * Text of the model.
		 */
		protected String modelText;

		/*
		 * ================= ModelContext ============================
		 */

		@Override
		public ModelFile createModelFile(String location, InputStream contents) throws Exception {

			// Record the location
			this.location = location;

			// Record the text of configuration
			StringWriter writer = new StringWriter();
			Reader reader = new InputStreamReader(contents);
			int data;
			while ((data = reader.read()) != -1) {
				writer.write(data);
			}
			this.modelText = writer.toString();

			// Return this as the Model File
			return this;
		}

		/*
		 * ================= ModelFile ================================
		 */

		@Override
		public String getLocation() {
			return this.location;
		}
	}

}
