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
