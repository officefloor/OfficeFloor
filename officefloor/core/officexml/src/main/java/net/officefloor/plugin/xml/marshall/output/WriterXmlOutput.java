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
