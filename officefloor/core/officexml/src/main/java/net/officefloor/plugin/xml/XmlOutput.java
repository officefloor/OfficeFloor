package net.officefloor.plugin.xml;

import java.io.IOException;

/**
 * Contract to output the text of the XML.
 * 
 * @author Daniel Sagenschneider
 */
public interface XmlOutput {

	/**
	 * Writes the XML snippet.
	 * 
	 * @param xmlSnippet
	 *            XML snippet.
	 * @throws IOException
	 *             If fail to write the XML snippet.
	 */
	void write(String xmlSnippet) throws IOException;
}
