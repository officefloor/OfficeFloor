/*-
 * #%L
 * PolyglotScript
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

package net.officefloor.script;

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
