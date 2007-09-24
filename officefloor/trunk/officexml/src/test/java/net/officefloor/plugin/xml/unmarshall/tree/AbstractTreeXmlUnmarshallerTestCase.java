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
package net.officefloor.plugin.xml.unmarshall.tree;

import java.io.File;
import java.io.FileInputStream;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.xml.unmarshall.tree.objects.ComplexChild;
import net.officefloor.plugin.xml.unmarshall.tree.objects.ComplexParent;
import net.officefloor.plugin.xml.unmarshall.tree.objects.FirstObject;
import net.officefloor.plugin.xml.unmarshall.tree.objects.FourthObject;
import net.officefloor.plugin.xml.unmarshall.tree.objects.ManyChildren;
import net.officefloor.plugin.xml.unmarshall.tree.objects.RecursiveObject;
import net.officefloor.plugin.xml.unmarshall.tree.objects.SecondObject;
import net.officefloor.plugin.xml.unmarshall.tree.objects.ThirdObject;

/**
 * Tests the
 * {@link net.officefloor.plugin.xml.unmarshall.tree.TreeXmlUnmarshaller}.
 * 
 * @author Daniel
 */
public abstract class AbstractTreeXmlUnmarshallerTestCase extends
		OfficeFrameTestCase {

	/**
	 * Creates the {@link TreeXmlUnmarshaller} to test the loading non-recursive
	 * values.
	 * 
	 * @return {@link TreeXmlUnmarshaller}.
	 * @throws Exception
	 *             If fail to create the {@link TreeXmlUnmarshaller}.
	 */
	protected abstract TreeXmlUnmarshaller createNonRecursiveTreeXmlUnmarshaller()
			throws Exception;

	/**
	 * Creates the {@link TreeXmlUnmarshaller} to test the loading recursive
	 * values.
	 * 
	 * @return {@link TreeXmlUnmarshaller}.
	 * @throws Exception
	 *             If fail to create the {@link TreeXmlUnmarshaller}.
	 */
	protected abstract TreeXmlUnmarshaller createRecursiveTreeXmlUnmarshaller()
			throws Exception;

	/**
	 * Ensures able to load xml values (non-recursively).
	 */
	public void testLoadNonRecursiveValues() throws Exception {

		// Contains the xml
		final String XML_FILE_NAME = "NonRecursiveInputFile.xml";

		// Create the target object
		FirstObject targetObject = new FirstObject();

		// Create the Tree XML unmarshaller
		TreeXmlUnmarshaller xmlUnmarshaller = this
				.createNonRecursiveTreeXmlUnmarshaller();

		// Obtain input to XML
		File xmlFile = this.findFile(this.getClass(), XML_FILE_NAME);

		// Load the xml onto the target object
		xmlUnmarshaller.unmarshall(new FileInputStream(xmlFile), targetObject);

		// Validate target object loaded appropriately
		assertEquals("Incorrect info.", "first class value", targetObject
				.getInfo());
		SecondObject second = targetObject.getSecond();
		assertNotNull("Missing second object", second);
		assertEquals("Incorrect details.", "attribute value", second
				.getDetails());
		assertEquals("Incorrect value for second.", "second class value",
				second.getValue());
		ThirdObject third = second.getThird();
		assertNotNull("Missing third object", third);
		assertEquals("Incorrect value for third.", "third class value", third
				.getValue());
		FourthObject[] fourths = targetObject.getFourths();
		assertEquals("Invalid number of fourth objects", 2, fourths.length);
		assertEquals("Incorrect first 'fourth object' value",
				"forth class value", fourths[0].getValue());
		assertEquals("Incorrect second 'fourth object' value",
				"forth class repeated value", fourths[1].getValue());
	}

	/**
	 * Ensures able to load xml value recursively.
	 */
	public void testLoadRecursiveValues() throws Exception {

		// Contains the recursive xml.
		final String XML_FILE_NAME = "RecursiveInputFile.xml";

		// Create the target object
		final RecursiveObject targetObject = new RecursiveObject();

		// Create the XML unmarshaller
		TreeXmlUnmarshaller xmlUnmarshaller = this
				.createRecursiveTreeXmlUnmarshaller();

		// Obtain input to XML
		File xmlFile = this.findFile(this.getClass(), XML_FILE_NAME);

		// Load the xml onto the target object
		xmlUnmarshaller.unmarshall(new FileInputStream(xmlFile), targetObject);

		// Validate target object loaded appropriately

		// Validate person
		assertEquals("Director not loaded", "Director", targetObject
				.getPerson().getPosition());
		assertEquals("Manager not loaded", "Manager", targetObject.getPerson()
				.getPerson().getPosition());
		assertEquals("Worker not loaded", "Worker", targetObject.getPerson()
				.getPerson().getPerson().getPosition());
		assertEquals("Contract worker not loaded", "ContractWorker",
				targetObject.getPerson().getPerson().getPerson().getPerson()
						.getPosition());

		// Validate complex parent/child
		// First
		ComplexParent parent = targetObject.getComplexParent();
		ComplexChild child = parent.getComplexChild();
		assertEquals("Incorrect first parent", "First", parent.getInfo());
		assertEquals("Incorrect first child", "First", child.getInfo());
		// Second
		parent = child.getComplexParent();
		child = parent.getComplexChild();
		assertEquals("Incorrect second parent", "Second", parent.getInfo());
		assertEquals("Incorrect second child", "Second", child.getInfo());
		// Third
		parent = child.getComplexParent();
		child = parent.getComplexChild();
		assertEquals("Incorrect third parent", "Third", parent.getInfo());
		assertEquals("Incorrect third child", "Third", child.getInfo());

		// Many children
		ManyChildren manyChild = targetObject.getManyChildren();
		assertEquals("Many Child One incorrect", "One", manyChild.getName());
		ManyChildren[] oneChildren = manyChild.getChildren();
		assertEquals("Incorrect number of One children", 3, oneChildren.length);
		assertEquals("Many Child Two incorrect", "Two", oneChildren[0]
				.getName());
		assertEquals("Many Child Five incorrect", "Five", oneChildren[1]
				.getName());
		assertEquals("Many Child Six incorrect", "Six", oneChildren[2]
				.getName());
		ManyChildren[] twoChildren = oneChildren[0].getChildren();
		assertEquals("Incorrect number of Two children", 2, twoChildren.length);
		assertEquals("Many Child Three incorrect", "Three", twoChildren[0]
				.getName());
		assertEquals("Many Child Four incorrect", "Four", twoChildren[1]
				.getName());
		ManyChildren[] sixChildren = oneChildren[2].getChildren();
		assertEquals("Incorrect number of Six children", 2, sixChildren.length);
		assertEquals("Many Child Seven incorrect", "Seven", sixChildren[0]
				.getName());
		assertEquals("Many Child Eight incorrect", "Eight", sixChildren[1]
				.getName());
		ManyChildren[] eightChildren = sixChildren[1].getChildren();
		assertEquals("Incorrect number of Eight children", 1,
				eightChildren.length);
		assertEquals("Many Child Nine incorrect", "Nine", eightChildren[0]
				.getName());
	}
}
