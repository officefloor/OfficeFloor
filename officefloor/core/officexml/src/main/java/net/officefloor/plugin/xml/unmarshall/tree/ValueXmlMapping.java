package net.officefloor.plugin.xml.unmarshall.tree;

import net.officefloor.plugin.xml.XmlMarshallException;
import net.officefloor.plugin.xml.unmarshall.load.DynamicValueLoader;

/**
 * {@link net.officefloor.plugin.xml.unmarshall.tree.XmlMapping} that loads a value to the
 * target object.
 * 
 * @author Daniel Sagenschneider
 */
public class ValueXmlMapping implements XmlMapping {

	/**
	 * Loads the value onto the target object.
	 */
	protected final DynamicValueLoader loader;

	/**
	 * Initiate with the {@link DynamicValueLoader}.
	 * 
	 * @param loader
	 *            Loads the value onto the target object.
	 */
	public ValueXmlMapping(DynamicValueLoader loader) {
		// Store state
		this.loader = loader;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.xml.tree.XmlMapping#startMapping(net.officefloor.plugin.xml.tree.XmlState,
	 *      java.lang.String)
	 */
	public void startMapping(XmlState state, String elementName)
			throws XmlMarshallException {
		// Does nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.xml.tree.XmlMapping#endMapping(net.officefloor.plugin.xml.tree.XmlState,
	 *      java.lang.String)
	 */
	public void endMapping(XmlState state, String value)
			throws XmlMarshallException {
		// Load the value
		this.loader.loadValue(state.getCurrentTargetObject(), value);
	}
}
