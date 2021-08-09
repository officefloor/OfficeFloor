/*-
 * #%L
 * OfficeXml
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

package net.officefloor.plugin.xml.unmarshall.designate;

import java.io.InputStream;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.xml.unmarshall.tree.TreeXmlUnmarshallerTest;
import net.officefloor.plugin.xml.unmarshall.tree.objects.FirstObject;
import net.officefloor.plugin.xml.unmarshall.tree.objects.RecursiveObject;

/**
 * Tests the
 * {@link net.officefloor.plugin.xml.unmarshall.designate.DesignateXmlUnmarshaller}.
 * 
 * @author Daniel Sagenschneider
 */
public class DesignateXmlUnmarshallerTest extends OfficeFrameTestCase {

	/**
	 * Package class.
	 */
	private final Class<?> packageClass = TreeXmlUnmarshallerTest.class;

	/**
	 * {@link DesignateXmlUnmarshaller} to be tested.
	 */
	private DesignateXmlUnmarshaller unmarshaller;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {

		// Obtain meta-data of the delegates
		InputStream nonRecursiveMetaData = this.findInputStream(
				this.packageClass, "NonRecursiveMetaData.xml");
		InputStream recursiveMetaData = this.findInputStream(this.packageClass,
				"RecursiveMetaData.xml");

		// Initiate the unmarshaller
		this.unmarshaller = new DesignateXmlUnmarshaller();
		this.unmarshaller.registerTreeXmlUnmarshaller(nonRecursiveMetaData);
		this.unmarshaller.registerTreeXmlUnmarshaller(recursiveMetaData);
	}

	/**
	 * Ensure able to unmarshall via the various delegates.
	 */
	public void testUnmarshall() throws Exception {

		// Unmarshall non-recursive
		FirstObject nonRecursive = (FirstObject) this.unmarshaller
				.unmarshall(this.findInputStream(this.packageClass,
						"NonRecursiveInputFile.xml"));
		assertNotNull("Non-recursive not obtained", nonRecursive);

		// Unmarshall recursive
		RecursiveObject recursive = (RecursiveObject) this.unmarshaller
				.unmarshall(this.findInputStream(this.packageClass,
						"RecursiveInputFile.xml"));
		assertNotNull("Recursive not obtained", recursive);
	}

}
