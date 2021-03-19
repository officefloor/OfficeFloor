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
