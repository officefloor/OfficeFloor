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
package net.officefloor.tutorial.navigatehttpserver;

import net.officefloor.plugin.work.clazz.FlowInterface;

/**
 * Logic for <code>TemplateTwo.ofp</code>.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: example
public class TemplateTwo {

	@FlowInterface
	public interface Flows {
		void next();
	}

	public void process(Flows flows) {
		flows.next();
	}

}
// END SNIPPET: example