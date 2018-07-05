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
package net.officefloor.plugin.xml.unmarshall.flat;

import java.io.File;
import java.io.FileInputStream;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.plugin.xml.XmlUnmarshaller;

/**
 * Tests the {@link FlatXmlUnmarshallerManagedObjectSource}.
 *
 * @author Daniel Sagenschneider
 */
public class FlatXmlUnmarshallerManagedObjectSourceTest extends
		OfficeFrameTestCase {

	/**
	 * Name of the file containing the XML to be loaded onto the target object.
	 */
	protected static final String VALUED_XML_FILE_NAME = "InputFile.xml";

	/**
	 * Name of the file containing the XML of null values to be loaded onto the
	 * target object.
	 */
	protected static final String NULL_XML_FILE_NAME = "NullInputFile.xml";

	/**
	 * {@link FlatXmlUnmarshallerManagedObjectSource}to test.
	 */
	protected FlatXmlUnmarshallerManagedObjectSource resourceSource;

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

		// Initiate the loader
		ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();

		// Specify methods
		loader.addProperty("string-value", "setString");
		loader.addProperty("boolean-true", "setBoolean");
		loader.addProperty("boolean-false", "setBooleanObject");
		loader.addProperty("char-value", "setChar");
		loader.addProperty("charactor", "setCharacter");
		loader.addProperty("byte-value", "setByte");
		loader.addProperty("Byte", "setByteObject");
		loader.addProperty("int-value", "setInt");
		loader.addProperty("Integer", "setInteger");
		loader.addProperty("long-value", "setLong");
		loader.addProperty("Long", "setLongObject");
		loader.addProperty("float-value", "setFloat");
		loader.addProperty("Float", "setFloatObject");
		loader.addProperty("double-value", "setDouble");
		loader.addProperty("Double", "setDoubleObject");
		loader.addProperty("Date", "setDate");

		// Specify target object
		loader.addProperty(
				FlatXmlUnmarshallerManagedObjectSource.CLASS_PROPERTY_NAME,
				this.targetObject.getClass().getName());

		// Create the resource source
		this.resourceSource = loader
				.loadManagedObjectSource(FlatXmlUnmarshallerManagedObjectSource.class);
	}

	/**
	 * Ensure able to do a simple load.
	 */
	public void testSimpleLoad() throws Throwable {

		// Create the XML unmarshaller managed object
		ManagedObject managedObject = new ManagedObjectUserStandAlone()
				.sourceManagedObject(this.resourceSource);

		// Obtain the XML unmarshaller
		XmlUnmarshaller unmarshaller = (XmlUnmarshaller) managedObject
				.getObject();

		// Obtain input to XML
		File xmlFile = this.findFile(this.getClass(), VALUED_XML_FILE_NAME);

		// Unmarshall the XML onto the target object
		unmarshaller
				.unmarshall(new FileInputStream(xmlFile), this.targetObject);

		// Validate the target object has been loaded appropriately
		FlatXmlUnmarshallerTest.validateTargetObject(this.targetObject);
	}

	/**
	 * Ensure able to load null values correctly.
	 */
	public void testNullLoad() throws Throwable {

		// Create the XML unmarshaller managed object
		ManagedObject managedObject = new ManagedObjectUserStandAlone()
				.sourceManagedObject(this.resourceSource);

		// Obtain the XML unmarshaller
		XmlUnmarshaller unmarshaller = (XmlUnmarshaller) managedObject
				.getObject();

		// Obtain input to XML
		File xmlFile = this.findFile(this.getClass(), NULL_XML_FILE_NAME);

		// Unmarshall the XML onto the target object
		unmarshaller
				.unmarshall(new FileInputStream(xmlFile), this.targetObject);

		// Validate that appropriate null values loaded to target object
		FlatXmlUnmarshallerTest.validateTargetObjectNulls(this.targetObject);
	}

}