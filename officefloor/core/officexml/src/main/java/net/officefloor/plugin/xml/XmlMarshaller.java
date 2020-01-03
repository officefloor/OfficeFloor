package net.officefloor.plugin.xml;

/**
 * Contract to marshall XML from an object.
 * 
 * @author Daniel Sagenschneider
 */
public interface XmlMarshaller {

	/**
	 * Marshalls the source object as XML to the output.
	 * 
	 * @param source
	 *            Object to marshall into XML.
	 * @param output
	 *            Output to send the XML.
	 * @throws XmlMarshallException
	 *             If fails to marshall source.
	 */
	void marshall(Object source, XmlOutput output) throws XmlMarshallException;
}
