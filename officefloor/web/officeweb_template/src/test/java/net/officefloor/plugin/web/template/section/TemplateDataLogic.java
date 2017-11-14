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
package net.officefloor.plugin.web.template.section;

import net.officefloor.plugin.section.clazz.NextFunction;

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
	@NextFunction("doExternalFlow")
	public void requiredForIntegration() {
	}

}