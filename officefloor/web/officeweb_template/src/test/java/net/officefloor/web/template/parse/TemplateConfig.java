package net.officefloor.web.template.parse;

import java.util.LinkedList;
import java.util.List;

/**
 * Test configuration.
 * 
 * @author Daniel Sagenschneider
 */
public class TemplateConfig {

	/**
	 * {@link TemplateSectionConfig} instances.
	 */
	public List<TemplateSectionConfig> sections = new LinkedList<TemplateSectionConfig>();

	public void addSection(TemplateSectionConfig section) {
		this.sections.add(section);
	}
}
