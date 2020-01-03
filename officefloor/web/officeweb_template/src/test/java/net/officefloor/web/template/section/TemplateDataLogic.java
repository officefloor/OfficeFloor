package net.officefloor.web.template.section;

import net.officefloor.plugin.section.clazz.Next;

/**
 * Logic for template with Data suffix on method name.
 * 
 * @author Daniel Sagenschneider
 */
public class TemplateDataLogic {

	/**
	 * Obtains the bean for starting template.
	 * 
	 * @return Starting template bean.
	 */
	public TemplateDataLogic getTemplateData() {
		return this;
	}

	/**
	 * Obtains the message.
	 * 
	 * @return Message.
	 */
	public String getMessage() {
		return "hello world";
	}

	/**
	 * Obtains the bean for section data.
	 * 
	 * @return Section data.
	 */
	public TemplateDataLogic getSectionData() {
		return this;
	}

	/**
	 * Obtains the description for the section.
	 * 
	 * @return Description.
	 */
	public String getDescription() {
		return "section data";
	}

	/**
	 * Required to have output flow for integration testing setup.
	 */
	@Next("doExternalFlow")
	public void requiredForIntegration() {
	}

}