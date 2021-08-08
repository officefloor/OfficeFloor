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

package net.officefloor.plugin.xml.unmarshall.tree;

import net.officefloor.plugin.xml.unmarshall.translate.TranslatorRegistry;
import net.officefloor.plugin.xml.unmarshall.tree.objects.ComplexChild;
import net.officefloor.plugin.xml.unmarshall.tree.objects.ComplexParent;
import net.officefloor.plugin.xml.unmarshall.tree.objects.FirstObject;
import net.officefloor.plugin.xml.unmarshall.tree.objects.FourthObject;
import net.officefloor.plugin.xml.unmarshall.tree.objects.ManyChildren;
import net.officefloor.plugin.xml.unmarshall.tree.objects.Person;
import net.officefloor.plugin.xml.unmarshall.tree.objects.RecursiveObject;
import net.officefloor.plugin.xml.unmarshall.tree.objects.SecondObject;
import net.officefloor.plugin.xml.unmarshall.tree.objects.ThirdObject;

/**
 * Tests the
 * {@link net.officefloor.plugin.xml.unmarshall.tree.TreeXmlUnmarshaller}.
 * 
 * @author Daniel Sagenschneider
 */
public class TreeXmlUnmarshallerTest extends
		AbstractTreeXmlUnmarshallerTestCase {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.xml.tree.AbstractTreeXmlUnmarshallerTestCase#createNonRecursiveTreeXmlUnmarshaller()
	 */
	protected TreeXmlUnmarshaller createNonRecursiveTreeXmlUnmarshaller()
			throws Exception {

		// Create the configuration
		XmlMappingMetaData[] configuration = new XmlMappingMetaData[] {
				new XmlMappingMetaData("first-class@info", "setInfo", null),
				new XmlMappingMetaData(
						"second-class",
						"setSecond",
						SecondObject.class,
						new XmlMappingMetaData[] {
								new XmlMappingMetaData("second-class@details",
										"setDetails", null),
								new XmlMappingMetaData("value", "setValue",
										null),
								new XmlMappingMetaData(
										"third-class",
										"setThird",
										ThirdObject.class,
										new XmlMappingMetaData[] { new XmlMappingMetaData(
												"value", "setValue", null) },
										null) }, null),
				new XmlMappingMetaData("fourth-class", "addFourth",
						FourthObject.class,
						new XmlMappingMetaData[] { new XmlMappingMetaData(
								"value", "setValue", null) }, null) };

		// Create the XML unmarshaller
		return new TreeXmlUnmarshaller(new XmlMappingMetaData(
				FirstObject.class, "xml-message", configuration),
				new TranslatorRegistry(), new ReferencedXmlMappingRegistry());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.xml.tree.AbstractTreeXmlUnmarshallerTestCase#createRecursiveTreeXmlUnmarshaller()
	 */
	protected TreeXmlUnmarshaller createRecursiveTreeXmlUnmarshaller()
			throws Exception {
		// Create the configuration
		XmlMappingMetaData[] configuration = new XmlMappingMetaData[] {
				new XmlMappingMetaData(
						"person",
						"setPerson",
						Person.class,
						new XmlMappingMetaData[] {
								new XmlMappingMetaData("person@position",
										"setPosition", null),
								new XmlMappingMetaData("setPerson", "Person") },
						"Person"),
				new XmlMappingMetaData("complex-parent", "setComplexParent",
						ComplexParent.class, new XmlMappingMetaData[] {
								new XmlMappingMetaData("complex-parent@info",
										"setInfo", null),
								new XmlMappingMetaData("complex-child",
										"setComplexChild", ComplexChild.class,
										new XmlMappingMetaData[] {
												new XmlMappingMetaData(
														"complex-child@info",
														"setInfo", null),
												new XmlMappingMetaData(
														"setComplexParent",
														"ComplexParent") },
										null) }, "ComplexParent"),
				new XmlMappingMetaData("many-children", "setManyChildren",
						ManyChildren.class, new XmlMappingMetaData[] {
								new XmlMappingMetaData("addChild",
										"ManyChildren"),
								new XmlMappingMetaData("many-children@name",
										"setName", null) }, "ManyChildren") };

		// Create the XML unmarshaller
		return new TreeXmlUnmarshaller(new XmlMappingMetaData(
				RecursiveObject.class, "recursive-xml-message", configuration),
				new TranslatorRegistry(), new ReferencedXmlMappingRegistry());
	}

}
