package net.officefloor.web.template.parse;

import java.util.LinkedList;
import java.util.List;

/**
 * Test configuration.
 * 
 * @author Daniel Sagenschneider
 */
public class TemplateSectionConfig {

	/**
	 * Name of the section.
	 */
	public String name;

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * {@link TemplateSectionContentConfig} instances.
	 */
	public List<TemplateSectionContentConfig> contents = new LinkedList<TemplateSectionContentConfig>();

	public void addContent(TemplateSectionContentConfig content) {
		this.contents.add(content);
	}
}
