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
package net.officefloor.plugin.managedfunction.method;

import java.lang.reflect.Method;

import net.officefloor.frame.api.function.ManagedFunction;

/**
 * Translate the return value of the {@link MethodFunction}.
 * 
 * @param <R> {@link MethodFunction} return type.
 * @param <T> Translated type.
 * 
 * @author Daniel Sagenschneider
 */
public interface MethodReturnTranslator<R, T> {

	/**
	 * Translates the {@link Method} return value for next {@link ManagedFunction}
	 * argument.
	 * 
	 * @param context {@link MethodReturnTranslatorContext}.
	 * @throws Exception If fails to translate.
	 */
	void translate(MethodReturnTranslatorContext<R, T> context) throws Exception;

}