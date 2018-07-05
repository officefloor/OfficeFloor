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
package net.officefloor.plugin.xml.marshall.tree;

import net.officefloor.plugin.xml.marshall.translate.TranslatorRegistry;
import net.officefloor.plugin.xml.marshall.tree.objects.RootObject;

/**
 * Tests the {@link net.officefloor.plugin.xml.marshall.tree.TreeXmlMarshaller}.
 * 
 * @author Daniel Sagenschneider
 */
public class TreeXmlMarshallerTest extends AbstractTreeXmlMarshallerTestCase {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.xml.marshall.tree.AbstractTreeXmlMarshallerTestCase#createFlatMarshaller()
	 */
	protected TreeXmlMarshaller createFlatMarshaller() throws Exception {

		// Create the configuation of the marshaller
		XmlMappingMetaData metaData = new XmlMappingMetaData(
				XmlMappingType.ROOT,
				"root",
				RootObject.class,
				new XmlMappingMetaData[] {
						new XmlMappingMetaData(
								XmlMappingType.ATTRIBUTES,
								new XmlMappingMetaData[] { new XmlMappingMetaData(
										XmlMappingType.ATTRIBUTE, "boolean",
										"getBoolean", true) }),
						new XmlMappingMetaData(XmlMappingType.VALUE, "byte",
								"getByte", true) }, null);

		// Create and return the marshaller
		return new TreeXmlMarshaller(metaData, new TranslatorRegistry(),
				new ReferencedXmlMappingRegistry());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.xml.marshall.tree.AbstractTreeXmlMarshallerTestCase#createTreeMarshaller()
	 */
	protected TreeXmlMarshaller createTreeMarshaller() throws Exception {

		// Create the configuation of the marshaller (follows same method twice)
		XmlMappingMetaData metaData = new XmlMappingMetaData(
				XmlMappingType.ROOT,
				"root",
				RootObject.class,
				new XmlMappingMetaData[] {
						new XmlMappingMetaData(
								XmlMappingType.ATTRIBUTES,
								new XmlMappingMetaData[] { new XmlMappingMetaData(
										XmlMappingType.ATTRIBUTE, "int",
										"getInt", true) }),
						new XmlMappingMetaData(
								XmlMappingType.OBJECT,
								"child",
								"getChild",
								new XmlMappingMetaData[] {
										new XmlMappingMetaData(
												XmlMappingType.ATTRIBUTES,
												new XmlMappingMetaData[] { new XmlMappingMetaData(
														XmlMappingType.ATTRIBUTE,
														"long", "getLong", true) }),
										new XmlMappingMetaData(
												XmlMappingType.VALUE, "float",
												"getFloat", true) }, null),
						new XmlMappingMetaData(
								XmlMappingType.OBJECT,
								"another-child",
								"getChild",
								new XmlMappingMetaData[] { new XmlMappingMetaData(
										XmlMappingType.ATTRIBUTES,
										new XmlMappingMetaData[] { new XmlMappingMetaData(
												XmlMappingType.ATTRIBUTE,
												"double", "getDouble", true) }), },
								null) }, null);

		// Create and return the marshaller
		return new TreeXmlMarshaller(metaData, new TranslatorRegistry(),
				new ReferencedXmlMappingRegistry());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.xml.marshall.tree.AbstractTreeXmlMarshallerTestCase#createCollectionMarshaller()
	 */
	protected TreeXmlMarshaller createCollectionMarshaller() throws Exception {

		// Create the configuation of the marshaller (follows same method twice)
		XmlMappingMetaData metaData = new XmlMappingMetaData(
				XmlMappingType.ROOT,
				"root",
				RootObject.class,
				new XmlMappingMetaData[] { new XmlMappingMetaData(
						XmlMappingType.COLLECTION,
						"children",
						"getChildren",
						new XmlMappingMetaData[] { new XmlMappingMetaData(
								XmlMappingType.ITEM,
								null,
								RootObject.class,
								new XmlMappingMetaData[] { new XmlMappingMetaData(
										XmlMappingType.VALUE, "int", "getInt",
										true) }, null) }, null) }, null);

		// Create and return the marshaller
		return new TreeXmlMarshaller(metaData, new TranslatorRegistry(),
				new ReferencedXmlMappingRegistry());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.xml.marshall.tree.AbstractTreeXmlMarshallerTestCase#createTypeMarshaller()
	 */
	protected TreeXmlMarshaller createTypeMarshaller() throws Exception {

		// Create the configuation of the marshaller (follows same method twice)
		XmlMappingMetaData metaData = new XmlMappingMetaData(
				XmlMappingType.ROOT,
				"root",
				RootObject.class,
				new XmlMappingMetaData[] { new XmlMappingMetaData(
						XmlMappingType.TYPE,
						"generic",
						"getGenericType",
						new XmlMappingMetaData[] { new XmlMappingMetaData(
								XmlMappingType.ITEM,
								"root-object",
								RootObject.class,
								new XmlMappingMetaData[] { new XmlMappingMetaData(
										XmlMappingType.VALUE, "int", "getInt",
										true) }, null) }, null) }, null);

		// Create and return the marshaller
		return new TreeXmlMarshaller(metaData, new TranslatorRegistry(),
				new ReferencedXmlMappingRegistry());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.xml.marshall.tree.AbstractTreeXmlMarshallerTestCase#createReferenceMarshaller()
	 */
	protected TreeXmlMarshaller createReferenceMarshaller() throws Exception {

		// Create the configuation of the marshaller (follows same method twice)
		XmlMappingMetaData metaData = new XmlMappingMetaData(
				XmlMappingType.ROOT,
				"root",
				RootObject.class,
				new XmlMappingMetaData[] {
						new XmlMappingMetaData(
								XmlMappingType.ATTRIBUTES,
								new XmlMappingMetaData[] { new XmlMappingMetaData(
										XmlMappingType.ATTRIBUTE, "char",
										"getChar", true) }),
						new XmlMappingMetaData(XmlMappingType.REFERENCE,
								"getRecursiveChild", "REFERENCE") },
				"REFERENCE");

		// Create and return the marshaller
		return new TreeXmlMarshaller(metaData, new TranslatorRegistry(),
				new ReferencedXmlMappingRegistry());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.xml.marshall.tree.AbstractTreeXmlMarshallerTestCase#createReferenceCollectionMarshaller()
	 */
	@Override
	protected TreeXmlMarshaller createReferenceCollectionMarshaller()
			throws Exception {

		// Create the configuation of the marshaller (follows same method twice)
		XmlMappingMetaData metaData = new XmlMappingMetaData(
				XmlMappingType.ROOT,
				"root",
				RootObject.class,
				new XmlMappingMetaData[] { new XmlMappingMetaData(
						XmlMappingType.COLLECTION,
						null,
						"getRecursiveChildren",
						new XmlMappingMetaData[] { new XmlMappingMetaData(
								XmlMappingType.ITEM,
								"root",
								RootObject.class,
								new XmlMappingMetaData[] { new XmlMappingMetaData(
										XmlMappingType.REFERENCE,
										"getRecursiveChildren", "REFERENCE") },
								null) }, "REFERENCE") }, null);

		// Create and return the marshaller
		return new TreeXmlMarshaller(metaData, new TranslatorRegistry(),
				new ReferencedXmlMappingRegistry());
	}

}
