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
package net.officefloor.polyglot.script;

import javax.script.ScriptException;

import net.officefloor.server.http.HttpException;

/**
 * <p>
 * Translate {@link ScriptException} to possible more appropriate
 * {@link Throwable}.
 * <p>
 * For example, the {@link HttpException} may be wrapped in the
 * {@link ScriptException} and this provides means to extract and throw.
 * 
 * @author Daniel Sagenschneider
 */
public interface ScriptExceptionTranslator {

	/**
	 * Translates the {@link ScriptException}.
	 * 
	 * @param scriptException {@link ScriptException}.
	 * @return Translated {@link Throwable} or <code>null</code> to throw original
	 *         {@link ScriptException}.
	 */
	Throwable translate(ScriptException scriptException);

}