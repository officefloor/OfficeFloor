/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.officefloor.plugin.web.http.template.parse;

import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.plugin.web.http.template.parse.HttpTemplate;
import net.officefloor.plugin.web.http.template.parse.HttpTemplateParser;
import net.officefloor.plugin.web.http.template.parse.HttpTemplateSection;
import net.officefloor.plugin.web.http.template.parse.HttpTemplateSectionContent;
import net.officefloor.plugin.web.http.template.parse.LinkHttpTemplateSectionContent;
import net.officefloor.plugin.web.http.template.parse.ReferenceHttpTemplateSectionContent;
import net.officefloor.plugin.web.http.template.parse.StaticHttpTemplateSectionContent;

/**
 * {@link HttpTemplateParser} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpTemplateParserImpl implements HttpTemplateParser {

	/**
	 * Parses the {@link HttpTemplate} from the raw template content.
	 * 
	 * @param rawTemplateContent
	 *            Raw template content.
	 * @return {@link HttpTemplate}.
	 */
	public static HttpTemplate parseHttpTemplate(String rawTemplateContent) {

		// Split the sections of the template
		List<SectionStruct> sectionStructs = new LinkedList<SectionStruct>();
		SectionStruct currentSection = new SectionStruct();
		String[] sectionsSplit = rawTemplateContent.split("<!--");
		for (String sectionContent : sectionsSplit) {

			// Obtain the section structure
			SectionStruct struct = parseSection(sectionContent);

			// Determine if not a new section
			if (struct == null) {
				// Add section content to current structure
				currentSection.content += sectionContent;

				// Ensure first current section is added if has content
				if ((currentSection.name == null)
						&& (currentSection.content.length() > 0)) {
					currentSection.name = "template";
					sectionStructs.add(currentSection);
				}
				continue;
			}

			// New section
			sectionStructs.add(struct);
			currentSection = struct;
		}

		// Create the listing of sections
		HttpTemplateSection[] sections = new HttpTemplateSection[sectionStructs
				.size()];
		for (int s = 0; s < sections.length; s++) {
			SectionStruct sectionStruct = sectionStructs.get(s);

			// Create the content for the section
			HttpTemplateSectionContent[] contents = parseHttpTemplateSectionContent(sectionStruct.content);

			// Create and load the section
			HttpTemplateSection section = new HttpTemplateSectionImpl(
					sectionStruct.name, contents);
			sections[s] = section;
		}

		// Create the template
		HttpTemplate template = new HttpTemplateImpl(sections);

		// Return the template
		return template;
	}

	/**
	 * Parses the section content to obtain its name and raw content.
	 * 
	 * @param rawSectionContent
	 *            Raw section content.
	 * @return {@link SectionStruct} or <code>null</code> if input not complete
	 *         raw content for a section.
	 */
	public static SectionStruct parseSection(String rawSectionContent) {

		// Obtain the index of section name start
		int nameStartBracket = rawSectionContent.indexOf("{");
		if (nameStartBracket < 0) {
			// Section must have a name
			return null;
		}

		// Ensure only white spacing leading up to name bracket
		String leadingCharacters = rawSectionContent.substring(0,
				nameStartBracket);
		if (leadingCharacters.trim().length() != 0) {
			// Must only have leading white space to name
			return null;
		}

		// Obtain the index of the section name finish
		int nameFinishBracket = rawSectionContent.indexOf("}");
		if (nameFinishBracket < 0) {
			// Section must have name
			return null;
		}

		// Obtain the index of closing comment
		int closingComment = rawSectionContent.indexOf("-->");
		if (closingComment < 0) {
			// Comment must finish in section to have content
			return null;
		}

		// Ensure order is start/finish bracket followed by closing comment
		if ((nameStartBracket > nameFinishBracket)
				|| (nameFinishBracket > closingComment)) {
			// Order is not correct
			return null;
		}

		// Obtain details of section
		SectionStruct struct = new SectionStruct();
		struct.name = rawSectionContent.substring(nameStartBracket
				+ "]".length(), nameFinishBracket);
		struct.content = rawSectionContent.substring(closingComment
				+ "-->".length());

		// Return the details of section
		return struct;
	}

	/**
	 * Structure to contain the raw details of a {@link HttpTemplateSection}.
	 */
	public static class SectionStruct {

		/**
		 * {@link HttpTemplateSection} name.
		 */
		public String name;

		/**
		 * {@link HttpTemplateSection} content.
		 */
		public String content = "";

		/**
		 * Only allow creation from static method.
		 */
		private SectionStruct() {
		}
	}

	/**
	 * Parse state of a {@link HttpTemplateSection} into
	 * {@link HttpTemplateSectionContent} instances.
	 */
	private enum SectionParseState {
		STATIC,

		REFERENCE_PREFIX, REFERENCE,

		LINK_PREFIX, LINK
	}

	/**
	 * Parses the raw content into {@link HttpTemplateSectionContent} instances.
	 * 
	 * @param rawContent
	 *            Raw content.
	 * @return {@link HttpTemplateSectionContent} instances.
	 */
	public static HttpTemplateSectionContent[] parseHttpTemplateSectionContent(
			String rawContent) {

		// Listing of contents
		List<HttpTemplateSectionContent> contents = new LinkedList<HttpTemplateSectionContent>();

		// Parse the raw content
		StringBuilder buffer = new StringBuilder();
		SectionParseState parseState = SectionParseState.STATIC;
		for (int i = 0; i < rawContent.length(); i++) {
			char character = rawContent.charAt(i);

			// Process based on state
			switch (parseState) {
			case STATIC:
				// Determine if starting reference/link
				switch (character) {
				case '$':
					// Possibly starting reference
					parseState = SectionParseState.REFERENCE_PREFIX;
					break;
				case '#':
					parseState = SectionParseState.LINK_PREFIX;
					break;
				default:
					// Add static content to buffer
					buffer.append(character);
					break;
				}
				break;

			case REFERENCE_PREFIX:
				// Determine if starting reference
				if (character == '{') {
					// Add the static content and parsing reference
					buffer = addStatic(contents, buffer);
					parseState = SectionParseState.REFERENCE;
				} else {
					// Not parsing reference, so add back content
					buffer.append('$');
					buffer.append(character);
					parseState = SectionParseState.STATIC;
				}
				break;

			case REFERENCE:
				// Determine if complete reference
				if (character == '}') {
					// Add the reference and now parsing static
					buffer = addReference(contents, buffer);
					parseState = SectionParseState.STATIC;
				} else {
					// Add content for key
					buffer.append(character);
				}
				break;

			case LINK_PREFIX:
				// Determine if starting link
				if (character == '{') {
					// Add the static content and parsing link
					buffer = addStatic(contents, buffer);
					parseState = SectionParseState.LINK;
				} else {
					// Not parsing link, so add back content
					buffer.append('#');
					buffer.append(character);
					parseState = SectionParseState.STATIC;
				}
				break;

			case LINK:
				// Determine if complete link
				if (character == '}') {
					// Add the link and now parsing static
					buffer = addLink(contents, buffer);
					parseState = SectionParseState.STATIC;
				} else {
					// Add content for key
					buffer.append(character);
				}
				break;

			default:
				throw new IllegalStateException("Unknown parse state: "
						+ parseState);
			}
		}

		// End of parsing so set remaining static content
		switch (parseState) {
		case STATIC:
			// Do nothing and take as is
			break;

		case REFERENCE_PREFIX:
			// Ending with prefix
			buffer.append('$');
			break;
		case REFERENCE:
			// Partial reference content
			String referenceContent = "${" + buffer.toString();
			buffer = new StringBuilder(referenceContent);
			break;

		case LINK_PREFIX:
			// Ending with prefix
			buffer.append('#');
			break;
		case LINK:
			// Partial link content
			String linkContent = "#{" + buffer.toString();
			buffer = new StringBuilder(linkContent);
			break;

		default:
			throw new IllegalStateException("Unknown parse state: "
					+ parseState);
		}

		// Add the remaining static content
		addStatic(contents, buffer);

		// Return the contents
		return contents.toArray(new HttpTemplateSectionContent[0]);
	}

	/**
	 * Adds the {@link StaticHttpTemplateSectionContent} to the contents.
	 * 
	 * @param contents
	 *            Contents.
	 * @param staticContent
	 *            Static content.
	 * @return New {@link StringBuilder} instance.
	 */
	private static StringBuilder addStatic(
			List<HttpTemplateSectionContent> contents,
			StringBuilder staticContent) {

		// Determine if last content is also static
		String prefixStaticContent = "";
		if (contents.size() > 0) {
			// Obtain last content
			HttpTemplateSectionContent httpContent = contents.get(contents
					.size() - 1);
			if (httpContent instanceof StaticHttpTemplateSectionContent) {
				// Last content is static, so add only a single static
				StaticHttpTemplateSectionContent staticHttpContent = (StaticHttpTemplateSectionContent) httpContent;
				prefixStaticContent = staticHttpContent.getStaticContent();

				// Remove last content, so add with additional static content
				contents.remove(httpContent);
			}
		}

		// Only add if have static content
		String content = staticContent.toString();
		if ((prefixStaticContent.length() + content.length()) > 0) {
			contents.add(new StaticHttpTemplateSectionContentImpl(
					prefixStaticContent + content));
		}

		// Return new buffer
		return new StringBuilder();
	}

	/**
	 * Adds the {@link ReferenceHttpTemplateSectionContent} to the contents.
	 * 
	 * @param contents
	 *            Contents.
	 * @param referenceKey
	 *            Reference key.
	 * @return New {@link StringBuilder} instance.
	 */
	private static StringBuilder addReference(
			List<HttpTemplateSectionContent> contents,
			StringBuilder referenceKey) {
		// Always add reference
		String key = referenceKey.toString();
		contents.add(new ReferenceHttpTemplateSectionContentImpl(key));

		// Return new buffer
		return new StringBuilder();
	}

	/**
	 * Adds the {@link LinkHttpTemplateSectionContent} to the contents.
	 * 
	 * @param contents
	 *            Contents.
	 * @param referenceKey
	 *            Reference key.
	 * @return New {@link StringBuilder} instance.
	 */
	private static StringBuilder addLink(
			List<HttpTemplateSectionContent> contents, StringBuilder linkName) {
		// Always add link
		String name = linkName.toString();
		contents.add(new LinkHttpTemplateSectionContentImpl(name));

		// Return new buffer
		return new StringBuilder();
	}

	/*
	 * =================== HttpTemplateParser ==========================
	 */

	@Override
	public HttpTemplate parse(Reader reader) throws IOException {

		// Read in the contents of template
		StringBuilder contentsBuilder = new StringBuilder();
		for (int value = reader.read(); value != -1; value = reader.read()) {
			char character = (char) value;
			contentsBuilder.append(character);
		}
		String contents = contentsBuilder.toString();

		// Return the parsed HTTP Template
		return parseHttpTemplate(contents);
	}

}