/*-
 * #%L
 * OfficeXml
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.plugin.xml.unmarshall.flat;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.xml.unmarshall.load.ValueLoaderFactory;
import net.officefloor.plugin.xml.unmarshall.translate.TranslatorRegistry;

/**
 * Tests the
 * {@link net.officefloor.plugin.xml.unmarshall.flat.FlatXmlUnmarshaller}.
 * 
 * @author Daniel Sagenschneider
 */
public class FlatXmlUnmarshallerTest extends OfficeFrameTestCase {

	/**
	 * Name of the file containing the XML to be loaded onto the target object.
	 */
	protected static final String XML_FILE_NAME = "InputFile.xml";

	/**
	 * Name of the file containing the XML with null values to be loaded onto
	 * the target object.
	 */
	protected static final String NULL_XML_FILE_NAME = "NullInputFile.xml";

	/**
	 * XML unmarshaller to be tested.
	 */
	protected FlatXmlUnmarshaller xmlUnmarshaller;

	/**
	 * Object to have the XML loaded onto.
	 */
	protected TargetObject targetObject;

	/**
	 * Setup.
	 */
	protected void setUp() throws Exception {
		super.setUp();

		// Create the target object
		this.targetObject = new TargetObject();

		// Create the value loader factory
		ValueLoaderFactory valueLoaderFactory = new ValueLoaderFactory(
				new TranslatorRegistry(), this.targetObject.getClass());

		// Create the meta-data
		FlatXmlUnmarshallerMetaData metaData = new FlatXmlUnmarshallerMetaData(
				valueLoaderFactory, new XmlMapping[] {
						new XmlMapping("string-value", "setString"),
						new XmlMapping("boolean-true", "setBoolean"),
						new XmlMapping("boolean-false", "setBooleanObject"),
						new XmlMapping("char-value", "setChar"),
						new XmlMapping("charactor", "setCharacter"),
						new XmlMapping("byte-value", "setByte"),
						new XmlMapping("Byte", "setByteObject"),
						new XmlMapping("int-value", "setInt"),
						new XmlMapping("Integer", "setInteger"),
						new XmlMapping("long-value", "setLong"),
						new XmlMapping("Long", "setLongObject"),
						new XmlMapping("float-value", "setFloat"),
						new XmlMapping("Float", "setFloatObject"),
						new XmlMapping("double-value", "setDouble"),
						new XmlMapping("Double", "setDoubleObject"),
						new XmlMapping("Date", "setDate") });

		// Create the XML unmarshaller
		this.xmlUnmarshaller = new FlatXmlUnmarshaller(metaData);
	}

	/**
	 * Ensures able to load xml values.
	 */
	public void testLoadElementValues() throws Exception {

		// Obtain input to XML
		File xmlFile = this.findFile(this.getClass(), XML_FILE_NAME);

		// Load the xml onto the target object
		this.xmlUnmarshaller.unmarshall(new FileInputStream(xmlFile),
				this.targetObject);

		// Validate target object loaded appropriately
		validateTargetObject(this.targetObject);
	}

	/**
	 * Ensures able to appropriately load null XML values.
	 */
	public void testLoadNullValues() throws Exception {

		// Obtain input to XML
		File xmlFile = this.findFile(this.getClass(), NULL_XML_FILE_NAME);

		// Load the xml onto the target object
		this.xmlUnmarshaller.unmarshall(new FileInputStream(xmlFile),
				this.targetObject);

		// Validate target object loaded appropriately
		validateTargetObjectNulls(this.targetObject);

	}

	/**
	 * Validates the target object is appropriately intialised from the Input
	 * file.
	 * 
	 * @param targetObject
	 *            Target object.
	 */
	@SuppressWarnings("deprecation")
	static void validateTargetObject(TargetObject targetObject) {
		// Validate XML values loaded onto target object
		assertEquals("Incorrect string.", "A string value", targetObject
				.getString());
		assertEquals("Incorrect boolean true", true, targetObject.getBoolean());
		assertEquals("Incorrect boolean object false", Boolean.FALSE,
				targetObject.getBooleanObject());
		assertEquals("Incorrect char value", 'A', targetObject.getChar());
		assertEquals("Incorrect Charactor object", new Character('&'),
				targetObject.getCharacter());
		assertEquals("Incorrect byte value", (byte) 1, targetObject.getByte());
		assertEquals("Incorrect Byte object", new Byte("0"), targetObject
				.getByteObject());
		assertEquals("Incorrect int value", 1, targetObject.getInt());
		assertEquals("Incorrect Integer object", new Integer(-1), targetObject
				.getInteger());
		assertEquals("Incorrect long value", 2, targetObject.getLong());
		assertEquals("Incorrect Long object", new Long(-2), targetObject
				.getLongObject());
		assertEquals("Incorrect float value", 3.0, targetObject.getFloat(),
				0.0001);
		assertEquals("Incorrect Float object", new Float(-3.0), targetObject
				.getFloatObject());
		assertEquals("Incorrect double value", 4.0, targetObject.getDouble(),
				0.0001);
		assertEquals("Incorrect Double object", new Double(-4.0), targetObject
				.getDoubleObject());
		assertEquals("Incorrect Date", new Date("5 Jan 2006"), targetObject
				.getDate());
	}

	/**
	 * Validates appropriate null values loaded to target object.
	 * 
	 * @param targetObject
	 *            Target object to check null values.
	 */
	static void validateTargetObjectNulls(TargetObject targetObject) {
		// Validate null values loaded onto target object
		assertEquals("Incorrect string.", null, targetObject.getString());
		assertEquals("Incorrect boolean true", false, targetObject.getBoolean());
		assertEquals("Incorrect boolean object false", null, targetObject
				.getBooleanObject());
		assertEquals("Incorrect char value", ' ', targetObject.getChar());
		assertEquals("Incorrect Charactor object", null, targetObject
				.getCharacter());
		assertEquals("Incorrect byte value", (byte) 0, targetObject.getByte());
		assertEquals("Incorrect Byte object", null, targetObject
				.getByteObject());
		assertEquals("Incorrect int value", 0, targetObject.getInt());
		assertEquals("Incorrect Integer object", null, targetObject
				.getInteger());
		assertEquals("Incorrect long value", 0, targetObject.getLong());
		assertEquals("Incorrect Long object", null, targetObject
				.getLongObject());
		assertEquals("Incorrect float value", 0.0, targetObject.getFloat(),
				0.0001);
		assertEquals("Incorrect Float object", null, targetObject
				.getFloatObject());
		assertEquals("Incorrect double value", 0.0, targetObject.getDouble(),
				0.0001);
		assertEquals("Incorrect Double object", null, targetObject
				.getDoubleObject());
		assertEquals("Incorrect Date", null, targetObject.getDate());
	}
}
