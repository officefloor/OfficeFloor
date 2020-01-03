package net.officefloor.web.template.parse;

import net.officefloor.web.template.parse.BeanParsedTemplateSectionContent;
import net.officefloor.web.template.parse.ParsedTemplateSectionContent;

/**
 * {@link ParsedTemplateSectionContent} that references a bean to use.
 * 
 * @author Daniel Sagenschneider
 */
public class BeanParsedTemplateSectionContent implements ParsedTemplateSectionContent {

	/**
	 * Property name to obtain the bean.
	 */
	private final String beanName;

	/**
	 * {@link ParsedTemplateSectionContent} instances for the bean.
	 */
	private final ParsedTemplateSectionContent[] contents;

	/**
	 * Initiate.
	 * 
	 * @param beanName
	 *            Property name to obtain the bean.
	 * @param contents
	 *            {@link ParsedTemplateSectionContent} instances for the bean.
	 */
	public BeanParsedTemplateSectionContent(String beanName, ParsedTemplateSectionContent[] contents) {
		this.beanName = beanName;
		this.contents = contents;
	}

	/**
	 * Obtains the name of the property to obtain the bean.
	 * 
	 * @return Name of the property to obtain the bean.
	 */
	public String getPropertyName() {
		return this.beanName;
	}

	/**
	 * Obtains the {@link ParsedTemplateSectionContent} instances that comprise
	 * the content for this {@link BeanParsedTemplateSectionContent}.
	 * 
	 * @return {@link ParsedTemplateSectionContent} instances that comprise the
	 *         content for this {@link BeanParsedTemplateSectionContent}.
	 */
	public ParsedTemplateSectionContent[] getContent() {
		return this.contents;
	}

}