package net.officefloor.plugin.xml.unmarshall.tree;

import net.officefloor.plugin.xml.XmlMarshallException;

/**
 * {@link net.officefloor.plugin.xml.unmarshall.tree.XmlMapping} that puts the
 * root object into context.
 * 
 * @author Daniel Sagenschneider
 */
public class RootXmlMapping implements XmlMapping {

	/**
	 * {@link XmlContext} of the root target object.
	 */
	protected final XmlContext rootObjectContext;

	/**
	 * Initiate with {@link XmlContext} for the root target object.
	 * 
	 * @param rootObjectContext
	 *            {@link XmlContext} for the root target object.
	 */
	public RootXmlMapping(XmlContext rootObjectContext) {
		// Store state
		this.rootObjectContext = rootObjectContext;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.xml.unmarshall.tree.XmlMapping#startMapping(net.officefloor.plugin.xml.unmarshall.tree.XmlState,
	 *      java.lang.String)
	 */
	public void startMapping(XmlState state, String elementName)
			throws XmlMarshallException {
		// Push root context into scope
		state.pushContext(elementName, state.getCurrentTargetObject(),
				this.rootObjectContext);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.xml.unmarshall.tree.XmlMapping#endMapping(net.officefloor.plugin.xml.unmarshall.tree.XmlState,
	 *      java.lang.String)
	 */
	public void endMapping(XmlState state, String value)
			throws XmlMarshallException {
		// Pop the context happens on closing tag of pushed element name
	}

}
