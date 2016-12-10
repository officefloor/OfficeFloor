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
package net.officefloor.tutorial.sectionhttpserver;

import net.officefloor.plugin.work.clazz.FlowInterface;
import lombok.Data;

/**
 * Example logic for the template.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: example
public class TemplateLogic {

	@Data
	public static class Values {

		private final String text;
	}

	@FlowInterface
	public static interface Flows {

		void noBean();
	}

	/**
	 * Obtains the data for the template section.
	 * 
	 * @return {@link Values}.
	 */
	public Values getTemplateData() {
		return new Values("Hi");
	}

	/**
	 * Obtains the data for the hello section.
	 * 
	 * @return {@link Values}.
	 */
	public Values getHelloData() {
		return new Values("Hello");
	}

	/**
	 * Skips not render section.
	 * 
	 * @param flows
	 *            {@link Flows} which allows rendering control over the sections
	 *            of the template. As this method is called before rendering the
	 *            section it skips rendering to the <code>noBean</code> section.
	 */
	public void getNotRender(Flows flows) {
		flows.noBean();
	}

}
// END SNIPPET: example