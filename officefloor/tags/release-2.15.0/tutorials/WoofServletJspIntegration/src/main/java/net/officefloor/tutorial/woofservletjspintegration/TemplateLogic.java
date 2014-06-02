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
package net.officefloor.tutorial.woofservletjspintegration;

import net.officefloor.plugin.section.clazz.NextTask;

/**
 * Logic for the template.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class TemplateLogic {

	public RequestBean getTemplate(RequestBean bean) {
		bean.setText("REQUEST");
		return bean;
	}

	public SessionBean getSession(SessionBean bean) {
		bean.setText("SESSION");
		return bean;
	}

	public ApplicationBean getApplication(ApplicationBean bean) {
		// Value set by SetupListener (show upper case for WoOF template)
		bean.setText(bean.getText().toUpperCase());
		return bean;
	}

	@NextTask("JSP")
	public void link(RequestBean requestBean, SessionBean sessionBean,
			ApplicationBean applicationBean) {
		requestBean.setText("REQUEST");
		sessionBean.setText("SESSION");
		applicationBean.setText("application"); // show lower case for JSP
	}
}
// END SNIPPET: tutorial