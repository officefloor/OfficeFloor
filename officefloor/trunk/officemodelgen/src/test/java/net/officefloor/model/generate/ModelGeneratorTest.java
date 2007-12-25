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
import net.officefloor.repository.ConfigurationContext;
import net.officefloor.repository.ConfigurationItem;

/**
 * Tests the {@link net.officefloor.model.generate.ModelGenerator}.
 * 
 * @author Daniel
 */
public class ModelGeneratorTest extends OfficeFrameTestCase {

	/**
	 * Ensures creates the model.
	 */
	public void testModelGeneration() throws Exception {

		// Create the generic model
		GraphNodeMetaData general = new GraphNodeMetaData("License", null);

		// Create the model meta-data
		ModelMetaData metaData = new ModelMetaData("Class", "net.officefloor",
				"", new String[] { "net.officefloor.test.SpecialType" },
				new FieldMetaData[] {
						new FieldMetaData("field one", "int", "Field one.",
								null, null),
						new FieldMetaData("field two", "String", null, null,
								null) }, new ListMetaData[] {
						new ListMetaData("list one", "Integer", "List one."),
						new ListMetaData("list two", "SpecialType", null) });

		// Validate conversions
		assertEquals("Incorrect class", "ClassModel", metaData.getClassName());
		assertEquals("Incorrect events", "ClassEvent", metaData.getEventName());

		// Generate the model
		MockConfigurationContext context = new MockConfigurationContext();
		ModelGenerator generator = new ModelGenerator(metaData, general);
		ConfigurationItem item = generator.generateModel(context);

		// Validate file name
		assertEquals("Incorrect file name", "net/officefloor/ClassModel.java",
				item.getId());
		
		// Validate content
		String content = this.getFileContents(this.findFile(this.getClass(),
				"Model_ModelExpectedContent.txt"));
		assertContents(new StringReader(content), new StringReader(
				context.modelText));
	}

	/**
	 * Ensures creates the connection.
	 */
	public void testConnectionGeneration() throws Exception {

		// Create the generic model
		GraphNodeMetaData general = new GraphNodeMetaData("License", null);

		// Create the model meta-data
		ModelMetaData metaData = new ModelMetaData("Class", "net.officefloor",
				"", new String[] {}, new FieldMetaData[] {
						new FieldMetaData("field one", "String",
								"Test field one.", "link one", null),
						new FieldMetaData("field two", "String",
								"Test field two.", null, "link two") },
				new ListMetaData[] {});

		// Generate the model
		MockConfigurationContext context = new MockConfigurationContext();
		ModelGenerator generator = new ModelGenerator(metaData, general);
		ConfigurationItem item = generator.generateModel(context);

		// Validate file name
		assertEquals("Incorrect file name", "net/officefloor/ClassModel.java",
				item.getId());

		// Validate content
		String content = this.getFileContents(this.findFile(this.getClass(),
				"Model_ConnectionExpectedContent.txt"));
		BufferedReader actual = new BufferedReader(new StringReader(content));
		BufferedReader expected = new BufferedReader(new StringReader(
				context.modelText));
		String actualLine, expectedLine;
		int lineNumber = 1;
		while ((actualLine = actual.readLine()) != null) {
			expectedLine = expected.readLine();
			assertEquals("Incorrect line " + lineNumber, actualLine,
					expectedLine);
			lineNumber++;
		}
	}

}

class MockConfigurationContext implements ConfigurationContext,
		ConfigurationItem {

	/**
	 * Id of the model.
	 */
	protected String id;

	/**
	 * Text of the model.
	 */
	protected String modelText;

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.model.repository.ConfigurationContext#getId()
	 */
	public String getId() {
		return this.id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.model.repository.ConfigurationContext#getClasspath()
	 */
	public String[] getClasspath() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO implement");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.model.repository.ConfigurationContext#getConfigurationItem(java.lang.String)
	 */
	public ConfigurationItem getConfigurationItem(String id) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO implement");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.model.repository.ConfigurationContext#createConfigurationItem(java.lang.String,
	 *      java.io.InputStream)
	 */
	public ConfigurationItem createConfigurationItem(String id,
			InputStream configuration) throws Exception {

		// Record the id
		this.id = id;

		// Record the text of configuration
		StringWriter writer = new StringWriter();
		Reader reader = new InputStreamReader(configuration);
		int data;
		while ((data = reader.read()) != -1) {
			writer.write(data);
		}
		this.modelText = writer.toString();

		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.model.repository.ConfigurationItem#getConfiguration()
	 */
	public InputStream getConfiguration() throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO implement");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.model.repository.ConfigurationItem#setConfiguration(java.io.InputStream)
	 */
	public void setConfiguration(InputStream configuration) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO implement");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.model.repository.ConfigurationItem#getContext()
	 */
	public ConfigurationContext getContext() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO implement");
	}

}