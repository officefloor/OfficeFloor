/*-
 * #%L
 * Activity
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.activity.source;

import java.sql.SQLException;

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

	/**
	 * {@link SQLException}.
	 */
	public static SQLException failure = null;

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

	public void injectObject(String value) {
		result = value;
	}

	public void propagate(@Parameter SQLException value) throws SQLException {
		throw value;
	}

	public void handleEscalation(@Parameter SQLException value) {
		failure = value;
	}

}
