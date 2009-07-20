/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.plugin.work.http.html.template.parse;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;

/**
 * Template.
 * 
 * @author Daniel Sagenschneider
 */
public class Template {

	/**
	 * Parses the configuration into a {@link Template}.
	 * 
	 * @param inputStream
	 *            {@link InputStream} to configuration.
	 * @return {@link Template}.
	 * @throws IOException
	 *             If fails to read in {@link Template}.
	 */
	public static Template parse(InputStream inputStream) throws IOException {
		return parse(new InputStreamReader(inputStream));
	}

	/**
	 * Parses the configuration into a {@link Template}.
	 * 
	 * @param reader
	 *            {@link Reader} to configuration.
	 * @return {@link Template}.
	 * @throws IOException
	 *             If fails to read in {@link Template}.
	 */
	public static Template parse(Reader reader) throws IOException {

		// Read in the contents
		StringBuilder contents = new StringBuilder();
		for (int value = reader.read(); value != -1; value = reader.read()) {
			char character = (char) value;
			contents.append(character);
		}

		// Parse the contents
		return parse(contents.toString());
	}

	/**
	 * Parses the configuration into a {@link Template}.
	 * 
	 * @param configuration
	 *            Configuration.
	 * @return {@link Template}.
	 */
	public static Template parse(String configuration) {

		// Split the sections of the template
		List<SectionStruct> sectionStructs = new LinkedList<SectionStruct>();
		SectionStruct currentSection = new SectionStruct();
		String[] sectionsSplit = configuration.split("<!--");
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
		TemplateSection[] sections = new TemplateSection[sectionStructs.size()];
		for (int s = 0; s < sections.length; s++) {
			SectionStruct sectionStruct = sectionStructs.get(s);

			// Create the content for the section
			TemplateSectionContent[] contents = parseContent(sectionStruct.content);

			// Create and load the section
			TemplateSection section = new TemplateSection(sectionStruct.name,
					contents);
			sections[s] = section;
		}

		// Create the template
		Template template = new Template(sections);

		// Return the template
		return template;
	}

	/**
	 * Parse the section content to obtain its name and raw content.
	 * 
	 * @param rawSectionContent
	 *            Raw section content.
	 * @return {@link SectionStruct}.
	 */
	private static SectionStruct parseSection(String rawSectionContent) {

		// Obtain the index of section name start
		int nameStartBracket = rawSectionContent.indexOf("[");
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
		int nameFinishBracket = rawSectionContent.indexOf("]");
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
	 * Structure to contain the raw details of a {@link TemplateSection}.
	 */
	private static class SectionStruct {

		/**
		 * {@link TemplateSection} name.
		 */
		public String name;

		/**
		 * {@link TemplateSection} content.
		 */
		public String content = "";
	}

	/**
	 * Parses the raw content into {@link TemplateSectionContent} instances.
	 * 
	 * @param rawContent
	 *            Raw content.
	 * @return {@link TemplateSectionContent} instances.
	 */
	private static TemplateSectionContent[] parseContent(String rawContent) {

		// Split the section by reference
		List<TemplateSectionContent> contents = new LinkedList<TemplateSectionContent>();
		String[] contentsSplit = rawContent.split("[$][{]");

		// First split is always static content
		String firstContent = contentsSplit[0];
		if (firstContent.length() > 0) {
			contents.add(new StaticTemplateSectionContent(firstContent));
		}

		// Add remaining content
		for (int i = 1; i < contentsSplit.length; i++) {
			String contentSplit = contentsSplit[i];

			// Find location of closing bracket to reference key
			int keyCloseBracket = contentSplit.indexOf("}");

			// Obtain the key and following static content
			String key;
			String staticContent;
			if (keyCloseBracket < 0) {
				// No closing bracket found, so all static content
				key = "";
				staticContent = "${" + contentSplit;
			} else {
				// Closing bracket so have key
				key = contentSplit.substring(0, keyCloseBracket);
				staticContent = contentSplit.substring(keyCloseBracket
						+ "}".length());
			}

			// Load the key content
			if (key.length() > 0) {
				contents.add(new ReferenceTemplateSectionContent(key));
			}

			// Load the static content
			if (staticContent.length() > 0) {
				contents.add(new StaticTemplateSectionContent(staticContent));
			}
		}

		// Return the contents
		return contents.toArray(new TemplateSectionContent[0]);
	}

	/**
	 * {@link TemplateSection} instances.
	 */
	private final TemplateSection[] sections;

	/**
	 * Initiate.
	 * 
	 * @param sections
	 *            {@link TemplateSection} instances.
	 */
	private Template(TemplateSection[] sections) {
		this.sections = sections;
	}

	/**
	 * Obtains the {@link TemplateSection} instances for this {@link Template}.
	 * 
	 * @return {@link TemplateSection} instances.
	 */
	public TemplateSection[] getSections() {
		return this.sections;
	}
}
