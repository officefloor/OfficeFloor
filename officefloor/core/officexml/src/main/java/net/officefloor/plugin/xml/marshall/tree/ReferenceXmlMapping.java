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
