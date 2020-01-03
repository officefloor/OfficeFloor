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
