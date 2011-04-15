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
package net.officefloor.plugin.web.http.template.section;

import junit.framework.TestCase;
import net.officefloor.plugin.section.clazz.NextTask;
import net.officefloor.plugin.web.http.parameters.source.HttpParametersObjectManagedObjectSource;
import net.officefloor.plugin.web.http.template.HttpParameters;

/**
 * Provides logic for the HTTP Parameters template.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpParametersTemplateLogic {

	/**
	 * {@link HttpParametersBean}.
	 */
	private HttpParametersBean bean;

	/**
	 * Handles the submit.
	 * 
	 * @param request
	 *            {@link HttpParametersBean}.
	 */
	public void submit(HttpParametersBean request) {
		this.bean = request;
	}

	/**
	 * Provides the values for the template.
	 * 
	 * @param request
	 *            {@link HttpParametersBean}.
	 * @return {@link HttpParametersBean}.
	 */
	public HttpParametersBean getTemplate(HttpParametersBean request) {
		TestCase.assertSame("Must be same bean", this.bean, request);
		return request;
	}

	/**
	 * Required from test configuration.
	 */
	@NextTask("doExternalFlow")
	public void required() {
	}

	/**
	 * Parameter to be a {@link HttpParametersObjectManagedObjectSource}.
	 */
	@HttpParameters
	public static class HttpParametersBean {

		/**
		 * Text.
		 */
		private String text;

		/**
		 * Obtains the text.
		 * 
		 * @return Text.
		 */
		public String getText() {
			return this.text;
		}

		/**
		 * Specifies the text.
		 * 
		 * @param text
		 *            Text.
		 */
		public void setText(String text) {
			this.text = text;
		}
	}

}