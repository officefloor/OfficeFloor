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
				boolean isCloseElement = (characters[i + 1] == '/');

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
