package net.officefloor.web.template.parse;

import java.util.LinkedList;
import java.util.List;

/**
 * Test configuration.
 * 
 * @author Daniel Sagenschneider
 */
public class BeanTemplateSectionContentConfig implements
		TemplateSectionContentConfig {

	/**
	 * Property name to obtain the bean.
	 */
	public String beanName;

	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	/**
	 * Allows overriding the open tag. May have spacing or within comment.
	 */
	public String openTag = null;

	public void setOpenTag(String openTag) {
		this.openTag = openTag;
	}

	/**
	 * Obtains the open tag.
	 * 
	 * @return Open tag.
	 */
	public String getOpenTag() {
		return (this.openTag == null ? ("${" + this.beanName + " ")
				: this.openTag);
	}

	/**
	 * Allows overriding the close tag. May be blank, have spacing or within
	 * comment.
	 */
	public String closeTag = " $}";

	public void setCloseTag(String closeTag) {
		this.closeTag = closeTag;
	}

	/**
	 * Obtains the close tag.
	 * 
	 * @return Close tag.
	 */
	public String getCloseTag() {
		return (this.closeTag == null ? "" : this.closeTag);
	}

	/**
	 * {@link TemplateSectionContentConfig} instances.
	 */
	public List<TemplateSectionContentConfig> contents = new LinkedList<TemplateSectionContentConfig>();

	public void addContent(TemplateSectionContentConfig content) {
		this.contents.add(content);
	}
}