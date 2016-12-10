/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.plugin.gwt.template.transform;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.plugin.gwt.template.tranform.HtmlTagType;
import net.officefloor.plugin.gwt.template.tranform.HtmlTemplateTransformation;
import net.officefloor.plugin.gwt.template.tranform.HtmlTemplateTransformationContext;
import net.officefloor.plugin.gwt.template.tranform.HtmlTemplateTransformer;

/**
 * Transforms the HTML template.
 * 
 * @author Daniel Sagenschneider
 */
public class HtmlTemplateTransformerImpl implements HtmlTemplateTransformer {

	/*
	 * ===================== HtmlTemplateTransformer =========================
	 */

	@Override
	public String transform(String rawHtml,
			HtmlTemplateTransformation transformation) {

		// Buffer transformed content
		StringBuilder transformed = new StringBuilder();

		// Obtain initial start
		int previousTagEnd = 0; // start
		int tagStart = rawHtml.indexOf('<');
		if (tagStart < 0) {
			// No tag at all, so include all content
			transformed.append(rawHtml);
		}

		// Transform raw HTML
		TRANSFORMED: while (tagStart >= 0) {

			// Include the pre-tag content
			String preTagContent = rawHtml.substring(previousTagEnd, tagStart);
			transformed.append(preTagContent);

			// Find the tag end
			int tagEnd = rawHtml.indexOf('>', tagStart);
			if (tagEnd < 0) {
				// No closing bracket, so include remaining
				String tagContent = rawHtml.substring(tagStart);
				transformed.append(tagContent);
				break TRANSFORMED; // all content included
			}
			tagEnd += ">".length(); // include ending bracket

			// Obtain the tag content
			String tagContent = rawHtml.substring(tagStart, tagEnd);

			// Determine if a comment
			if (tagContent.startsWith("<!--")) {

				// Re-obtain details for comment
				tagEnd = rawHtml.indexOf("-->", tagStart);
				if (tagEnd < 0) {
					// No closing comment, so include remaining
					tagContent = rawHtml.substring(tagStart);
					transformed.append(tagContent);
					break TRANSFORMED; // all content included
				}
				tagEnd += "-->".length(); // include end comment
				tagContent = rawHtml.substring(tagStart, tagEnd);

				// Append comment
				transformed.append(tagContent);

			} else {

				// Tag, so notify for transformation
				HtmlTemplateTransformationContextImpl context = new HtmlTemplateTransformationContextImpl(
						tagContent);
				transformation.transform(context);

				// Prepend content
				if (context.prepend.length() > 0) {
					transformed.append(context.prepend);
				}

				// Add tag content
				if (context.input.length() == 0) {
					// Just the tag content
					transformed.append(tagContent);
				} else {
					// Include on the tag content
					switch (context.tagType) {
					case OPEN:
						// Append
						transformed.append(tagContent);
						transformed.append(context.input);
						break;
					case OPEN_CLOSE:
						// Obtain without closing slash and bracket
						tagContent = tagContent.substring(0,
								tagContent.length() - "/>".length());

						// Input between split open/close tags
						transformed.append(tagContent);
						transformed.append(">");
						transformed.append(context.input);
						transformed.append("</" + context.name + ">");
						break;
					case CLOSE:
						// Prepend
						transformed.append(context.input);
						transformed.append(tagContent);
						break;
					}
				}

				// Append content
				if (context.append.length() > 0) {
					transformed.append(context.append);
				}
			}

			// Initiate for next iteration
			tagStart = rawHtml.indexOf('<', tagEnd);
			if (tagStart < 0) {
				// No further tags, so include remaining
				tagContent = rawHtml.substring(tagEnd);
				transformed.append(tagContent);
				break TRANSFORMED; // all content included
			}
			previousTagEnd = tagEnd;
		}

		// Return transformed content
		return transformed.toString();
	}

	/**
	 * {@link HtmlTemplateTransformationContext} implementation.
	 */
	private static class HtmlTemplateTransformationContextImpl implements
			HtmlTemplateTransformationContext {

		/**
		 * Tag content.
		 */
		private final String tagContent;

		/**
		 * {@link HtmlTagType}.
		 */
		private final HtmlTagType tagType;

		/**
		 * Tag name.
		 */
		public final String name;

		/**
		 * Attributes.
		 */
		private final Map<String, String> attributes = new HashMap<String, String>();

		/**
		 * Prepend content.
		 */
		public String prepend = "";

		/**
		 * Input content.
		 */
		public String input = "";

		/**
		 * Append content.
		 */
		public String append = "";

		/**
		 * Initiate.
		 * 
		 * @param tagContent
		 *            Tag content.
		 */
		public HtmlTemplateTransformationContextImpl(String tagContent) {
			this.tagContent = tagContent;

			// Parse out details, by first removing opening/closing brackets
			String content = this.tagContent.substring("<".length(),
					this.tagContent.length() - ">".length());

			// Determine type of tag (and remove indicator)
			if (content.startsWith("/")) {
				this.tagType = HtmlTagType.CLOSE;
				content = content.substring("/".length());
			} else if (content.endsWith("/")) {
				this.tagType = HtmlTagType.OPEN_CLOSE;
				content = content.substring(0, content.length() - "/".length());
			} else {
				this.tagType = HtmlTagType.OPEN;
			}

			// Obtain the name (and strip from content)
			this.name = content.split("\\s")[0];
			content = content.substring(this.name.length()).trim();

			// Parse out the attributes
			String[] segments = content.split("=");
			String name = segments[0].trim();
			for (int i = 1; i < segments.length; i++) {
				String segment = segments[i];

				// Maintain the attribute name
				String attributeName = name;

				// Determine if last segment (as value only)
				String attributeValue;
				if (i == (segments.length - 1)) {
					// Last segment, so all value
					attributeValue = segment.trim();

				} else {
					// Obtain the next name
					String[] names = segment.split("\\s");
					name = names[names.length - 1];

					// Rest will be the attribute value
					attributeValue = segment.substring(0,
							segment.length() - name.length()).trim();
				}

				// Determine if strip quotes
				if (attributeValue.startsWith("\"")
						|| attributeValue.startsWith("'")) {
					attributeValue = attributeValue.substring(1,
							attributeValue.length() - 1);
				}

				// Add the attribute (with case insensitive name)
				this.attributes
						.put(attributeName.toUpperCase(), attributeValue);
			}
		}

		/*
		 * ================= HtmlTemplateTransformationContext =================
		 */

		@Override
		public HtmlTagType getTagType() {
			return this.tagType;
		}

		@Override
		public String getTagName() {
			return this.name;
		}

		@Override
		public String getAttributeValue(String name) {
			// Obtain attribute by case insensitive name
			return this.attributes.get(name.toUpperCase());
		}

		@Override
		public void prependContent(String content) {
			this.prepend += content;
		}

		@Override
		public void inputContent(String content) {
			this.input += content;
		}

		@Override
		public void appendContent(String content) {
			this.append += content;
		}
	}

}