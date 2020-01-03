package net.officefloor.plugin.xml.unmarshall.tree;

import net.officefloor.plugin.xml.XmlMarshallException;
import net.officefloor.plugin.xml.unmarshall.load.StaticValueLoader;

/**
 * {@link net.officefloor.plugin.xml.marshall.tree.XmlMapping} that loads a
 * static value to the target object.
 * 
 * @author Daniel Sagenschneider
 */
public class StaticXmlMapping implements XmlMapping {

	/**
	 * Loader to load the staic value.
	 */
	protected final StaticValueLoader loader;

	/**
	 * Initiate with static value loader.
	 * 
	 * @param loader
	 *            Loads the static value onto the target object.
	 */
	public StaticXmlMapping(StaticValueLoader loader) {
		// Store state
		this.loader = loader;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.xml.unmarshall.tree.XmlMapping#startMapping(net.officefloor.plugin.xml.unmarshall.tree.XmlState,
	 *      java.lang.String)
	 */
	public void startMapping(XmlState state, String elementName)
			throws XmlMarshallException {
		// Load static value to current target object
		this.loader.loadValue(state.getCurrentTargetObject());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.xml.unmarshall.tree.XmlMapping#endMapping(net.officefloor.plugin.xml.unmarshall.tree.XmlState,
	 *      java.lang.String)
	 */
	public void endMapping(XmlState state, String elementName)
			throws XmlMarshallException {
		// Do nothing
	}

}
