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

import net.officefloor.plugin.xml.XmlOutput;

/**
 * Provides formatting of the XML output to a delegate
 * {@link net.officefloor.plugin.xml.XmlOutput}.
 * 
 * @author Daniel Sagenschneider
 */
public class FormattedXmlOutput implements XmlOutput {

	/**
	 * Delegate to write the formatted XML.
	 */
	private final XmlOutput delegate;

	/**
	 * Indent for formatting the XML.
	 */
	private final String indent;

	/**
	 * Most previous character of all calls to {@link #write(String)}.
	 */
	private char prevChar = '.'; // not match '>' as initially root

	/**
	 * Current element depth.
	 */
	private int elementDepth = -1; // as matches on root

	/**
	 * Initiate.
	 * 
	 * @param delegate
	 *            Delegate {@link XmlOutput} to send the formatted XML.
	 * @param indent
	 *            Indent to use in formatting the XML.
	 */
	public FormattedXmlOutput(XmlOutput delegate, String indent) {
		this.delegate = delegate;
		this.indent = indent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.xml.XmlOutput#write(java.lang.String)
	 */
	public void write(String xmlSnippet) throws IOException {
		// As all XML output must stream through this method, decoration used
		// for formatting. Also assumes not white spacing between elements.

		// Stream through characters formatting
		char[] characters = xmlSnippet.toCharArray();
		for (int i = 0; i < characters.length; i++) {
			char character = characters[i];

			// Decorate with formatting
			switch (character) {
			case '<':

				// Determine if closing element
				// Note for '<' will always have another character
				boolean isCloseElement = (characters.length > 1) && (characters[i + 1] == '/');

				// Increment before indent if opening
				if (!isCloseElement) {
					this.elementDepth++;
				}

				// Determine if text in element
				if (this.prevChar == '>') {
					// No text in element therefore format
					this.delegate.write("\n");
					for (int j = 0; j < this.elementDepth; j++) {
						this.delegate.write(this.indent);
					}
				}

				// Decrement after indent if closing
				if (isCloseElement) {
					this.elementDepth--;
				}

				break;

			case '>':
				// Determine if no closing element
				if (this.prevChar == '/') {
					this.elementDepth--;
				}

				break;

			default:
				break;
			}

			// Always write the character (as only decorating with extra
			// formatting)
			this.delegate.write(String.valueOf(character));

			// Setup for next iteration/call
			this.prevChar = character;
		}
	}

}
