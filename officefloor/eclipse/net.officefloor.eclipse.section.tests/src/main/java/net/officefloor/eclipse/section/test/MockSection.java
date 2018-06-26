/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.eclipse.section.test;

import net.officefloor.eclipse.section.SectionEditor;
import net.officefloor.plugin.managedfunction.clazz.FlowInterface;

/**
 * Mock section {@link Class} for testing the {@link SectionEditor}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockSection {

	@FlowInterface
	public static interface Flows {

		void outputOne();

		void outputTwo(String value);
	}

	public void inputOne() {
	}

	public void inputTwo(MockObject object) {
		object.setValue("inputTwo");
	}

	public void inputThree(Flows flows) {
		flows.outputTwo("inputThree");
	}
}