/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
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
package net.officefloor.activity.source;

import net.officefloor.activity.procedure.Procedure;
import net.officefloor.plugin.section.clazz.Parameter;

/**
 * Example {@link Procedure} for testing.
 * 
 * @author Daniel Sagenschneider
 */
public class ExampleProcedure {

	/**
	 * Indicates if the procedure was run.
	 */
	public static boolean isProcedureRun = false;

	/**
	 * Result.
	 */
	public static String result = null;

	/*
	 * ============ Procedures =================
	 */

	public void procedure() {
		isProcedureRun = true;
	}

	public String passThrough(@Parameter String value) {
		return value;
	}

	public void result(@Parameter String value) {
		result = value;
	}
}