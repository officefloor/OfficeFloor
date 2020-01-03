package net.officefloor.model.generate.model;

import java.io.InputStream;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.generate.GraphNodeMetaData;
import net.officefloor.plugin.xml.XmlUnmarshaller;

/**
 * Ensures able to unmarshall {@link ModelMetaData}.
 * 
 * @author Daniel Sagenschneider
 */
public class ModelMetaDataUnmarshallTest extends OfficeFrameTestCase {

	/**
	 * Unmarshall {@link ModelMetaData}.
	 */
	public void testUnmarshallModelMetaData() throws Exception {

		// Obtain the configuration
		final InputStream input = this.findInputStream(this.getClass(),
				"Test.model.xml");

		// Unmarshal the model
		XmlUnmarshaller unmarshaller = GraphNodeMetaData
				.getModelMetaDataXmlUnmarshaller();
		ModelMetaData model = new ModelMetaData();
		unmarshaller.unmarshall(input, model);

		// Validate the model
		assertEquals("Incorrect name", "test", model.getName());
		assertEquals("Incorrect package", "net.officefloor.test",
				model.getPackageName());
		assertEquals("Incorrect import one", "net.officefloor.test.ImportOne",
				model.getImportClasses().get(0));
		assertEquals("Incorrect import two", "net.officefloor.test.ImportTwo",
				model.getImportClasses().get(1));
		assertEquals("Incorrect interface one",
				"net.officefloor.test.InterfaceOne",
				model.getInterfaces().get(0));
		assertEquals("Incorrect interface two",
				"net.officefloor.test.InterfaceTwo",
				model.getInterfaces().get(1));
		FieldMetaData field = model.getFields().get(0);
		assertEquals("Incorrect field name", "Field One", field.getName());
		assertEquals("Incorrect field type", "String", field.getType());
		assertTrue("Incorrect field cascade", field.isCascadeRemove());
		assertEquals("Incorrect field description", "Test field",
				field.getDescription());
		assertEquals("Incorrect field end-field", "Link One",
				field.getEndField());
		assertEquals("Incorrect field end-list", "Link Two", field.getEndList());
		ListMetaData list = model.getLists().get(0);
		assertEquals("Incorrect list name", "List One", list.getName());
		assertEquals("Incorrect list type", "ImportOne", list.getType());
		assertTrue("Incorrect list cascade", list.isCascadeRemove());
		assertEquals("Incorrect list description", "Test list",
				list.getDescription());
	}

}