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

import java.util.LinkedList;
import java.util.List;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.gwt.template.tranform.HtmlTagType;
import net.officefloor.plugin.gwt.template.tranform.HtmlTemplateTransformation;
import net.officefloor.plugin.gwt.template.tranform.HtmlTemplateTransformationContext;
import net.officefloor.plugin.gwt.template.tranform.HtmlTemplateTransformer;

/**
 * Tests the {@link HtmlTemplateTransformer}.
 * 
 * @author Daniel Sagenschneider
 */
public class HtmlTemplateTransformerTest extends OfficeFrameTestCase implements
		HtmlTemplateTransformation {

	/**
	 * Empty HTML tag.
	 */
	public void testEmptyHtml() {
		this.addTag(HtmlTagType.OPEN, "html");
		this.addTag(HtmlTagType.CLOSE, "html");
		this.doTransformTest("<html></html>", null);
	}

	/**
	 * Just HTML tag.
	 */
	public void testJustHtml() {
		this.addTag(HtmlTagType.OPEN_CLOSE, "html");
		this.doTransformTest("<html/>", null);
	}

	/**
	 * Just Body within HTML tag.
	 */
	public void testJustBody() {
		this.addTag(HtmlTagType.OPEN, "html");
		this.addTag(HtmlTagType.OPEN_CLOSE, "body");
		this.addTag(HtmlTagType.CLOSE, "html");
		this.doTransformTest("<html><body/></html>", null);
	}

	/**
	 * Ensure pick up attribute.
	 */
	public void testAttribute() {
		this.addTag(HtmlTagType.OPEN_CLOSE, "html", "name", "value");
		this.doTransformTest("<html name=\"value\"/>", null);
	}

	/**
	 * Ensure able to parse multiple attributes.
	 */
	public void testMultipleAttributes() {
		this.addTag(HtmlTagType.OPEN_CLOSE, "html", "one", "A", "two", "B");
		this.doTransformTest("<html one='A' two='B' />", null);
	}

	/**
	 * Ensure attribute name may be case insensitive.
	 */
	public void testCaseInsensitiveAttributeName() {
		this.addTag(HtmlTagType.OPEN_CLOSE, "html", "VaRiOuS_CaSe", "value");
		this.doTransformTest("<html various_CASE=value />", null);
	}

	/**
	 * Ensure can prepend content.
	 */
	public void testPrepend() {
		this.addTag(HtmlTagType.OPEN, "html");
		this.addTag(HtmlTagType.OPEN_CLOSE, "body").prepend = "PREPEND";
		this.addTag(HtmlTagType.CLOSE, "html");
		this.doTransformTest("<html><body/></html>",
				"<html>PREPEND<body/></html>");
	}

	/**
	 * Ensure can append content.
	 */
	public void testAppend() {
		this.addTag(HtmlTagType.OPEN, "html");
		this.addTag(HtmlTagType.OPEN_CLOSE, "body").append = "APPEND";
		this.addTag(HtmlTagType.CLOSE, "html");
		this.doTransformTest("<html><body/></html>",
				"<html><body/>APPEND</html>");
	}

	/**
	 * Ensure can input content.
	 */
	public void testInputOnOpen() {
		this.addTag(HtmlTagType.OPEN, "html").include = "INPUT";
		this.addTag(HtmlTagType.CLOSE, "html");
		this.doTransformTest("<html></html>", "<html>INPUT</html>");
	}

	/**
	 * Ensure can input content.
	 */
	public void testInputOnOpenClose() {
		this.addTag(HtmlTagType.OPEN, "html");
		this.addTag(HtmlTagType.OPEN_CLOSE, "body").include = "INPUT";
		this.addTag(HtmlTagType.CLOSE, "html");
		this.doTransformTest("<html><body/></html>",
				"<html><body>INPUT</body></html>");
	}

	/**
	 * Ensure can input content.
	 */
	public void testInputOnClose() {
		this.addTag(HtmlTagType.OPEN, "html");
		this.addTag(HtmlTagType.CLOSE, "html").include = "INPUT";
		this.doTransformTest("<html></html>", "<html>INPUT</html>");
	}

	/**
	 * Ensure includes spacing.
	 */
	public void testSpacing() {
		this.addTag(HtmlTagType.OPEN, "html");
		this.addTag(HtmlTagType.OPEN_CLOSE, "body");
		this.addTag(HtmlTagType.CLOSE, "html");
		this.doTransformTest("<html > \t \r \n <body /> </html>", null);
	}

	/**
	 * Ensure ignore comments.
	 */
	public void testIgnoreComment() {
		this.addTag(HtmlTagType.OPEN, "html").append = "A";
		this.addTag(HtmlTagType.CLOSE, "html").prepend = "B";
		this.doTransformTest("<html><!-- <html/> --></html>",
				"<html>A<!-- <html/> -->B</html>");
	}

	/**
	 * Ensure can handle no closing bracket.
	 */
	public void testEdgeCase_NoClosingBracket() {
		this.doTransformTest("<html", null);
	}

	/**
	 * Ensure can handle no closing comment.
	 */
	public void testEdgeCase_NoClosingComment() {
		this.doTransformTest("<!-- no closing >", null);
	}

	/**
	 * Ensure can handle empty.
	 */
	public void testEdgeCase_Empty() {
		this.doTransformTest("", null);
	}

	/**
	 * Ensure can handle no tags.
	 */
	public void testEdgeCase_NoTags() {
		this.doTransformTest(" ", null);
	}

	/**
	 * Ensure can handle surrounding content.
	 */
	public void testEdgeCase_SurroundingContent() {
		this.addTag(HtmlTagType.OPEN_CLOSE, "html");
		this.doTransformTest(" <html/> ", null);
	}

	/**
	 * Ensure can handle no attribute value.
	 */
	public void testEdgeCase_NoAttributeValue() {
		this.addTag(HtmlTagType.OPEN_CLOSE, "html");
		this.doTransformTest("<html name />", null);
	}

	/**
	 * Ensure can handle no attribute value.
	 */
	public void testEdgeCase_NoAttributeClosingQuote() {
		this.addTag(HtmlTagType.OPEN_CLOSE, "html", "name", "tes");
		this.doTransformTest("<html name=\"test />", null);
	}

	/**
	 * Ensure can handle attribute spacing.
	 */
	public void testEdgeCase_AttributeSpacing() {
		this.addTag(HtmlTagType.OPEN_CLOSE, "html", "name", "test");
		this.doTransformTest("<html name = test />", null);
	}

	/*
	 * ==================== HtmlTemplateTransformation ===================
	 */

	@Override
	public void transform(HtmlTemplateTransformationContext context) {

		// Ensure still expecting tag
		if (this.expectedTags.size() == 0) {
			fail("Unknown parsed tag "
					+ new TagStruct(context.getTagType(), context.getTagName()));
		}

		// Should be as first expected tag
		TagStruct tag = this.expectedTags.remove(0);
		assertEquals("Incorrect tag name", tag.name, context.getTagName());
		assertEquals("Incorrect type for tag " + tag.name, tag.type,
				context.getTagType());
		for (int i = 0; i < tag.attributeNameValues.length; i += 2) {
			String name = tag.attributeNameValues[i];
			assertEquals("Incorrect attribute " + name + " [" + tag.name + "]",
					tag.attributeNameValues[i + 1],
					context.getAttributeValue(name));
		}

		// Add transformation content
		if (tag.prepend != null) {
			context.prependContent(tag.prepend);
		}
		if (tag.include != null) {
			context.inputContent(tag.include);
		}
		if (tag.append != null) {
			context.appendContent(tag.append);
		}
	}

	/**
	 * Expected tags.
	 */
	private final List<TagStruct> expectedTags = new LinkedList<TagStruct>();

	/**
	 * Tests the transforming.
	 * 
	 * @param raw
	 *            Raw text for transforming.
	 * @param expectedTransformed
	 *            Expected transformed text. <code>null</code> indicates should
	 *            not change.
	 */
	private void doTransformTest(String raw, String expectedTransformed) {

		// Transform the raw content
		HtmlTemplateTransformer transformer = new HtmlTemplateTransformerImpl();
		String transformed = transformer.transform(raw, this);

		// Ensure all tags as expected
		if (this.expectedTags.size() > 0) {
			StringBuilder text = new StringBuilder();
			for (TagStruct tag : this.expectedTags) {
				text.append("   " + tag.toString() + "\n");
			}
			fail("Expected tags not parsed:\n" + text.toString());
		}

		// Determine if correctly transformed
		String expected = (expectedTransformed == null ? raw
				: expectedTransformed);
		assertEquals("Incorrectly transformed", expected, transformed);
	}

	/**
	 * Adds an expected tag.
	 * 
	 * @param type
	 *            {@link HtmlTagType}.
	 * @param name
	 *            Tag name.
	 * @param attributeNameValues
	 *            Attribute name/value pairs.
	 * @return {@link TagStruct} added.
	 */
	private TagStruct addTag(HtmlTagType type, String name,
			String... attributeNameValues) {
		TagStruct tag = new TagStruct(type, name, attributeNameValues);
		this.expectedTags.add(tag);
		return tag;
	}

	/**
	 * Expected tag.
	 */
	private static class TagStruct {

		/**
		 * {@link HtmlTagType}.
		 */
		public final HtmlTagType type;

		/**
		 * Tag name.
		 */
		private final String name;

		/**
		 * Attribute name/value pairs.
		 */
		private final String[] attributeNameValues;

		/**
		 * Prepend.
		 */
		public String prepend = null;

		/**
		 * Include.
		 */
		public String include = null;

		/**
		 * Append.
		 */
		public String append = null;

		/**
		 * Initiate.
		 * 
		 * @param type
		 *            {@link HtmlTagType}.
		 * @param name
		 *            Tag name.
		 * @param attributeNameValues
		 *            Attribute name/value pairs.
		 */
		public TagStruct(HtmlTagType type, String name,
				String... attributeNameValues) {
			this.type = type;
			this.name = name;
			this.attributeNameValues = attributeNameValues;
		}

		/*
		 * ===================== Object =============================
		 */

		@Override
		public String toString() {
			StringBuilder text = new StringBuilder();
			text.append(this.type == HtmlTagType.CLOSE ? "</" : "<");
			text.append(this.name);
			for (int i = 0; i < this.attributeNameValues.length; i += 2) {
				text.append(" ");
				text.append(this.attributeNameValues[i]);
				text.append("=\"");
				text.append(this.attributeNameValues[i + 1]);
				text.append("\"");
			}
			text.append(this.type == HtmlTagType.OPEN_CLOSE ? "/>" : ">");
			return text.toString();
		}
	}

}