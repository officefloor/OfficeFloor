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
package net.officefloor.plugin.xml.marshall.tree;

import java.io.StringWriter;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.xml.XmlMarshaller;
import net.officefloor.plugin.xml.XmlOutput;
import net.officefloor.plugin.xml.marshall.output.WriterXmlOutput;
import net.officefloor.plugin.xml.marshall.tree.objects.RootObject;

/**
 * Tests the {@link net.officefloor.plugin.xml.XmlMarshaller} implementation.
 * 
 * @author Daniel
 */
public abstract class AbstractTreeXmlMarshallerTestCase extends
		OfficeFrameTestCase {

	/**
	 * Initiate the writer to capture the marshalled XML.
	 */
	protected StringWriter writer;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();

		// Initiate the writer
		writer = new StringWriter();
	}

	/**
	 * Ensures able to do a flat marshall of an object.
	 */
	public void testFlatMarshall() throws Exception {

		// Create the marshaller
		XmlMarshaller marshaller = this.createFlatMarshaller();

		// Create the root object
		RootObject object = new RootObject();

		// Marshall the object
		marshaller.marshall(object, new WriterXmlOutput(this.writer));

		// Validate the output
		assertEquals("Incorrect XML",
				"<root boolean=\"true\"><byte>1</byte></root>", this.writer
						.toString());
	}

	/**
	 * Creates the {@link TreeXmlMarshaller} for the flat marshall test.
	 * 
	 * @return {@link TreeXmlMarshaller} for flat marshall test.
	 * @throws Exception
	 *             If fails to create the {@link TreeXmlMarshaller}.
	 */
	protected abstract TreeXmlMarshaller createFlatMarshaller()
			throws Exception;

	/**
	 * Ensure able to tree marshall an object.
	 */
	public void testTreeMarshall() throws Exception {

		// Create the marshaller
		XmlMarshaller marshaller = this.createTreeMarshaller();

		// Create the root object
		RootObject object = new RootObject();

		// Marshall the object
		marshaller.marshall(object, new WriterXmlOutput(this.writer));

		// Validate the output
		assertEquals(
				"Incorrect XML",
				"<root int=\"2\"><child long=\"3\"><float>4.4</float></child><another-child double=\"5.5\"/></root>",
				this.writer.toString());

	}

	/**
	 * Creates the {@link TreeXmlMarshaller} for the tree marshall test.
	 * 
	 * @return {@link TreeXmlMarshaller} for tree marshall test.
	 * @throws Exception
	 *             If fails to create the {@link TreeXmlMarshaller}.
	 */
	protected abstract TreeXmlMarshaller createTreeMarshaller()
			throws Exception;

	/**
	 * Ensure able to marshall a Collection.
	 */
	public void testCollectionMarshall() throws Exception {

		// Create the marshaller
		XmlMarshaller marshaller = this.createCollectionMarshaller();

		// Create the root object
		RootObject object = new RootObject();

		// Marshall the object
		marshaller.marshall(object, new WriterXmlOutput(this.writer));

		// Validate the output
		assertEquals(
				"Incorrect XML",
				"<root><children><int>2</int><int>2</int><int>2</int><int>2</int></children></root>",
				this.writer.toString());

	}

	/**
	 * Creates the {@link TreeXmlMarshaller} for the collection marshall test.
	 * 
	 * @return {@link TreeXmlMarshaller} for collection marshall test.
	 * @throws Exception
	 *             If fails to create the {@link TreeXmlMarshaller}.
	 */
	protected abstract TreeXmlMarshaller createCollectionMarshaller()
			throws Exception;

	/**
	 * Ensure able to marshall a type.
	 */
	public void testTypeMarshall() throws Exception {

		// Create the marshaller
		XmlMarshaller marshaller = this.createTypeMarshaller();

		// Create the root object
		RootObject object = new RootObject();

		// Marshall the object
		marshaller.marshall(object, new WriterXmlOutput(this.writer));

		// Validate the output
		assertEquals(
				"Incorrect XML",
				"<root><generic><root-object><int>2</int></root-object></generic></root>",
				this.writer.toString());

	}

	/**
	 * Creates the {@link TreeXmlMarshaller} for the type marshall test.
	 * 
	 * @return {@link TreeXmlMarshaller} for type marshall test.
	 * @throws Exception
	 *             If fails to create the {@link TreeXmlMarshaller}.
	 */
	protected abstract TreeXmlMarshaller createTypeMarshaller()
			throws Exception;

	/**
	 * Ensure able to marshall a reference.
	 */
	public void testReferenceMarshall() throws Exception {

		// Create the marshaller
		XmlMarshaller marshaller = this.createReferenceMarshaller();

		// Create the root object
		RootObject object = new RootObject();

		// Marshall the object
		marshaller.marshall(object, new WriterXmlOutput(this.writer));

		// Validate the output
		assertEquals(
				"Incorrect XML",
				"<root char=\"a\"><root char=\"a\"><root char=\"a\"></root></root></root>",
				this.writer.toString());
	}

	/**
	 * Creates the {@link TreeXmlMarshaller} for the reference marshall test.
	 * 
	 * @return {@link TreeXmlMarshaller} for reference marshall test.
	 * @throws Exception
	 *             If fails to create the {@link TreeXmlMarshaller}.
	 */
	protected abstract TreeXmlMarshaller createReferenceMarshaller()
			throws Exception;

	/**
	 * Ensure able to marshall a referenced collection.
	 */
	public void testReferenceCollectionMarshall() throws Exception {

		// Create the Collection Marshaller
		XmlMarshaller marshaller = this.createReferenceCollectionMarshaller();

		// Create the root object
		RootObject object = new RootObject();

		// Marshall the object
		XmlOutput output = new WriterXmlOutput(this.writer);
		marshaller.marshall(object, output);

		// Validate the output
		assertEquals(
				"Incorrect XML",
				"<root><root><root></root><root></root></root><root><root></root><root></root></root></root>",
				this.writer.toString());
	}

	/**
	 * Creates the {@link TreeXmlMarshaller} for the reference collection
	 * marshall test.
	 * 
	 * @return {@link TreeXmlMarshaller} for reference collection marshall test.
	 * @throws Exception
	 *             If fails to create the {@link TreeXmlMarshaller}.
	 */
	protected abstract TreeXmlMarshaller createReferenceCollectionMarshaller()
			throws Exception;

}
