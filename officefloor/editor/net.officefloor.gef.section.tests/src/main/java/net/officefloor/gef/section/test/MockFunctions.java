/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.gef.section.test;

import java.io.IOException;
import java.sql.SQLException;

import net.officefloor.gef.section.SectionEditor;
import net.officefloor.plugin.managedfunction.clazz.FlowInterface;

/**
 * Functions for testing {@link SectionEditor}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockFunctions {

	@FlowInterface
	public static interface Flows {

		void outputOne();

		void outputTwo(String value);
	}

	public void functionOne() {
	}

	public void functionTwo(MockObject object, String parameter) {
		object.setValue("functionTwo");
	}

	public String functionThree(Flows flows) {
		flows.outputTwo("functionThree");
		return "functionThree";
	}

	public void functionFour() throws IOException, SQLException, RuntimeException {
	}
}