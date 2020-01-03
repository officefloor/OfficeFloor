package net.officefloor.plugin.xml.marshall.output;

import java.io.IOException;
import java.io.Writer;

import net.officefloor.plugin.xml.XmlOutput;

/**
 * Implementation of {@link net.officefloor.plugin.xml.XmlOutput} to write to a
 * {@link java.io.Writer}.
 * 
 * @author Daniel Sagenschneider
 */
public class WriterXmlOutput implements XmlOutput {

	/**
	 * {@link java.io.Writer} to write XML.
	 */
	protected final Writer writer;

	/**
	 * Initiate with the {@link Writer}.
	 * 
	 * @param writer
	 *            {@link Writer}.
	 */
	public WriterXmlOutput(Writer writer) {
		this.writer = writer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.xml.XmlOutput#write(java.lang.String)
	 */
	public void write(String xmlSnippet) throws IOException {
		// output
		this.writer.write(xmlSnippet);
	}

}
