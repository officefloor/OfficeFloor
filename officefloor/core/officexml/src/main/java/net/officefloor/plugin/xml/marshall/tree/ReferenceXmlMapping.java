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

package net.officefloor.plugin.xml.marshall.tree;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.officefloor.plugin.xml.XmlMarshallException;
import net.officefloor.plugin.xml.XmlOutput;

/**
 * Enables referencing another
 * {@link net.officefloor.plugin.xml.marshall.tree.XmlWriter} to write the XML
 * of the object obtained from the source object.
 * 
 * @author Daniel Sagenschneider
 */
public class ReferenceXmlMapping implements XmlMapping {

	/**
	 * Name of method to obtain object to write as XML.
	 */
	protected final String getMethodName;

	/**
	 * {@link XmlMapping} being wrapped to delegate XML writing.
	 */
	protected final XmlMapping delegate;

	/**
	 * Methods specific to a class.
	 */
	protected final Map<Class<?>, Method> methods = new HashMap<Class<?>, Method>();

	/**
	 * Initiate to apply reference mapping.
	 * 
	 * @param getMethod
	 *            Method to obtain object to apply reference from upper bound
	 *            type of source object.
	 * @param delegate
	 *            Delgate to object the {@link XmlWriter} to write the XML.
	 */
	public ReferenceXmlMapping(Method getMethod, XmlMapping delegate) {
		// Store state
		this.getMethodName = getMethod.getName();
		this.delegate = delegate;

		// Register the method
		this.methods.put(getMethod.getDeclaringClass(), getMethod);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.xml.marshall.tree.XmlMapping#map(java.lang.Object,
	 *      net.officefloor.plugin.xml.XmlOutput)
	 */
	public void map(Object source, XmlOutput output)
			throws XmlMarshallException {

		// Only map if have source
		if (source == null) {
			return;
		}

		// Obtain type to obtain object to map
		Class<?> type = source.getClass();

		// Obtain method to obtain object to write
		Method getMethod = this.methods.get(type);
		if (getMethod == null) {
			// Do not have therefore create and cache
			getMethod = XmlMarshallerUtil
					.obtainMethod(type, this.getMethodName);
			this.methods.put(type, getMethod);
		}

		// Obtain object to write
		Object object = XmlMarshallerUtil.getReturnValue(source, getMethod);

		// Delegate to write XML
		this.getWriter().writeXml(object, output);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.xml.marshall.tree.XmlMapping#getWriter()
	 */
	public XmlWriter getWriter() {
		// Utilise delegate's writer (subsequently the referencing)
		return this.delegate.getWriter();
	}

}
