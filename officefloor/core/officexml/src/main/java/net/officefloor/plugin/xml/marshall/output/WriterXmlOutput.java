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
