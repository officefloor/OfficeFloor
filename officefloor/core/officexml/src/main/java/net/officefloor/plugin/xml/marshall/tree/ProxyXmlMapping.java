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

import net.officefloor.plugin.xml.XmlMarshallException;
import net.officefloor.plugin.xml.XmlOutput;

/**
 * Enables wrapping of a
 * {@link net.officefloor.plugin.xml.marshall.tree.XmlWriter} to let it be
 * referenced.
 * 
 * @author Daniel Sagenschneider
 */
public class ProxyXmlMapping implements XmlMapping {

	/**
	 * {@link XmlMapping} being wrapped to delegate XML mapping.
	 */
	protected XmlMapping delegate;

	/**
	 * <p>
	 * The creation of {@link XmlMapping} (and its {@link XmlWriter}) will
	 * recursively load its {@link XmlMapping}.
	 * </p>
	 * <p>
	 * This is therefore necessary to enable a child to reference this in place
	 * of the actual parent {@link XmlMapping}, as the parent
	 * {@link XmlMapping} will not be available until all its descendants are
	 * created.
	 * </p>
	 * 
	 * @param delegate
	 *            Delegate to do the XML writing.
	 */
	public void setDelegate(XmlMapping delegate) {
		this.delegate = delegate;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.xml.marshall.tree.XmlMapping#map(java.lang.Object,
	 *      net.officefloor.plugin.xml.XmlOutput)
	 */
	public void map(Object source, XmlOutput output)
			throws XmlMarshallException {
		// Delegate
		this.delegate.map(source, output);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.xml.marshall.tree.XmlMapping#getWriter()
	 */
	public XmlWriter getWriter() {
		return this.delegate.getWriter();
	}

}
