/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.xml.unmarshall.designate;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.officefloor.plugin.xml.XmlMarshallException;
import net.officefloor.plugin.xml.unmarshall.translate.TranslatorRegistry;
import net.officefloor.plugin.xml.unmarshall.tree.ReferencedXmlMappingRegistry;
import net.officefloor.plugin.xml.unmarshall.tree.TreeXmlUnmarshaller;
import net.officefloor.plugin.xml.unmarshall.tree.TreeXmlUnmarshallerManagedObjectSource;
import net.officefloor.plugin.xml.unmarshall.tree.XmlContext;
import net.officefloor.plugin.xml.unmarshall.tree.XmlMappingMetaData;
import net.officefloor.plugin.xml.unmarshall.tree.XmlState;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * {@link net.officefloor.plugin.xml.XmlUnmarshaller} that delegates to XML
 * specific {@link net.officefloor.plugin.xml.XmlUnmarshaller} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class DesignateXmlUnmarshaller {

	/**
	 * {@link Map} of root element name to {@link DelegateMetaData}.
	 */
	private final Map<String, DelegateMetaData> delegates = new HashMap<String, DelegateMetaData>();

	/**
	 * Registers a delegate
	 * {@link net.officefloor.plugin.xml.unmarshall.tree.TreeXmlUnmarshaller}.
	 * 
	 * @param configuration
	 *            Configuration of the
	 *            {@link net.officefloor.plugin.xml.unmarshall.tree.TreeXmlUnmarshaller}.
	 * @throws XmlMarshallException
	 *             If fails to register the
	 *             {@link net.officefloor.plugin.xml.unmarshall.tree.TreeXmlUnmarshaller}.
	 */
	public void registerTreeXmlUnmarshaller(InputStream configuration)
			throws XmlMarshallException {

		// Create the translator registry
		TranslatorRegistry translatorRegistry = new TranslatorRegistry();

		// Create the XML mapping meta-data
		XmlMappingMetaData metaData = TreeXmlUnmarshallerManagedObjectSource
				.createXmlMappingMetaData(configuration, translatorRegistry);

		// Obtain the root element name
		String rootElementName = metaData.getElementName();

		// Obtain the root class
		String rootClassName = metaData.getLoadObjectClassName();
		Class<?> rootClass;
		try {
			rootClass = Class.forName(rootClassName);
		} catch (ClassNotFoundException ex) {
			// Propagate
			throw new XmlMarshallException(
					"Can not find class of target object '" + rootClassName
							+ "'", ex);
		}

		// Register the delegate
		this.delegates.put(rootElementName, new DelegateMetaData(rootClass,
				new XmlContext(rootClass, metaData.getElementName(), metaData
						.getLoadObjectConfiguration(), translatorRegistry,
						new ReferencedXmlMappingRegistry())));
	}

	/**
	 * Unmarshalls the input XML returning the unmarshalled object.
	 * 
	 * @param xml
	 *            XML.
	 * @return Unmarshalled object.
	 * @throws XmlMarshallException
	 *             If fails to unmarshall the XML.
	 */
	public Object unmarshall(InputStream xml) throws XmlMarshallException {

		// Create the parser
		SAXParser parser;
		try {
			parser = SAXParserFactory.newInstance().newSAXParser();
		} catch (Exception ex) {
			// Propagate failure
			throw new XmlMarshallException("Failure to create a SAX parser");
		}

		// Create the handler
		DesignateHandler handler = new DesignateHandler();

		// Unmarshall the XML
		try {
			parser.parse(xml, handler);
		} catch (SAXException ex) {
			// Propagate failure
			throw new XmlMarshallException(ex.getMessage(),
					(ex.getCause() == null ? ex : ex.getCause()));
		} catch (IOException ex) {
			// Propagate failure
			throw new XmlMarshallException(ex.getMessage(), ex);
		}

		// Return the unmarshalled object
		return handler.targetObject;
	}

	/**
	 * Meta-data of the delegate
	 * {@link net.officefloor.plugin.xml.unmarshall.tree.TreeXmlUnmarshaller}.
	 */
	protected class DelegateMetaData {

		/**
		 * Root {@link Class}.
		 */
		public final Class<?> rootClass;

		/**
		 * Delegate {@link XmlContext}.
		 */
		public final XmlContext context;

		/**
		 * Initiate.
		 * 
		 * @param rootClass
		 *            Root {@link Class}.
		 * @param delegateContext
		 *            Delegate {@link XmlContext}.
		 */
		public DelegateMetaData(Class<?> rootClass, XmlContext delegateContext) {
			this.rootClass = rootClass;
			this.context = delegateContext;
		}
	}

	/**
	 * {@link DefaultHandler} to designate to the delegate
	 * {@link DefaultHandler}.
	 */
	protected class DesignateHandler extends DefaultHandler {

		/**
		 * Delegate {@link DefaultHandler}.
		 */
		private DefaultHandler delegate = null;

		/**
		 * Unmarshalled object.
		 */
		public Object targetObject = null;

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
		 *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
		 */
		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {

			// Ensure have delegate
			if (this.delegate == null) {

				// Obtain the delegate meta-data
				DelegateMetaData metaData = DesignateXmlUnmarshaller.this.delegates
						.get(qName);
				if (metaData == null) {
					throw new SAXException(
							"No delegate registered for root element '"
									+ qName + "'");
				}

				// Create the target object
				Class<?> targetClass = metaData.rootClass;
				try {
					this.targetObject = targetClass.newInstance();
				} catch (InstantiationException ex) {
					// Propagate
					throw new SAXException(
							"Failed instantiating target object "
									+ targetClass.getName()
									+ " by default constructor", ex);
				} catch (IllegalAccessException ex) {
					// Propagate
					throw new SAXException(
							"Failed access to create target object "
									+ targetClass.getName()
									+ " by default constructor", ex);
				}

				// Create the delegate default handler
				XmlState xmlState = new XmlState(metaData.context);
				this.delegate = new TreeXmlUnmarshaller.HandlerImpl(xmlState);

				// Specify the target object
				xmlState.setTargetObject(this.targetObject);
			}

			// Delegate
			this.delegate.startElement(uri, localName, qName, attributes);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
		 */
		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			// Delegate
			this.delegate.characters(ch, start, length);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String,
		 *      java.lang.String, java.lang.String)
		 */
		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			// Delegate
			this.delegate.endElement(uri, localName, qName);
		}
	}

}
